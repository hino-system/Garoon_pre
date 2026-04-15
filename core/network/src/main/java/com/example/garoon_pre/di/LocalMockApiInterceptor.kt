package com.example.garoon_pre.di

import com.example.garoon_pre.core.datastore.ConnectionMode
import com.example.garoon_pre.core.datastore.SessionStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class LocalMockApiInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
    private val storage: LocalMockStorage
) : Interceptor {

    private val backend by lazy { LocalMockApiBackend(storage) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = runBlocking { sessionStore.getConnectionSettings() }
        if (settings.mode != ConnectionMode.LOCAL) {
            return chain.proceed(chain.request())
        }
        return backend.handle(chain.request())
    }
}

private class LocalMockApiBackend(
    private val storage: LocalMockStorage
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val jstZone = ZoneId.of("Asia/Tokyo")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

    @Synchronized
    fun handle(request: Request): Response {
        val path = request.url.encodedPath
        val method = request.method.uppercase()

        return when {
            path == "/api/v1/auth/login" && method == "POST" -> handleLogin(request)

            path == "/api/v1/users" && method == "GET" -> handleGetUsers(request)

            path == "/api/v1/schedules" && method == "GET" -> handleGetSchedules(request)
            path == "/api/v1/schedules" && method == "POST" -> handleCreateSchedule(request)
            path.startsWith("/api/v1/schedules/") && method == "GET" -> {
                handleGetScheduleById(request, path.substringAfterLast("/"))
            }
            path.startsWith("/api/v1/schedules/") && method == "PUT" -> {
                handleUpdateSchedule(request, path.substringAfterLast("/"))
            }

            path == "/api/v1/board-categories" && method == "GET" -> handleGetBoardCategories(request)
            path == "/api/v1/board-posts" && method == "GET" -> handleGetBoardPosts(request)
            path == "/api/v1/board-posts" && method == "POST" -> handleCreateBoardPost(request)
            path.startsWith("/api/v1/board-posts/") && path.endsWith("/comments") && method == "POST" -> {
                val postId = path.removePrefix("/api/v1/board-posts/").removeSuffix("/comments")
                handleCreateBoardComment(request, postId)
            }
            path.startsWith("/api/v1/board-posts/") && method == "GET" -> {
                handleGetBoardPostDetail(request, path.substringAfterLast("/"))
            }
            path.startsWith("/api/v1/board-posts/") && method == "PUT" -> {
                handleUpdateBoardPost(request, path.substringAfterLast("/"))
            }
            path.startsWith("/api/v1/board-posts/") && method == "DELETE" -> {
                handleDeleteBoardPost(request, path.substringAfterLast("/"))
            }
            path.startsWith("/api/v1/board-comments/") && method == "DELETE" -> {
                handleDeleteBoardComment(request, path.substringAfterLast("/"))
            }

            path == "/health" && method == "GET" -> {
                jsonResponse(request, 200, JSONObject().put("ok", true).toString())
            }

            else -> jsonError(request, 404, "ローカルモックに未実装のAPIです: $method $path")
        }
    }

    private fun handleLogin(request: Request): Response {
        val state = storage.load()
        val body = request.readJsonBody()
        val userId = body.optString("userId").trim()
        val password = body.optString("password").trim()

        val user = state.users.firstOrNull { it.userId == userId && it.password == password }
            ?: return jsonError(request, 401, "ユーザーIDまたはパスワードが違います")

        val token = "local-token-${user.id}"
        val userJson = JSONObject()
            .put("id", user.id)
            .put("userId", user.userId)
            .put("displayName", user.displayName)
            .putNullable("department1", user.department1)
            .putNullable("department2", user.department2)
            .put("position", user.position)
            .put("role", user.role)

        return jsonResponse(
            request,
            200,
            JSONObject()
                .put("token", token)
                .put("user", userJson)
                .toString()
        )
    }

    private fun handleGetUsers(request: Request): Response {
        val state = storage.load()
        requireCurrentUser(request, state) ?: return unauthorized(request)

        val items = JSONArray()
        state.users.forEach { user ->
            items.put(
                JSONObject()
                    .put("id", user.id)
                    .put("userId", user.userId)
                    .put("displayName", user.displayName)
                    .putNullable("department1", user.department1)
                    .putNullable("department2", user.department2)
                    .put("position", user.position)
                    .put("role", user.role)
            )
        }

        return jsonResponse(
            request,
            200,
            JSONObject().put("items", items).toString()
        )
    }

    private fun handleGetSchedules(request: Request): Response {
        val state = storage.load()
        requireCurrentUser(request, state) ?: return unauthorized(request)

        val date = request.url.queryParameter("date").orEmpty()
        if (date.length != 8) {
            return jsonError(request, 400, "date を指定してください")
        }

        val requestedUserIds = request.url.queryParameter("userIds")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?: emptyList()

        val items = JSONArray()

        state.schedules
            .filter { schedule ->
                val ownerId = schedule.ownerUserId.orEmpty()
                requestedUserIds.isEmpty() || ownerId in requestedUserIds
            }
            .mapNotNull { schedule ->
                expandScheduleForDate(schedule, date)
            }
            .sortedWith(compareBy<LocalSchedule>({ it.startAt }, { it.organizerName }, { it.title }))
            .forEach { schedule ->
                items.put(scheduleToJson(schedule))
            }

        return jsonResponse(
            request,
            200,
            JSONObject().put("items", items).toString()
        )
    }

    private fun handleGetScheduleById(request: Request, scheduleId: String): Response {
        val state = storage.load()
        requireCurrentUser(request, state) ?: return unauthorized(request)

        val schedule = state.schedules.firstOrNull { it.id == scheduleId }
            ?: return jsonError(request, 404, "予定が見つかりません")

        return jsonResponse(request, 200, scheduleToJson(schedule).toString())
    }

    private fun handleCreateSchedule(request: Request): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)
        val body = request.readJsonBody()

        val created = LocalSchedule(
            id = UUID.randomUUID().toString(),
            title = body.optString("title").trim(),
            startAt = body.optString("startAt").trim(),
            endAt = body.optString("endAt").trim(),
            repeatRule = body.optString("repeatRule").ifBlank { "なし" },
            location = body.optNullableString("location"),
            description = body.optNullableString("description"),
            ownerUserId = currentUser.id,
            organizerName = currentUser.displayName
        )

        state.schedules.add(created)
        storage.save(state)

        return jsonResponse(request, 200, scheduleToJson(created).toString())
    }

    private fun handleUpdateSchedule(request: Request, scheduleId: String): Response {
        val state = storage.load()
        requireCurrentUser(request, state) ?: return unauthorized(request)

        val target = state.schedules.firstOrNull { it.id == scheduleId }
            ?: return jsonError(request, 404, "予定が見つかりません")

        val body = request.readJsonBody()
        target.title = body.optString("title").trim()
        target.startAt = body.optString("startAt").trim()
        target.endAt = body.optString("endAt").trim()
        target.repeatRule = body.optString("repeatRule").ifBlank { "なし" }
        target.location = body.optNullableString("location")
        target.description = body.optNullableString("description")

        storage.save(state)
        return jsonResponse(request, 200, scheduleToJson(target).toString())
    }

    private fun handleGetBoardCategories(request: Request): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)

        val items = JSONArray()
        state.boardCategories.forEach { category ->
            val visiblePosts = visibleBoardPostsFor(currentUser, category.id, state)
            items.put(
                JSONObject()
                    .put("id", category.id)
                    .put("name", category.name)
                    .put("description", category.description)
                    .put("totalPostCount", visiblePosts.size)
                    .put("activePostCount", visiblePosts.size)
                    .put("canPost", canPostCategory(currentUser, category.id))
                    .put("canAdmin", canAdminCategory(currentUser, category.id))
            )
        }

        return jsonResponse(
            request,
            200,
            JSONObject().put("items", items).toString()
        )
    }

    private fun handleGetBoardPosts(request: Request): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)
        val categoryId = request.url.queryParameter("categoryId").orEmpty()

        val category = state.boardCategories.firstOrNull { it.id == categoryId }
            ?: return jsonError(request, 404, "カテゴリーが見つかりません")

        val items = JSONArray()
        visibleBoardPostsFor(currentUser, categoryId, state)
            .sortedByDescending { it.createdAt }
            .forEach { post ->
                items.put(boardPostToJson(post, currentUser, state, includeComments = false))
            }

        val response = JSONObject()
            .put(
                "category",
                JSONObject()
                    .put("id", category.id)
                    .put("name", category.name)
                    .put("description", category.description)
                    .put("canPost", canPostCategory(currentUser, category.id))
                    .put("canAdmin", canAdminCategory(currentUser, category.id))
            )
            .put("items", items)

        return jsonResponse(request, 200, response.toString())
    }

    private fun handleGetBoardPostDetail(request: Request, postId: String): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)

        val post = state.boardPosts.firstOrNull { it.id == postId }
            ?: return jsonError(request, 404, "掲示板投稿が見つかりません")

        if (!canViewPost(currentUser, post)) {
            return jsonError(request, 403, "閲覧権限がありません")
        }

        return jsonResponse(
            request,
            200,
            boardPostToJson(post, currentUser, state, includeComments = true).toString()
        )
    }

    private fun handleCreateBoardPost(request: Request): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)
        val body = request.readJsonBody()
        val categoryId = body.optString("categoryId").trim()

        if (!canPostCategory(currentUser, categoryId)) {
            return jsonError(request, 403, "投稿権限がありません")
        }

        val now = nowYmdHm()

        val created = LocalBoardPost(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            targetDepartment1 = body.optNullableString("targetDepartment1"),
            title = body.optString("title").trim(),
            body = body.optString("body").trim(),
            startAt = body.optString("startAt").trim(),
            endAt = body.optString("endAt").trim(),
            allowComments = body.optBoolean("allowComments", true),
            authorUserId = currentUser.id,
            authorName = currentUser.displayName,
            createdAt = now,
            updatedAt = now
        )

        state.boardPosts.add(created)
        storage.save(state)

        return jsonResponse(
            request,
            200,
            boardPostToJson(created, currentUser, state, includeComments = true).toString()
        )
    }

    private fun handleUpdateBoardPost(request: Request, postId: String): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)

        val post = state.boardPosts.firstOrNull { it.id == postId }
            ?: return jsonError(request, 404, "掲示板投稿が見つかりません")

        if (!canEditPost(currentUser, post)) {
            return jsonError(request, 403, "編集権限がありません")
        }

        val body = request.readJsonBody()
        post.title = body.optString("title").trim()
        post.body = body.optString("body").trim()
        post.startAt = body.optString("startAt").trim()
        post.endAt = body.optString("endAt").trim()
        post.allowComments = body.optBoolean("allowComments", true)
        post.targetDepartment1 = body.optNullableString("targetDepartment1")
        post.updatedAt = nowYmdHm()

        storage.save(state)

        return jsonResponse(
            request,
            200,
            boardPostToJson(post, currentUser, state, includeComments = true).toString()
        )
    }

    private fun handleDeleteBoardPost(request: Request, postId: String): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)

        val post = state.boardPosts.firstOrNull { it.id == postId }
            ?: return jsonError(request, 404, "掲示板投稿が見つかりません")

        if (!canDeletePost(currentUser, post)) {
            return jsonError(request, 403, "削除権限がありません")
        }

        state.boardPosts.removeAll { it.id == postId }
        storage.save(state)

        return jsonResponse(request, 200, JSONObject().put("ok", true).toString())
    }

    private fun handleCreateBoardComment(request: Request, postId: String): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)

        val post = state.boardPosts.firstOrNull { it.id == postId }
            ?: return jsonError(request, 404, "掲示板投稿が見つかりません")

        if (!canViewPost(currentUser, post)) {
            return jsonError(request, 403, "閲覧権限がありません")
        }

        if (!post.allowComments) {
            return jsonError(request, 400, "コメント不可の投稿です")
        }

        val body = request.readJsonBody()
        val now = nowYmdHm()

        val comment = LocalBoardComment(
            id = UUID.randomUUID().toString(),
            postId = post.id,
            body = body.optString("body").trim(),
            authorUserId = currentUser.id,
            authorName = currentUser.displayName,
            createdAt = now,
            updatedAt = now
        )

        post.comments.add(comment)
        storage.save(state)

        return jsonResponse(
            request,
            200,
            boardCommentToJson(comment, currentUser, state).toString()
        )
    }

    private fun handleDeleteBoardComment(request: Request, commentId: String): Response {
        val state = storage.load()
        val currentUser = requireCurrentUser(request, state) ?: return unauthorized(request)

        val targetPost = state.boardPosts.firstOrNull { post ->
            post.comments.any { it.id == commentId }
        } ?: return jsonError(request, 404, "コメントが見つかりません")

        val comment = targetPost.comments.first { it.id == commentId }
        val canDelete = comment.authorUserId == currentUser.id ||
                canAdminCategory(currentUser, targetPost.categoryId)

        if (!canDelete) {
            return jsonError(request, 403, "コメント削除権限がありません")
        }

        targetPost.comments.removeAll { it.id == commentId }
        storage.save(state)

        return jsonResponse(request, 200, JSONObject().put("ok", true).toString())
    }

    private fun visibleBoardPostsFor(
        currentUser: LocalUser,
        categoryId: String,
        state: LocalMockState
    ): List<LocalBoardPost> {
        return state.boardPosts.filter { it.categoryId == categoryId && canViewPost(currentUser, it) }
    }

    private fun canViewPost(currentUser: LocalUser, post: LocalBoardPost): Boolean {
        if (post.categoryId != "cat-department") return true
        val targetDepartment = post.targetDepartment1?.takeIf { it.isNotBlank() } ?: return true
        return currentUser.department1 == targetDepartment
    }

    private fun canPostCategory(currentUser: LocalUser, categoryId: String): Boolean {
        return when (categoryId) {
            "cat-company" -> currentUser.role in setOf("department_manager", "president")
            "cat-department" -> currentUser.role in setOf("section_manager", "department_manager", "president")
            "cat-free" -> true
            else -> false
        }
    }

    private fun canAdminCategory(currentUser: LocalUser, categoryId: String): Boolean {
        return when (categoryId) {
            "cat-company" -> currentUser.role in setOf("department_manager", "president")
            "cat-department" -> currentUser.role in setOf("section_manager", "department_manager", "president")
            "cat-free" -> currentUser.role in setOf("department_manager", "president")
            else -> false
        }
    }

    private fun canEditPost(currentUser: LocalUser, post: LocalBoardPost): Boolean {
        return post.authorUserId == currentUser.id || canAdminCategory(currentUser, post.categoryId)
    }

    private fun canDeletePost(currentUser: LocalUser, post: LocalBoardPost): Boolean {
        return canEditPost(currentUser, post)
    }

    private fun requireCurrentUser(
        request: Request,
        state: LocalMockState
    ): LocalUser? {
        val authHeader = request.header("Authorization").orEmpty()
        if (!authHeader.startsWith("Bearer ")) return null

        val token = authHeader.removePrefix("Bearer ").trim()
        val userId = token.removePrefix("local-token-")
        return state.users.firstOrNull { it.id == userId }
    }

    private fun unauthorized(request: Request): Response {
        return jsonError(request, 401, "ローカルモードの認証情報がありません")
    }

    private fun scheduleToJson(item: LocalSchedule): JSONObject {
        return JSONObject()
            .put("id", item.id)
            .put("title", item.title)
            .put("startAt", item.startAt)
            .put("endAt", item.endAt)
            .put("repeatRule", item.repeatRule)
            .putNullable("location", item.location)
            .putNullable("description", item.description)
            .putNullable("ownerUserId", item.ownerUserId)
            .put("organizerName", item.organizerName)
    }

    private fun boardPostToJson(
        post: LocalBoardPost,
        currentUser: LocalUser,
        state: LocalMockState,
        includeComments: Boolean
    ): JSONObject {
        val category = state.boardCategories.firstOrNull { it.id == post.categoryId }
        val commentsJson = JSONArray()

        if (includeComments) {
            post.comments
                .sortedBy { it.createdAt }
                .forEach { commentsJson.put(boardCommentToJson(it, currentUser, state)) }
        }

        return JSONObject()
            .put("id", post.id)
            .put("categoryId", post.categoryId)
            .put("categoryName", category?.name ?: "")
            .putNullable("targetDepartment1", post.targetDepartment1)
            .put("title", post.title)
            .put("body", post.body)
            .put("startAt", post.startAt)
            .put("endAt", post.endAt)
            .put("allowComments", post.allowComments)
            .put("authorUserId", post.authorUserId)
            .put("authorName", post.authorName)
            .put("createdAt", post.createdAt)
            .put("updatedAt", post.updatedAt)
            .put("status", resolveBoardStatus(post))
            .put("canEdit", canEditPost(currentUser, post))
            .put("canDelete", canDeletePost(currentUser, post))
            .put("canComment", post.allowComments)
            .put("commentCount", post.comments.size)
            .put("comments", commentsJson)
    }

    private fun boardCommentToJson(
        comment: LocalBoardComment,
        currentUser: LocalUser,
        state: LocalMockState
    ): JSONObject {
        val parentPost = state.boardPosts.firstOrNull { it.id == comment.postId }
        val canDelete = comment.authorUserId == currentUser.id ||
                (parentPost != null && canAdminCategory(currentUser, parentPost.categoryId))

        return JSONObject()
            .put("id", comment.id)
            .put("postId", comment.postId)
            .put("body", comment.body)
            .put("authorUserId", comment.authorUserId)
            .put("authorName", comment.authorName)
            .put("createdAt", comment.createdAt)
            .put("updatedAt", comment.updatedAt)
            .put("canDelete", canDelete)
    }

    private fun resolveBoardStatus(post: LocalBoardPost): String {
        val now = nowYmdHm()
        return when {
            now < post.startAt -> "scheduled"
            now > post.endAt -> "expired"
            else -> "active"
        }
    }

    private fun expandScheduleForDate(source: LocalSchedule, requestedDate: String): LocalSchedule? {
        val sourceDate = source.startAt.take(8)
        if (!matchesDate(source, requestedDate)) return null
        if (source.repeatRule == "なし" || sourceDate == requestedDate) return source

        val startTime = source.startAt.takeLast(4)
        val endTime = source.endAt.takeLast(4)

        return source.copy(
            startAt = requestedDate + startTime,
            endAt = requestedDate + endTime
        )
    }

    private fun matchesDate(schedule: LocalSchedule, date: String): Boolean {
        val startDate = runCatching {
            LocalDate.parse(schedule.startAt.take(8), dateFormatter)
        }.getOrNull() ?: return false

        val targetDate = runCatching {
            LocalDate.parse(date, dateFormatter)
        }.getOrNull() ?: return false

        if (targetDate.isBefore(startDate)) return false
        if (schedule.startAt.take(8) == date) return true

        return when (schedule.repeatRule) {
            "毎日" -> true
            "営業日（月〜金）" -> targetDate.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            "毎週" -> startDate.dayOfWeek == targetDate.dayOfWeek
            "毎月" -> startDate.dayOfMonth == targetDate.dayOfMonth
            "毎年" -> startDate.monthValue == targetDate.monthValue &&
                    startDate.dayOfMonth == targetDate.dayOfMonth
            else -> false
        }
    }

    private fun nowYmdHm(): String {
        return LocalDateTime.now(jstZone).format(dateTimeFormatter)
    }

    private fun jsonResponse(request: Request, code: Int, body: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "ERROR")
            .body(body.toResponseBody(jsonMediaType))
            .build()
    }

    private fun jsonError(request: Request, code: Int, message: String): Response {
        return jsonResponse(
            request,
            code,
            JSONObject().put("message", message).toString()
        )
    }

    private fun Request.readJsonBody(): JSONObject {
        val body = body ?: return JSONObject()
        val buffer = Buffer()
        body.writeTo(buffer)
        val raw = buffer.readUtf8()
        return if (raw.isBlank()) JSONObject() else JSONObject(raw)
    }

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotBlank() }
    }

    private fun JSONObject.putNullable(key: String, value: String?): JSONObject {
        return put(key, value ?: JSONObject.NULL)
    }
}