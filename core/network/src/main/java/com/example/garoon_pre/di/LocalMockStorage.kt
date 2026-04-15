package com.example.garoon_pre.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

data class LocalMockState(
    val users: MutableList<LocalUser>,
    val schedules: MutableList<LocalSchedule>,
    val boardCategories: MutableList<LocalBoardCategory>,
    val boardPosts: MutableList<LocalBoardPost>
)

data class LocalUser(
    val id: String,
    val userId: String,
    val password: String,
    val displayName: String,
    val department1: String?,
    val department2: String?,
    val position: String,
    val role: String
)

data class LocalSchedule(
    val id: String,
    var title: String,
    var startAt: String,
    var endAt: String,
    var repeatRule: String,
    var location: String?,
    var description: String?,
    var ownerUserId: String?,
    var organizerName: String
)

data class LocalBoardCategory(
    val id: String,
    val name: String,
    val description: String
)

data class LocalBoardComment(
    val id: String,
    val postId: String,
    var body: String,
    val authorUserId: String,
    val authorName: String,
    val createdAt: String,
    var updatedAt: String
)

data class LocalBoardPost(
    val id: String,
    val categoryId: String,
    var targetDepartment1: String?,
    var title: String,
    var body: String,
    var startAt: String,
    var endAt: String,
    var allowComments: Boolean,
    val authorUserId: String,
    val authorName: String,
    val createdAt: String,
    var updatedAt: String,
    val comments: MutableList<LocalBoardComment> = mutableListOf()
)

@Singleton
class LocalMockStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val file = File(context.filesDir, "local_mock_state.json")

    @Synchronized
    fun load(): LocalMockState {
        if (!file.exists()) {
            val seeded = createSeedState()
            save(seeded)
            return seeded
        }

        return runCatching {
            parseState(JSONObject(file.readText()))
        }.getOrElse {
            val seeded = createSeedState()
            save(seeded)
            seeded
        }
    }

    @Synchronized
    fun save(state: LocalMockState) {
        file.parentFile?.mkdirs()
        file.writeText(stateToJson(state).toString())
    }

    @Synchronized
    fun reset(): LocalMockState {
        val seeded = createSeedState()
        save(seeded)
        return seeded
    }

    private fun stateToJson(state: LocalMockState): JSONObject {
        return JSONObject()
            .put("users", JSONArray().apply {
                state.users.forEach { put(userToJson(it)) }
            })
            .put("schedules", JSONArray().apply {
                state.schedules.forEach { put(scheduleToJson(it)) }
            })
            .put("boardCategories", JSONArray().apply {
                state.boardCategories.forEach { put(categoryToJson(it)) }
            })
            .put("boardPosts", JSONArray().apply {
                state.boardPosts.forEach { put(boardPostToJson(it)) }
            })
    }

    private fun parseState(json: JSONObject): LocalMockState {
        val users = mutableListOf<LocalUser>()
        val schedules = mutableListOf<LocalSchedule>()
        val boardCategories = mutableListOf<LocalBoardCategory>()
        val boardPosts = mutableListOf<LocalBoardPost>()

        val usersArray = json.optJSONArray("users") ?: JSONArray()
        for (i in 0 until usersArray.length()) {
            users += jsonToUser(usersArray.getJSONObject(i))
        }

        val schedulesArray = json.optJSONArray("schedules") ?: JSONArray()
        for (i in 0 until schedulesArray.length()) {
            schedules += jsonToSchedule(schedulesArray.getJSONObject(i))
        }

        val categoriesArray = json.optJSONArray("boardCategories") ?: JSONArray()
        for (i in 0 until categoriesArray.length()) {
            boardCategories += jsonToCategory(categoriesArray.getJSONObject(i))
        }

        val postsArray = json.optJSONArray("boardPosts") ?: JSONArray()
        for (i in 0 until postsArray.length()) {
            boardPosts += jsonToBoardPost(postsArray.getJSONObject(i))
        }

        return LocalMockState(
            users = users,
            schedules = schedules,
            boardCategories = boardCategories,
            boardPosts = boardPosts
        )
    }

    private fun userToJson(item: LocalUser): JSONObject {
        return JSONObject()
            .put("id", item.id)
            .put("userId", item.userId)
            .put("password", item.password)
            .put("displayName", item.displayName)
            .put("department1", item.department1 ?: JSONObject.NULL)
            .put("department2", item.department2 ?: JSONObject.NULL)
            .put("position", item.position)
            .put("role", item.role)
    }

    private fun jsonToUser(json: JSONObject): LocalUser {
        return LocalUser(
            id = json.getString("id"),
            userId = json.getString("userId"),
            password = json.getString("password"),
            displayName = json.getString("displayName"),
            department1 = json.optNullableString("department1"),
            department2 = json.optNullableString("department2"),
            position = json.getString("position"),
            role = json.getString("role")
        )
    }

    private fun scheduleToJson(item: LocalSchedule): JSONObject {
        return JSONObject()
            .put("id", item.id)
            .put("title", item.title)
            .put("startAt", item.startAt)
            .put("endAt", item.endAt)
            .put("repeatRule", item.repeatRule)
            .put("location", item.location ?: JSONObject.NULL)
            .put("description", item.description ?: JSONObject.NULL)
            .put("ownerUserId", item.ownerUserId ?: JSONObject.NULL)
            .put("organizerName", item.organizerName)
    }

    private fun jsonToSchedule(json: JSONObject): LocalSchedule {
        return LocalSchedule(
            id = json.getString("id"),
            title = json.getString("title"),
            startAt = json.getString("startAt"),
            endAt = json.getString("endAt"),
            repeatRule = json.getString("repeatRule"),
            location = json.optNullableString("location"),
            description = json.optNullableString("description"),
            ownerUserId = json.optNullableString("ownerUserId"),
            organizerName = json.getString("organizerName")
        )
    }

    private fun categoryToJson(item: LocalBoardCategory): JSONObject {
        return JSONObject()
            .put("id", item.id)
            .put("name", item.name)
            .put("description", item.description)
    }

    private fun jsonToCategory(json: JSONObject): LocalBoardCategory {
        return LocalBoardCategory(
            id = json.getString("id"),
            name = json.getString("name"),
            description = json.getString("description")
        )
    }

    private fun boardCommentToJson(item: LocalBoardComment): JSONObject {
        return JSONObject()
            .put("id", item.id)
            .put("postId", item.postId)
            .put("body", item.body)
            .put("authorUserId", item.authorUserId)
            .put("authorName", item.authorName)
            .put("createdAt", item.createdAt)
            .put("updatedAt", item.updatedAt)
    }

    private fun jsonToBoardComment(json: JSONObject): LocalBoardComment {
        return LocalBoardComment(
            id = json.getString("id"),
            postId = json.getString("postId"),
            body = json.getString("body"),
            authorUserId = json.getString("authorUserId"),
            authorName = json.getString("authorName"),
            createdAt = json.getString("createdAt"),
            updatedAt = json.getString("updatedAt")
        )
    }

    private fun boardPostToJson(item: LocalBoardPost): JSONObject {
        return JSONObject()
            .put("id", item.id)
            .put("categoryId", item.categoryId)
            .put("targetDepartment1", item.targetDepartment1 ?: JSONObject.NULL)
            .put("title", item.title)
            .put("body", item.body)
            .put("startAt", item.startAt)
            .put("endAt", item.endAt)
            .put("allowComments", item.allowComments)
            .put("authorUserId", item.authorUserId)
            .put("authorName", item.authorName)
            .put("createdAt", item.createdAt)
            .put("updatedAt", item.updatedAt)
            .put("comments", JSONArray().apply {
                item.comments.forEach { put(boardCommentToJson(it)) }
            })
    }

    private fun jsonToBoardPost(json: JSONObject): LocalBoardPost {
        val comments = mutableListOf<LocalBoardComment>()
        val commentsArray = json.optJSONArray("comments") ?: JSONArray()
        for (i in 0 until commentsArray.length()) {
            comments += jsonToBoardComment(commentsArray.getJSONObject(i))
        }

        return LocalBoardPost(
            id = json.getString("id"),
            categoryId = json.getString("categoryId"),
            targetDepartment1 = json.optNullableString("targetDepartment1"),
            title = json.getString("title"),
            body = json.getString("body"),
            startAt = json.getString("startAt"),
            endAt = json.getString("endAt"),
            allowComments = json.optBoolean("allowComments", true),
            authorUserId = json.getString("authorUserId"),
            authorName = json.getString("authorName"),
            createdAt = json.getString("createdAt"),
            updatedAt = json.getString("updatedAt"),
            comments = comments
        )
    }

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotBlank() }
    }

    private fun createSeedState(): LocalMockState {
        val users = seedUsers().toMutableList()
        val schedules = seedSchedules().toMutableList()
        val categories = seedBoardCategories().toMutableList()
        val posts = seedBoardPosts().toMutableList()

        return LocalMockState(
            users = users,
            schedules = schedules,
            boardCategories = categories,
            boardPosts = posts
        )
    }

    private fun seedUsers(): List<LocalUser> {
        fun roleFromPosition(position: String): String {
            return when (position) {
                "社長" -> "president"
                "部長" -> "department_manager"
                "課長" -> "section_manager"
                else -> "member"
            }
        }

        val rows = listOf(
            listOf(null, null, "社長", "佐藤", "1", "1"),
            listOf("営業部", null, "部長", "鈴木", "2", "2"),
            listOf("営業部", "営業1課", "課長", "高橋", "3", "3"),
            listOf("営業部", "営業1課", "一般", "田中", "4", "4"),
            listOf("営業部", "営業1課", "一般", "伊藤", "5", "5"),
            listOf("営業部", null, "部長", "渡辺", "6", "6"),
            listOf("営業部", "営業2課", "課長", "山本", "7", "7"),
            listOf("営業部", "営業2課", "一般", "中村", "8", "8"),
            listOf("営業部", "営業2課", "一般", "小林", "9", "9"),
            listOf("総務部", null, "部長", "加藤", "10", "10"),
            listOf("総務部", "総務1課", "課長", "吉田", "11", "11"),
            listOf("総務部", "総務1課", "一般", "山田", "12", "12"),
            listOf("総務部", "総務1課", "一般", "佐々木", "13", "13"),
            listOf("総務部", null, "部長", "山口", "14", "14"),
            listOf("総務部", "総務2課", "課長", "松本", "15", "15"),
            listOf("総務部", "総務2課", "一般", "井上", "16", "16"),
            listOf("総務部", "総務2課", "一般", "木村", "17", "17"),
            listOf("人事部", null, "部長", "林", "18", "18"),
            listOf("人事部", "人事1課", "課長", "斎藤", "19", "19"),
            listOf("人事部", "人事1課", "一般", "清水", "20", "20"),
            listOf("人事部", "人事1課", "一般", "山崎", "21", "21"),
            listOf("人事部", null, "部長", "森", "22", "22"),
            listOf("人事部", "人事2課", "課長", "池田", "23", "23"),
            listOf("人事部", "人事2課", "一般", "橋本", "24", "24"),
            listOf("人事部", "人事2課", "一般", "阿部", "25", "25"),
            listOf("情報システム部", null, "部長", "石川", "26", "26"),
            listOf("情報システム部", "情報システム1課", "課長", "山下", "27", "27"),
            listOf("情報システム部", "情報システム1課", "一般", "中島", "28", "28"),
            listOf("情報システム部", "情報システム1課", "一般", "石井", "29", "29"),
            listOf("情報システム部", null, "部長", "小川", "30", "30"),
            listOf("情報システム部", "情報システム2課", "課長", "前田", "31", "31"),
            listOf("情報システム部", "情報システム2課", "一般", "岡田", "32", "32"),
            listOf("情報システム部", "情報システム2課", "一般", "長谷川", "33", "33")
        )

        return rows.mapIndexed { index, row ->
            val department1 = row[0]
            val department2 = row[1]
            val position = row[2].orEmpty()
            val displayName = row[3].orEmpty()
            val userId = row[4].orEmpty()
            val password = row[5].orEmpty()

            LocalUser(
                id = "emp-${(index + 1).toString().padStart(3, '0')}",
                userId = userId,
                password = password,
                displayName = displayName,
                department1 = department1,
                department2 = department2,
                position = position,
                role = roleFromPosition(position)
            )
        }
    }

    private fun seedSchedules(): List<LocalSchedule> {
        return listOf(
            LocalSchedule(
                id = "sch-001",
                title = "経営会議",
                startAt = "202604150900",
                endAt = "202604151000",
                repeatRule = "毎週",
                location = "大会議室",
                description = "月次業績と重点施策の確認",
                ownerUserId = "emp-001",
                organizerName = "佐藤"
            ),
            LocalSchedule(
                id = "sch-002",
                title = "経営レビュー",
                startAt = "202604151400",
                endAt = "202604151500",
                repeatRule = "なし",
                location = "大会議室",
                description = "月次数字と重点施策の確認",
                ownerUserId = "emp-001",
                organizerName = "佐藤"
            ),
            LocalSchedule(
                id = "sch-003",
                title = "年度重点施策確認",
                startAt = "202605121015",
                endAt = "202605121115",
                repeatRule = "なし",
                location = "大会議室",
                description = "役員向け進捗共有",
                ownerUserId = "emp-001",
                organizerName = "佐藤"
            ),
            LocalSchedule(
                id = "sch-004",
                title = "営業部週次会議",
                startAt = "202604141000",
                endAt = "202604141100",
                repeatRule = "毎週",
                location = "第1会議室",
                description = "営業部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-002",
                organizerName = "鈴木"
            ),
            LocalSchedule(
                id = "sch-005",
                title = "案件棚卸",
                startAt = "202604161515",
                endAt = "202604161615",
                repeatRule = "なし",
                location = "営業部島",
                description = "重点案件の進捗確認",
                ownerUserId = "emp-002",
                organizerName = "鈴木"
            ),
            LocalSchedule(
                id = "sch-006",
                title = "顧客訪問準備",
                startAt = "202605131130",
                endAt = "202605131230",
                repeatRule = "なし",
                location = "オンライン",
                description = "訪問資料の最終チェック",
                ownerUserId = "emp-002",
                organizerName = "鈴木"
            ),
            LocalSchedule(
                id = "sch-007",
                title = "営業1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "営業1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-003",
                organizerName = "高橋"
            ),
            LocalSchedule(
                id = "sch-008",
                title = "見積確認ミーティング",
                startAt = "202604171630",
                endAt = "202604171730",
                repeatRule = "なし",
                location = "オンライン",
                description = "見積条件と提出日確認",
                ownerUserId = "emp-003",
                organizerName = "高橋"
            ),
            LocalSchedule(
                id = "sch-009",
                title = "売上見込更新",
                startAt = "202605141445",
                endAt = "202605141545",
                repeatRule = "なし",
                location = "営業部島",
                description = "月次予測の更新",
                ownerUserId = "emp-003",
                organizerName = "高橋"
            ),
            LocalSchedule(
                id = "sch-010",
                title = "営業1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "営業1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-004",
                organizerName = "田中"
            ),
            LocalSchedule(
                id = "sch-011",
                title = "商談準備",
                startAt = "202604181345",
                endAt = "202604181430",
                repeatRule = "なし",
                location = "オンライン",
                description = "ヒアリング内容の整理",
                ownerUserId = "emp-004",
                organizerName = "田中"
            ),
            LocalSchedule(
                id = "sch-012",
                title = "競合情報共有",
                startAt = "202605151500",
                endAt = "202605151545",
                repeatRule = "なし",
                location = "オンライン",
                description = "大型案件の比較情報共有",
                ownerUserId = "emp-004",
                organizerName = "田中"
            ),
            LocalSchedule(
                id = "sch-013",
                title = "営業1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "営業1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-005",
                organizerName = "伊藤"
            ),
            LocalSchedule(
                id = "sch-014",
                title = "顧客提案レビュー",
                startAt = "202604191300",
                endAt = "202604191345",
                repeatRule = "なし",
                location = "応接室A",
                description = "提案書の確認と役割分担",
                ownerUserId = "emp-005",
                organizerName = "伊藤"
            ),
            LocalSchedule(
                id = "sch-015",
                title = "案件フォロー会",
                startAt = "202605161515",
                endAt = "202605161600",
                repeatRule = "なし",
                location = "第1会議室",
                description = "月初案件の状況確認",
                ownerUserId = "emp-005",
                organizerName = "伊藤"
            ),
            LocalSchedule(
                id = "sch-016",
                title = "営業部週次会議",
                startAt = "202604141000",
                endAt = "202604141100",
                repeatRule = "毎週",
                location = "第1会議室",
                description = "営業部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-006",
                organizerName = "渡辺"
            ),
            LocalSchedule(
                id = "sch-017",
                title = "案件棚卸",
                startAt = "202604201515",
                endAt = "202604201615",
                repeatRule = "なし",
                location = "営業部島",
                description = "重点案件の進捗確認",
                ownerUserId = "emp-006",
                organizerName = "渡辺"
            ),
            LocalSchedule(
                id = "sch-018",
                title = "顧客訪問準備",
                startAt = "202605171130",
                endAt = "202605171230",
                repeatRule = "なし",
                location = "オンライン",
                description = "訪問資料の最終チェック",
                ownerUserId = "emp-006",
                organizerName = "渡辺"
            ),
            LocalSchedule(
                id = "sch-019",
                title = "営業2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "営業2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-007",
                organizerName = "山本"
            ),
            LocalSchedule(
                id = "sch-020",
                title = "見積確認ミーティング",
                startAt = "202604211630",
                endAt = "202604211730",
                repeatRule = "なし",
                location = "オンライン",
                description = "見積条件と提出日確認",
                ownerUserId = "emp-007",
                organizerName = "山本"
            ),
            LocalSchedule(
                id = "sch-021",
                title = "売上見込更新",
                startAt = "202605181445",
                endAt = "202605181545",
                repeatRule = "なし",
                location = "営業部島",
                description = "月次予測の更新",
                ownerUserId = "emp-007",
                organizerName = "山本"
            ),
            LocalSchedule(
                id = "sch-022",
                title = "営業2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "営業2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-008",
                organizerName = "中村"
            ),
            LocalSchedule(
                id = "sch-023",
                title = "商談準備",
                startAt = "202604221345",
                endAt = "202604221430",
                repeatRule = "なし",
                location = "オンライン",
                description = "ヒアリング内容の整理",
                ownerUserId = "emp-008",
                organizerName = "中村"
            ),
            LocalSchedule(
                id = "sch-024",
                title = "競合情報共有",
                startAt = "202605191500",
                endAt = "202605191545",
                repeatRule = "なし",
                location = "オンライン",
                description = "大型案件の比較情報共有",
                ownerUserId = "emp-008",
                organizerName = "中村"
            ),
            LocalSchedule(
                id = "sch-025",
                title = "営業2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "営業2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-009",
                organizerName = "小林"
            ),
            LocalSchedule(
                id = "sch-026",
                title = "顧客提案レビュー",
                startAt = "202604231300",
                endAt = "202604231345",
                repeatRule = "なし",
                location = "応接室A",
                description = "提案書の確認と役割分担",
                ownerUserId = "emp-009",
                organizerName = "小林"
            ),
            LocalSchedule(
                id = "sch-027",
                title = "案件フォロー会",
                startAt = "202605201515",
                endAt = "202605201600",
                repeatRule = "なし",
                location = "第1会議室",
                description = "月初案件の状況確認",
                ownerUserId = "emp-009",
                organizerName = "小林"
            ),
            LocalSchedule(
                id = "sch-028",
                title = "総務部週次共有",
                startAt = "202604141030",
                endAt = "202604141130",
                repeatRule = "毎週",
                location = "総務部会議スペース",
                description = "総務部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-010",
                organizerName = "加藤"
            ),
            LocalSchedule(
                id = "sch-029",
                title = "来客対応準備",
                startAt = "202604241515",
                endAt = "202604241615",
                repeatRule = "なし",
                location = "受付前",
                description = "来週来客の動線確認",
                ownerUserId = "emp-010",
                organizerName = "加藤"
            ),
            LocalSchedule(
                id = "sch-030",
                title = "受付フロー見直し",
                startAt = "202605211130",
                endAt = "202605211230",
                repeatRule = "なし",
                location = "総務部会議スペース",
                description = "来客案内フローの改善",
                ownerUserId = "emp-010",
                organizerName = "加藤"
            ),
            LocalSchedule(
                id = "sch-031",
                title = "総務1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "総務1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-011",
                organizerName = "吉田"
            ),
            LocalSchedule(
                id = "sch-032",
                title = "会議室レイアウト変更",
                startAt = "202604251630",
                endAt = "202604251730",
                repeatRule = "なし",
                location = "大会議室",
                description = "説明会向けの配置調整",
                ownerUserId = "emp-011",
                organizerName = "吉田"
            ),
            LocalSchedule(
                id = "sch-033",
                title = "社内イベント準備",
                startAt = "202605221445",
                endAt = "202605221545",
                repeatRule = "なし",
                location = "大会議室",
                description = "表彰会運営の準備",
                ownerUserId = "emp-011",
                organizerName = "吉田"
            ),
            LocalSchedule(
                id = "sch-034",
                title = "総務1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "総務1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-012",
                organizerName = "山田"
            ),
            LocalSchedule(
                id = "sch-035",
                title = "社内便整理",
                startAt = "202604261345",
                endAt = "202604261430",
                repeatRule = "なし",
                location = "総務作業スペース",
                description = "社内配送物の仕分け",
                ownerUserId = "emp-012",
                organizerName = "山田"
            ),
            LocalSchedule(
                id = "sch-036",
                title = "契約書保管整理",
                startAt = "202605231500",
                endAt = "202605231545",
                repeatRule = "なし",
                location = "書庫",
                description = "書類の更新確認",
                ownerUserId = "emp-012",
                organizerName = "山田"
            ),
            LocalSchedule(
                id = "sch-037",
                title = "総務1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "総務1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-013",
                organizerName = "佐々木"
            ),
            LocalSchedule(
                id = "sch-038",
                title = "備品発注確認",
                startAt = "202604271300",
                endAt = "202604271345",
                repeatRule = "なし",
                location = "総務カウンター",
                description = "月次の在庫確認",
                ownerUserId = "emp-013",
                organizerName = "佐々木"
            ),
            LocalSchedule(
                id = "sch-039",
                title = "防災備蓄確認",
                startAt = "202605241515",
                endAt = "202605241600",
                repeatRule = "なし",
                location = "倉庫",
                description = "備蓄品の在庫確認",
                ownerUserId = "emp-013",
                organizerName = "佐々木"
            ),
            LocalSchedule(
                id = "sch-040",
                title = "総務部週次共有",
                startAt = "202604141030",
                endAt = "202604141130",
                repeatRule = "毎週",
                location = "総務部会議スペース",
                description = "総務部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-014",
                organizerName = "山口"
            ),
            LocalSchedule(
                id = "sch-041",
                title = "来客対応準備",
                startAt = "202604281515",
                endAt = "202604281615",
                repeatRule = "なし",
                location = "受付前",
                description = "来週来客の動線確認",
                ownerUserId = "emp-014",
                organizerName = "山口"
            ),
            LocalSchedule(
                id = "sch-042",
                title = "受付フロー見直し",
                startAt = "202605251130",
                endAt = "202605251230",
                repeatRule = "なし",
                location = "総務部会議スペース",
                description = "来客案内フローの改善",
                ownerUserId = "emp-014",
                organizerName = "山口"
            ),
            LocalSchedule(
                id = "sch-043",
                title = "総務2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "総務2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-015",
                organizerName = "松本"
            ),
            LocalSchedule(
                id = "sch-044",
                title = "会議室レイアウト変更",
                startAt = "202604151630",
                endAt = "202604151730",
                repeatRule = "なし",
                location = "大会議室",
                description = "説明会向けの配置調整",
                ownerUserId = "emp-015",
                organizerName = "松本"
            ),
            LocalSchedule(
                id = "sch-045",
                title = "社内イベント準備",
                startAt = "202605261445",
                endAt = "202605261545",
                repeatRule = "なし",
                location = "大会議室",
                description = "表彰会運営の準備",
                ownerUserId = "emp-015",
                organizerName = "松本"
            ),
            LocalSchedule(
                id = "sch-046",
                title = "総務2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "総務2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-016",
                organizerName = "井上"
            ),
            LocalSchedule(
                id = "sch-047",
                title = "社内便整理",
                startAt = "202604161345",
                endAt = "202604161430",
                repeatRule = "なし",
                location = "総務作業スペース",
                description = "社内配送物の仕分け",
                ownerUserId = "emp-016",
                organizerName = "井上"
            ),
            LocalSchedule(
                id = "sch-048",
                title = "契約書保管整理",
                startAt = "202605121500",
                endAt = "202605121545",
                repeatRule = "なし",
                location = "書庫",
                description = "書類の更新確認",
                ownerUserId = "emp-016",
                organizerName = "井上"
            ),
            LocalSchedule(
                id = "sch-049",
                title = "総務2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "総務2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-017",
                organizerName = "木村"
            ),
            LocalSchedule(
                id = "sch-050",
                title = "備品発注確認",
                startAt = "202604171300",
                endAt = "202604171345",
                repeatRule = "なし",
                location = "総務カウンター",
                description = "月次の在庫確認",
                ownerUserId = "emp-017",
                organizerName = "木村"
            ),
            LocalSchedule(
                id = "sch-051",
                title = "防災備蓄確認",
                startAt = "202605131515",
                endAt = "202605131600",
                repeatRule = "なし",
                location = "倉庫",
                description = "備蓄品の在庫確認",
                ownerUserId = "emp-017",
                organizerName = "木村"
            ),
            LocalSchedule(
                id = "sch-052",
                title = "人事部週次共有",
                startAt = "202604141100",
                endAt = "202604141200",
                repeatRule = "毎週",
                location = "人事会議室",
                description = "人事部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-018",
                organizerName = "林"
            ),
            LocalSchedule(
                id = "sch-053",
                title = "研修準備",
                startAt = "202604181515",
                endAt = "202604181615",
                repeatRule = "なし",
                location = "研修室A",
                description = "配布物と座席表の確認",
                ownerUserId = "emp-018",
                organizerName = "林"
            ),
            LocalSchedule(
                id = "sch-054",
                title = "説明会振り返り",
                startAt = "202605141130",
                endAt = "202605141230",
                repeatRule = "なし",
                location = "研修室A",
                description = "会社説明会の改善点整理",
                ownerUserId = "emp-018",
                organizerName = "林"
            ),
            LocalSchedule(
                id = "sch-055",
                title = "人事1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "人事1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-019",
                organizerName = "斎藤"
            ),
            LocalSchedule(
                id = "sch-056",
                title = "候補者対応確認",
                startAt = "202604191630",
                endAt = "202604191730",
                repeatRule = "なし",
                location = "オンライン",
                description = "選考連絡の進捗確認",
                ownerUserId = "emp-019",
                organizerName = "斎藤"
            ),
            LocalSchedule(
                id = "sch-057",
                title = "評価面談準備",
                startAt = "202605151445",
                endAt = "202605151545",
                repeatRule = "なし",
                location = "人事会議室",
                description = "面談シートと日程確認",
                ownerUserId = "emp-019",
                organizerName = "斎藤"
            ),
            LocalSchedule(
                id = "sch-058",
                title = "人事1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "人事1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-020",
                organizerName = "清水"
            ),
            LocalSchedule(
                id = "sch-059",
                title = "制度改定レビュー",
                startAt = "202604201345",
                endAt = "202604201430",
                repeatRule = "なし",
                location = "人事会議室",
                description = "就業規則改定点の確認",
                ownerUserId = "emp-020",
                organizerName = "清水"
            ),
            LocalSchedule(
                id = "sch-060",
                title = "求人票更新",
                startAt = "202605161500",
                endAt = "202605161545",
                repeatRule = "なし",
                location = "オンライン",
                description = "媒体掲載内容の見直し",
                ownerUserId = "emp-020",
                organizerName = "清水"
            ),
            LocalSchedule(
                id = "sch-061",
                title = "人事1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "人事1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-021",
                organizerName = "山崎"
            ),
            LocalSchedule(
                id = "sch-062",
                title = "面接評価すり合わせ",
                startAt = "202604211300",
                endAt = "202604211345",
                repeatRule = "なし",
                location = "人事会議室",
                description = "面接結果の共有",
                ownerUserId = "emp-021",
                organizerName = "山崎"
            ),
            LocalSchedule(
                id = "sch-063",
                title = "内定者フォロー会",
                startAt = "202605171515",
                endAt = "202605171600",
                repeatRule = "なし",
                location = "人事会議室",
                description = "内定者連絡状況の確認",
                ownerUserId = "emp-021",
                organizerName = "山崎"
            ),
            LocalSchedule(
                id = "sch-064",
                title = "人事部週次共有",
                startAt = "202604141100",
                endAt = "202604141200",
                repeatRule = "毎週",
                location = "人事会議室",
                description = "人事部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-022",
                organizerName = "森"
            ),
            LocalSchedule(
                id = "sch-065",
                title = "研修準備",
                startAt = "202604221515",
                endAt = "202604221615",
                repeatRule = "なし",
                location = "研修室A",
                description = "配布物と座席表の確認",
                ownerUserId = "emp-022",
                organizerName = "森"
            ),
            LocalSchedule(
                id = "sch-066",
                title = "説明会振り返り",
                startAt = "202605181130",
                endAt = "202605181230",
                repeatRule = "なし",
                location = "研修室A",
                description = "会社説明会の改善点整理",
                ownerUserId = "emp-022",
                organizerName = "森"
            ),
            LocalSchedule(
                id = "sch-067",
                title = "人事2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "人事2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-023",
                organizerName = "池田"
            ),
            LocalSchedule(
                id = "sch-068",
                title = "候補者対応確認",
                startAt = "202604231630",
                endAt = "202604231730",
                repeatRule = "なし",
                location = "オンライン",
                description = "選考連絡の進捗確認",
                ownerUserId = "emp-023",
                organizerName = "池田"
            ),
            LocalSchedule(
                id = "sch-069",
                title = "評価面談準備",
                startAt = "202605191445",
                endAt = "202605191545",
                repeatRule = "なし",
                location = "人事会議室",
                description = "面談シートと日程確認",
                ownerUserId = "emp-023",
                organizerName = "池田"
            ),
            LocalSchedule(
                id = "sch-070",
                title = "人事2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "人事2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-024",
                organizerName = "橋本"
            ),
            LocalSchedule(
                id = "sch-071",
                title = "制度改定レビュー",
                startAt = "202604241345",
                endAt = "202604241430",
                repeatRule = "なし",
                location = "人事会議室",
                description = "就業規則改定点の確認",
                ownerUserId = "emp-024",
                organizerName = "橋本"
            ),
            LocalSchedule(
                id = "sch-072",
                title = "求人票更新",
                startAt = "202605201500",
                endAt = "202605201545",
                repeatRule = "なし",
                location = "オンライン",
                description = "媒体掲載内容の見直し",
                ownerUserId = "emp-024",
                organizerName = "橋本"
            ),
            LocalSchedule(
                id = "sch-073",
                title = "人事2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "人事2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-025",
                organizerName = "阿部"
            ),
            LocalSchedule(
                id = "sch-074",
                title = "面接評価すり合わせ",
                startAt = "202604251300",
                endAt = "202604251345",
                repeatRule = "なし",
                location = "人事会議室",
                description = "面接結果の共有",
                ownerUserId = "emp-025",
                organizerName = "阿部"
            ),
            LocalSchedule(
                id = "sch-075",
                title = "内定者フォロー会",
                startAt = "202605211515",
                endAt = "202605211600",
                repeatRule = "なし",
                location = "人事会議室",
                description = "内定者連絡状況の確認",
                ownerUserId = "emp-025",
                organizerName = "阿部"
            ),
            LocalSchedule(
                id = "sch-076",
                title = "情シス週次定例",
                startAt = "202604141130",
                endAt = "202604141230",
                repeatRule = "毎週",
                location = "情報システム会議室",
                description = "情報システム部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-026",
                organizerName = "石川"
            ),
            LocalSchedule(
                id = "sch-077",
                title = "リリース判定会",
                startAt = "202604261515",
                endAt = "202604261615",
                repeatRule = "なし",
                location = "オンライン",
                description = "今週リリースの最終確認",
                ownerUserId = "emp-026",
                organizerName = "石川"
            ),
            LocalSchedule(
                id = "sch-078",
                title = "SSO設定レビュー",
                startAt = "202605221130",
                endAt = "202605221230",
                repeatRule = "なし",
                location = "オンライン",
                description = "認証設定変更の確認",
                ownerUserId = "emp-026",
                organizerName = "石川"
            ),
            LocalSchedule(
                id = "sch-079",
                title = "情シス1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "情報システム1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-027",
                organizerName = "山下"
            ),
            LocalSchedule(
                id = "sch-080",
                title = "端末棚卸確認",
                startAt = "202604271630",
                endAt = "202604271730",
                repeatRule = "なし",
                location = "倉庫前",
                description = "資産台帳の差異確認",
                ownerUserId = "emp-027",
                organizerName = "山下"
            ),
            LocalSchedule(
                id = "sch-081",
                title = "端末展開準備",
                startAt = "202605231445",
                endAt = "202605231545",
                repeatRule = "なし",
                location = "作業室",
                description = "キッティング対象の洗い出し",
                ownerUserId = "emp-027",
                organizerName = "山下"
            ),
            LocalSchedule(
                id = "sch-082",
                title = "情シス1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "情報システム1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-028",
                organizerName = "中島"
            ),
            LocalSchedule(
                id = "sch-083",
                title = "運用改善レビュー",
                startAt = "202604281345",
                endAt = "202604281430",
                repeatRule = "なし",
                location = "第2会議室",
                description = "申請導線とFAQ改善の検討",
                ownerUserId = "emp-028",
                organizerName = "中島"
            ),
            LocalSchedule(
                id = "sch-084",
                title = "監視アラート見直し",
                startAt = "202605241500",
                endAt = "202605241545",
                repeatRule = "なし",
                location = "情報システム会議室",
                description = "通知閾値の再整理",
                ownerUserId = "emp-028",
                organizerName = "中島"
            ),
            LocalSchedule(
                id = "sch-085",
                title = "情シス1課朝会",
                startAt = "202604140900",
                endAt = "202604140915",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "情報システム1課の案件共有と当日の連絡事項",
                ownerUserId = "emp-029",
                organizerName = "石井"
            ),
            LocalSchedule(
                id = "sch-086",
                title = "障害対応振り返り",
                startAt = "202604151300",
                endAt = "202604151345",
                repeatRule = "なし",
                location = "情報システム会議室",
                description = "対応経緯と改善案の整理",
                ownerUserId = "emp-029",
                organizerName = "石井"
            ),
            LocalSchedule(
                id = "sch-087",
                title = "月次保守計画確認",
                startAt = "202605251515",
                endAt = "202605251600",
                repeatRule = "なし",
                location = "情報システム会議室",
                description = "定例保守作業の段取り確認",
                ownerUserId = "emp-029",
                organizerName = "石井"
            ),
            LocalSchedule(
                id = "sch-088",
                title = "情シス週次定例",
                startAt = "202604141130",
                endAt = "202604141230",
                repeatRule = "毎週",
                location = "情報システム会議室",
                description = "情報システム部の重点テーマと連絡事項を確認",
                ownerUserId = "emp-030",
                organizerName = "小川"
            ),
            LocalSchedule(
                id = "sch-089",
                title = "リリース判定会",
                startAt = "202604161515",
                endAt = "202604161615",
                repeatRule = "なし",
                location = "オンライン",
                description = "今週リリースの最終確認",
                ownerUserId = "emp-030",
                organizerName = "小川"
            ),
            LocalSchedule(
                id = "sch-090",
                title = "SSO設定レビュー",
                startAt = "202605261130",
                endAt = "202605261230",
                repeatRule = "なし",
                location = "オンライン",
                description = "認証設定変更の確認",
                ownerUserId = "emp-030",
                organizerName = "小川"
            ),
            LocalSchedule(
                id = "sch-091",
                title = "情シス2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "情報システム2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-031",
                organizerName = "前田"
            ),
            LocalSchedule(
                id = "sch-092",
                title = "端末棚卸確認",
                startAt = "202604171630",
                endAt = "202604171730",
                repeatRule = "なし",
                location = "倉庫前",
                description = "資産台帳の差異確認",
                ownerUserId = "emp-031",
                organizerName = "前田"
            ),
            LocalSchedule(
                id = "sch-093",
                title = "端末展開準備",
                startAt = "202605121445",
                endAt = "202605121545",
                repeatRule = "なし",
                location = "作業室",
                description = "キッティング対象の洗い出し",
                ownerUserId = "emp-031",
                organizerName = "前田"
            ),
            LocalSchedule(
                id = "sch-094",
                title = "情シス2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "情報システム2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-032",
                organizerName = "岡田"
            ),
            LocalSchedule(
                id = "sch-095",
                title = "運用改善レビュー",
                startAt = "202604181345",
                endAt = "202604181430",
                repeatRule = "なし",
                location = "第2会議室",
                description = "申請導線とFAQ改善の検討",
                ownerUserId = "emp-032",
                organizerName = "岡田"
            ),
            LocalSchedule(
                id = "sch-096",
                title = "監視アラート見直し",
                startAt = "202605131500",
                endAt = "202605131545",
                repeatRule = "なし",
                location = "情報システム会議室",
                description = "通知閾値の再整理",
                ownerUserId = "emp-032",
                organizerName = "岡田"
            ),
            LocalSchedule(
                id = "sch-097",
                title = "情シス2課朝会",
                startAt = "202604140915",
                endAt = "202604140930",
                repeatRule = "営業日（月〜金）",
                location = "オンライン",
                description = "情報システム2課の案件共有と当日の連絡事項",
                ownerUserId = "emp-033",
                organizerName = "長谷川"
            ),
            LocalSchedule(
                id = "sch-098",
                title = "障害対応振り返り",
                startAt = "202604191300",
                endAt = "202604191345",
                repeatRule = "なし",
                location = "情報システム会議室",
                description = "対応経緯と改善案の整理",
                ownerUserId = "emp-033",
                organizerName = "長谷川"
            ),
            LocalSchedule(
                id = "sch-099",
                title = "月次保守計画確認",
                startAt = "202605141515",
                endAt = "202605141600",
                repeatRule = "なし",
                location = "情報システム会議室",
                description = "定例保守作業の段取り確認",
                ownerUserId = "emp-033",
                organizerName = "長谷川"
            )
        )
    }

    private fun seedBoardCategories(): List<LocalBoardCategory> {
        return listOf(
            LocalBoardCategory(
                id = "cat-company",
                name = "全社連絡",
                description = "全社員向けのお知らせを掲載します。"
            ),
            LocalBoardCategory(
                id = "cat-department",
                name = "部門連絡",
                description = "部門内のお知らせを共有します。"
            ),
            LocalBoardCategory(
                id = "cat-free",
                name = "社内共有",
                description = "自由に情報共有できる掲示板です。"
            )
        )
    }

    private fun seedBoardPosts(): List<LocalBoardPost> {
        fun comment(
            id: String,
            postId: String,
            body: String,
            authorUserId: String,
            authorName: String,
            createdAt: String,
            updatedAt: String = createdAt
        ) = LocalBoardComment(
            id = id,
            postId = postId,
            body = body,
            authorUserId = authorUserId,
            authorName = authorName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        fun post(
            id: String,
            categoryId: String,
            targetDepartment1: String?,
            title: String,
            body: String,
            startAt: String,
            endAt: String,
            allowComments: Boolean,
            authorUserId: String,
            authorName: String,
            createdAt: String,
            updatedAt: String = createdAt,
            comments: List<LocalBoardComment> = emptyList()
        ) = LocalBoardPost(
            id = id,
            categoryId = categoryId,
            targetDepartment1 = targetDepartment1,
            title = title,
            body = body,
            startAt = startAt,
            endAt = endAt,
            allowComments = allowComments,
            authorUserId = authorUserId,
            authorName = authorName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            comments = comments.toMutableList()
        )

        return listOf(
            post(
                id = "post-001",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "2026年度方針説明会のご案内",
                body = "4月22日（水）に年度方針説明会を実施します。\n各部門は参加者を4月18日までに取りまとめてください。\n当日は録画配信も予定しています。",
                startAt = "202604130900",
                endAt = "202604302359",
                allowComments = true,
                authorUserId = "emp-001",
                authorName = "佐藤",
                createdAt = "202604130900",
                comments = listOf(
                    comment("cmt-001", "post-001", "営業部でも参加者を取りまとめます。4/17までに一覧を共有します。", "emp-002", "鈴木", "202604131010"),
                    comment("cmt-002", "post-001", "採用候補者向け説明資料にも流用したいので、録画可否を確認したいです。", "emp-018", "林", "202604131018"),
                    comment("cmt-003", "post-001", "配信機材の準備は情報システム部で対応します。", "emp-026", "石川", "202604131026"),
                    comment("cmt-004", "post-001", "総務部で当日の会場設営担当を整理しておきます。", "emp-010", "加藤", "202604131033")
                )
            ),
            post(
                id = "post-002",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "ゴールデンウィーク前の各種申請締切について",
                body = "交通費・備品・押印申請は4月24日（金）17:00を締切とします。\n休暇申請は所属長承認まで完了してください。\n不明点は総務部までお願いします。",
                startAt = "202604131030",
                endAt = "202604252359",
                allowComments = true,
                authorUserId = "emp-010",
                authorName = "加藤",
                createdAt = "202604131030",
                comments = listOf(
                    comment("cmt-005", "post-002", "総務1課で押印申請の回収を進めます。4/23夕方に最終確認します。", "emp-011", "吉田", "202604131045"),
                    comment("cmt-006", "post-002", "入社書類の最終締切も同日扱いで問題ないか、人事部側で確認します。", "emp-020", "清水", "202604131053"),
                    comment("cmt-007", "post-002", "営業部は外出が多いので、週明けに再周知しておきます。", "emp-006", "渡辺", "202604131059")
                )
            ),
            post(
                id = "post-003",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "4/18 社内ネットワーク保守のお知らせ",
                body = "4月18日（土）9:00〜12:00で社内ネットワーク機器の保守を行います。\nこの時間帯はVPNとファイルサーバーが断続的に利用できない可能性があります。\n緊急時は情報システム部へ連絡してください。",
                startAt = "202604131100",
                endAt = "202604202359",
                allowComments = false,
                authorUserId = "emp-026",
                authorName = "石川",
                createdAt = "202604131100"
            ),
            post(
                id = "post-004",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "新卒採用会社説明会の運営協力募集",
                body = "4月21日（火）の会社説明会で受付・会場案内・座談会対応をお願いしたいです。\n協力可能な方はコメントで担当可能時間を書いてください。\n営業部・情報システム部からの協力も歓迎します。",
                startAt = "202604131130",
                endAt = "202604212359",
                allowComments = true,
                authorUserId = "emp-018",
                authorName = "林",
                createdAt = "202604131130",
                comments = listOf(
                    comment("cmt-008", "post-004", "受付担当可能です。午前枠を希望します。", "emp-020", "清水", "202604131145"),
                    comment("cmt-009", "post-004", "学生向け会社紹介の原稿を更新しておきます。", "emp-021", "山崎", "202604131152"),
                    comment("cmt-010", "post-004", "ありがとうございます。人事部主導で進め、必要に応じて各部門へ依頼してください。", "emp-001", "佐藤", "202604131205"),
                    comment("cmt-011", "post-004", "営業部から座談会対応で1名出せます。午後枠で調整します。", "emp-002", "鈴木", "202604131212"),
                    comment("cmt-012", "post-004", "会社説明会の投影資料は前回版をベースに更新しておきます。", "emp-026", "石川", "202604131219")
                )
            ),
            post(
                id = "post-005",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "月次全社会議の資料掲載",
                body = "4月分の全社会議資料を共有フォルダに掲載しました。\n部門別トピックの確認をお願いします。\n質問がある場合はコメント欄に記載してください。",
                startAt = "202604150830",
                endAt = "202604302359",
                allowComments = true,
                authorUserId = "emp-001",
                authorName = "佐藤",
                createdAt = "202604150830"
            ),
            post(
                id = "post-006",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "来客エリア改装に伴う導線変更",
                body = "4月25日〜4月27日の間、1階来客エリアの一部改装を行います。\n受付場所と会議室への導線が一時的に変わりますので、来客対応予定がある方は事前にご確認ください。",
                startAt = "202604151015",
                endAt = "202604282359",
                allowComments = true,
                authorUserId = "emp-014",
                authorName = "山口",
                createdAt = "202604151015"
            ),
            post(
                id = "post-007",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "健康診断日程の調整について",
                body = "今年度の定期健康診断を5月中旬から順次実施します。\n日程調整フォームを配布したので、4月30日までに希望日を入力してください。\n未提出者には個別にご案内します。",
                startAt = "202604160900",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-010",
                authorName = "加藤",
                createdAt = "202604160900"
            ),
            post(
                id = "post-008",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "社内ポータル改善アンケートのお願い",
                body = "社内ポータルの使い勝手改善に向けてアンケートを実施します。\n申請画面、掲示板、スケジュール機能について日頃感じていることを教えてください。\n所要時間は5分程度です。",
                startAt = "202604161130",
                endAt = "202605102359",
                allowComments = true,
                authorUserId = "emp-030",
                authorName = "小川",
                createdAt = "202604161130"
            ),
            post(
                id = "post-009",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "5月連休中の緊急連絡体制",
                body = "5月連休期間中の緊急連絡先一覧を更新しました。\n設備障害、システム障害、採用イベント対応に分けて連絡フローを記載しています。\n各管理職はメンバーへ展開をお願いします。",
                startAt = "202604171000",
                endAt = "202605062359",
                allowComments = false,
                authorUserId = "emp-001",
                authorName = "佐藤",
                createdAt = "202604171000"
            ),
            post(
                id = "post-010",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "名刺発注ルール変更のお知らせ",
                body = "5月から名刺発注フローを一部変更します。\n申請時に英字表記の確認欄を追加しました。\n新入社員や異動者がいる部門は、事前確認をお願いします。",
                startAt = "202604171140",
                endAt = "202605152359",
                allowComments = true,
                authorUserId = "emp-011",
                authorName = "吉田",
                createdAt = "202604171140"
            ),
            post(
                id = "post-011",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "採用広報用の写真撮影協力のお願い",
                body = "採用広報ページ更新にあたり、執務風景や打ち合わせ風景の写真を撮影します。\n4月下旬から5月上旬にかけて各部門を順番に回ります。\n掲載が難しい場合は事前に人事部へお知らせください。",
                startAt = "202604181000",
                endAt = "202605202359",
                allowComments = true,
                authorUserId = "emp-018",
                authorName = "林",
                createdAt = "202604181000"
            ),
            post(
                id = "post-012",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "セキュリティ研修受講のお願い",
                body = "全社員向けのeラーニング型セキュリティ研修を配信しました。\n受講期限は5月15日です。\n未受講者には部門管理職へ進捗共有します。",
                startAt = "202604181130",
                endAt = "202605152359",
                allowComments = true,
                authorUserId = "emp-026",
                authorName = "石川",
                createdAt = "202604181130"
            ),
            post(
                id = "post-013",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "上期キックオフ準備メモ",
                body = "上期キックオフ当日の進行案を共有します。\n各部門長は持ち時間の確認と資料提出期限の再確認をお願いします。\n司会進行は総務部と人事部で分担します。",
                startAt = "202604191000",
                endAt = "202605022359",
                allowComments = true,
                authorUserId = "emp-001",
                authorName = "佐藤",
                createdAt = "202604191000",
                comments = listOf(
                    comment("cmt-041", "post-013", "営業部の資料提出は4/24までに揃える想定で進めます。", "emp-002", "鈴木", "202604191015"),
                    comment("cmt-042", "post-013", "司会進行の台本たたき台を総務で作成します。", "emp-010", "加藤", "202604191023"),
                    comment("cmt-043", "post-013", "採用広報向けに写真撮影導線も加味しておきます。", "emp-018", "林", "202604191031")
                )
            ),
            post(
                id = "post-014",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "会議室予約ルール見直しについて",
                body = "利用率の高い会議室について、予約ルールを見直します。\n長時間仮押さえの抑制と、終了後の即時解放を徹底したいと考えています。\n改善案があればコメントください。",
                startAt = "202604191130",
                endAt = "202605102359",
                allowComments = true,
                authorUserId = "emp-014",
                authorName = "山口",
                createdAt = "202604191130",
                comments = listOf(
                    comment("cmt-044", "post-014", "30分以上の仮押さえが続く会議室は見直したいです。", "emp-011", "吉田", "202604191145"),
                    comment("cmt-045", "post-014", "営業は訪問前後で予定変更が多いので、開始15分前の自動確認があると助かります。", "emp-003", "高橋", "202604191153"),
                    comment("cmt-046", "post-014", "使い終わった会議室をすぐ解放できる導線は賛成です。", "emp-028", "中島", "202604191200")
                )
            ),
            post(
                id = "post-015",
                categoryId = "cat-company",
                targetDepartment1 = null,
                title = "VPN利用手順 更新版のお知らせ",
                body = "在宅勤務向けのVPN利用手順を更新しました。\n初回接続時の注意点と、証明書更新時の画面例を追加しています。\n新しい手順書はポータルの資料庫に掲載しています。",
                startAt = "202604201000",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-030",
                authorName = "小川",
                createdAt = "202604201000",
                comments = listOf(
                    comment("cmt-047", "post-015", "新入社員向けに初回接続マニュアルも別紙でまとめます。", "emp-027", "山下", "202604201015"),
                    comment("cmt-048", "post-015", "営業部でよくある問い合わせを整理して、FAQ候補を送ります。", "emp-007", "山本", "202604201023"),
                    comment("cmt-049", "post-015", "在宅勤務申請の案内からもリンクしておくと親切そうです。", "emp-019", "斎藤", "202604201031")
                )
            ),
            post(
                id = "post-016",
                categoryId = "cat-department",
                targetDepartment1 = "営業部",
                title = "営業部 4月重点案件共有",
                body = "今月はA商事・東和物流・北斗商事の3案件を重点管理します。\n提案書更新版は毎週木曜18:00までに共有フォルダへ保存してください。\n失注懸念のある案件は早めに相談してください。",
                startAt = "202604131200",
                endAt = "202604242359",
                allowComments = true,
                authorUserId = "emp-002",
                authorName = "鈴木",
                createdAt = "202604131200",
                comments = listOf(
                    comment("cmt-050", "post-016", "A商事の状況は本日夕方に更新します。", "emp-004", "田中", "202604131620"),
                    comment("cmt-051", "post-016", "東和物流の競合情報は営業2課からも共有します。", "emp-007", "山本", "202604131628"),
                    comment("cmt-052", "post-016", "失注懸念案件は木曜午前までに一覧化しておきます。", "emp-006", "渡辺", "202604131636")
                )
            ),
            post(
                id = "post-017",
                categoryId = "cat-department",
                targetDepartment1 = "営業部",
                title = "営業1課 ロープレ会実施のお知らせ",
                body = "4月17日（金）17:30から営業1課で商談ロープレ会を行います。\n新規提案のトークスクリプトを確認したいので、各自1パターン準備してください。\n営業2課の見学参加も歓迎します。",
                startAt = "202604131230",
                endAt = "202604192359",
                allowComments = true,
                authorUserId = "emp-003",
                authorName = "高橋",
                createdAt = "202604131230",
                comments = listOf(
                    comment("cmt-053", "post-017", "営業2課から2名見学予定です。", "emp-008", "中村", "202604131640"),
                    comment("cmt-054", "post-017", "ロープレの想定業界を共有してもらえると準備しやすいです。", "emp-005", "伊藤", "202604131648"),
                    comment("cmt-055", "post-017", "製造業向けと物流向けの2パターンで進める予定です。", "emp-003", "高橋", "202604131655")
                )
            ),
            post(
                id = "post-018",
                categoryId = "cat-department",
                targetDepartment1 = "営業部",
                title = "営業2課 失注案件共有会",
                body = "4月後半の改善に向けて、直近失注案件の振り返りを行います。\n案件背景、競合状況、次回改善案を簡単にまとめて持参してください。\n営業1課も任意参加歓迎です。",
                startAt = "202604151300",
                endAt = "202604262359",
                allowComments = true,
                authorUserId = "emp-007",
                authorName = "山本",
                createdAt = "202604151300",
                comments = listOf(
                    comment("cmt-056", "post-018", "営業1課からも2件ほど共有したい案件があります。", "emp-003", "高橋", "202604151315"),
                    comment("cmt-057", "post-018", "価格条件で競合に負けた案件の振り返りを持参します。", "emp-009", "小林", "202604151323"),
                    comment("cmt-058", "post-018", "事例共有の時間を少し長めに取りたいです。", "emp-006", "渡辺", "202604151331")
                )
            ),
            post(
                id = "post-019",
                categoryId = "cat-department",
                targetDepartment1 = "営業部",
                title = "提案テンプレート更新版を掲載しました",
                body = "製造業向け提案テンプレートを更新しました。\n導入効果の記載例と、比較表のサンプルを追加しています。\n次回提案から順次利用をお願いします。",
                startAt = "202604161000",
                endAt = "202605152359",
                allowComments = true,
                authorUserId = "emp-006",
                authorName = "渡辺",
                createdAt = "202604161000",
                comments = listOf(
                    comment("cmt-059", "post-019", "比較表のサンプルが入ったのはかなり使いやすそうです。", "emp-004", "田中", "202604161015"),
                    comment("cmt-060", "post-019", "物流業界向けの事例もあとで追加できそうです。", "emp-008", "中村", "202604161023"),
                    comment("cmt-061", "post-019", "過去提案書から流用できる図版も整理しておきます。", "emp-005", "伊藤", "202604161031")
                )
            ),
            post(
                id = "post-020",
                categoryId = "cat-department",
                targetDepartment1 = "営業部",
                title = "月末売上見込の更新依頼",
                body = "4月月末の売上見込を4月24日18:00までに更新してください。\n案件ランクの見直しも合わせてお願いします。\n厳しい案件はコメントで共有をお願いします。",
                startAt = "202604171300",
                endAt = "202604252359",
                allowComments = true,
                authorUserId = "emp-002",
                authorName = "鈴木",
                createdAt = "202604171300",
                comments = listOf(
                    comment("cmt-062", "post-020", "営業1課は木曜17時までに一度更新します。", "emp-003", "高橋", "202604171315"),
                    comment("cmt-063", "post-020", "営業2課も案件ランク見直しを進めています。", "emp-007", "山本", "202604171323"),
                    comment("cmt-064", "post-020", "厳しめ案件は別タブで管理できるようにしておきます。", "emp-006", "渡辺", "202604171331")
                )
            ),
            post(
                id = "post-021",
                categoryId = "cat-department",
                targetDepartment1 = "営業部",
                title = "5月商談同行希望の受付",
                body = "5月前半の大型商談で同行支援が必要な案件を集約します。\n若手メンバーの同席希望や、部長同行が必要な案件はコメントで共有してください。",
                startAt = "202604201100",
                endAt = "202605102359",
                allowComments = true,
                authorUserId = "emp-006",
                authorName = "渡辺",
                createdAt = "202604201100",
                comments = listOf(
                    comment("cmt-065", "post-021", "A商事の次回商談で同行をお願いしたいです。", "emp-004", "田中", "202604201115"),
                    comment("cmt-066", "post-021", "北斗商事案件は営業2課で若手同行枠を出せます。", "emp-007", "山本", "202604201123"),
                    comment("cmt-067", "post-021", "部長同行案件は週内に整理して返答します。", "emp-006", "渡辺", "202604201131")
                )
            ),
            post(
                id = "post-022",
                categoryId = "cat-department",
                targetDepartment1 = "総務部",
                title = "来客対応当番表 更新版掲載",
                body = "4月後半の来客対応当番表を更新しました。\n不在予定がある場合は4月16日までにコメントで調整希望を記載してください。\n受付マニュアルも最新版に差し替えています。",
                startAt = "202604131300",
                endAt = "202604302359",
                allowComments = true,
                authorUserId = "emp-010",
                authorName = "加藤",
                createdAt = "202604131300",
                comments = listOf(
                    comment("cmt-068", "post-022", "4/15午後の受付枠を担当できます。", "emp-016", "井上", "202604131720"),
                    comment("cmt-069", "post-022", "来客用の会議室予約も済ませました。", "emp-017", "木村", "202604131728"),
                    comment("cmt-070", "post-022", "4/18午前は外出なので別日の担当に回れます。", "emp-013", "佐々木", "202604131736")
                )
            ),
            post(
                id = "post-023",
                categoryId = "cat-department",
                targetDepartment1 = "総務部",
                title = "備品棚卸の進め方について",
                body = "4月末の備品棚卸に向けて、担当エリアを更新しました。\nカタログ在庫と現物差異がある場合は写真付きで共有してください。\n確認期限は4月28日です。",
                startAt = "202604151140",
                endAt = "202604302359",
                allowComments = true,
                authorUserId = "emp-011",
                authorName = "吉田",
                createdAt = "202604151140",
                comments = listOf(
                    comment("cmt-071", "post-023", "受付横の文具棚も対象に含めて確認します。", "emp-012", "山田", "202604151155"),
                    comment("cmt-072", "post-023", "在庫差異が出た備品は写真付きでまとめます。", "emp-016", "井上", "202604151203"),
                    comment("cmt-073", "post-023", "会議室備品は総務2課で集中的に確認します。", "emp-015", "松本", "202604151211")
                )
            ),
            post(
                id = "post-024",
                categoryId = "cat-department",
                targetDepartment1 = "総務部",
                title = "会議室レイアウト変更案の確認",
                body = "説明会対応に合わせて大会議室のレイアウトを見直しています。\nスクール形式と島型の両パターンを試したいので、設営しやすさの観点も含めて意見をください。",
                startAt = "202604171430",
                endAt = "202605052359",
                allowComments = true,
                authorUserId = "emp-015",
                authorName = "松本",
                createdAt = "202604171430",
                comments = listOf(
                    comment("cmt-074", "post-024", "スクール形式だと後方席の見え方が気になっています。", "emp-011", "吉田", "202604171445"),
                    comment("cmt-075", "post-024", "島型だと座談会への切り替えがしやすそうです。", "emp-017", "木村", "202604171453"),
                    comment("cmt-076", "post-024", "プロジェクターの位置も考えると中央通路は広めが良さそうです。", "emp-014", "山口", "202604171501")
                )
            ),
            post(
                id = "post-025",
                categoryId = "cat-department",
                targetDepartment1 = "総務部",
                title = "受付マニュアル改訂版を配布しました",
                body = "外部業者来訪時の案内フローを追記した改訂版を掲載しました。\n受付担当経験が浅いメンバーは一度目を通してください。\n気づきがあればコメント歓迎です。",
                startAt = "202604181410",
                endAt = "202605152359",
                allowComments = true,
                authorUserId = "emp-014",
                authorName = "山口",
                createdAt = "202604181410",
                comments = listOf(
                    comment("cmt-077", "post-025", "業者来訪時の名札受け渡し手順が分かりやすくなりました。", "emp-016", "井上", "202604181425"),
                    comment("cmt-078", "post-025", "受付担当の初回説明にも使いやすそうです。", "emp-012", "山田", "202604181433")
                )
            ),
            post(
                id = "post-026",
                categoryId = "cat-department",
                targetDepartment1 = "総務部",
                title = "社内イベント運営メンバー募集",
                body = "5月の社内表彰イベントに向けて、会場準備と受付対応を担当するメンバーを募ります。\n当日のみの参加でも大丈夫です。\n手伝える方はコメントください。",
                startAt = "202604201330",
                endAt = "202605202359",
                allowComments = true,
                authorUserId = "emp-010",
                authorName = "加藤",
                createdAt = "202604201330",
                comments = listOf(
                    comment("cmt-079", "post-026", "当日の受付なら対応できます。", "emp-015", "松本", "202604201345"),
                    comment("cmt-080", "post-026", "備品搬入の前日準備も手伝えます。", "emp-017", "木村", "202604201353"),
                    comment("cmt-081", "post-026", "タイムテーブル案が出たら見たいです。", "emp-013", "佐々木", "202604201401")
                )
            ),
            post(
                id = "post-027",
                categoryId = "cat-department",
                targetDepartment1 = "人事部",
                title = "面接評価シート運用変更",
                body = "一次面接後の評価入力を当日中必須に変更します。\n評価項目の定義を見直し、カルチャーフィット欄を追加しました。\n4月17日に短い説明会を行います。",
                startAt = "202604131330",
                endAt = "202604302359",
                allowComments = true,
                authorUserId = "emp-019",
                authorName = "斎藤",
                createdAt = "202604131330",
                comments = listOf(
                    comment("cmt-082", "post-027", "部長レビューは翌営業日までで運用します。", "emp-018", "林", "202604131845"),
                    comment("cmt-083", "post-027", "説明会は4/17 16:00で確定しました。", "emp-023", "池田", "202604131853"),
                    comment("cmt-084", "post-027", "カルチャーフィット欄の記入例もあると助かります。", "emp-021", "山崎", "202604131901")
                )
            ),
            post(
                id = "post-028",
                categoryId = "cat-department",
                targetDepartment1 = "人事部",
                title = "会社説明会の役割分担について",
                body = "次回会社説明会の受付、司会、座談会担当の割り振り案を掲載します。\n都合が悪い時間帯があれば今週中にコメントしてください。\n資料説明担当は別途調整します。",
                startAt = "202604151200",
                endAt = "202604252359",
                allowComments = true,
                authorUserId = "emp-018",
                authorName = "林",
                createdAt = "202604151200",
                comments = listOf(
                    comment("cmt-085", "post-028", "受付と司会の兼務は避けたいので、タイムライン確認後に再調整したいです。", "emp-020", "清水", "202604151215"),
                    comment("cmt-086", "post-028", "座談会担当は営業部と情シスにも相談しておきます。", "emp-018", "林", "202604151223"),
                    comment("cmt-087", "post-028", "資料説明パートは人事1課で一度台本化してみます。", "emp-019", "斎藤", "202604151231")
                )
            ),
            post(
                id = "post-029",
                categoryId = "cat-department",
                targetDepartment1 = "人事部",
                title = "候補者フォロー状況の共有",
                body = "内定承諾前の候補者について、フォロー面談や追加質問の状況を共有してください。\n温度感が下がっている候補者は早めに相談したいです。",
                startAt = "202604171000",
                endAt = "202605152359",
                allowComments = true,
                authorUserId = "emp-023",
                authorName = "池田",
                createdAt = "202604171000",
                comments = listOf(
                    comment("cmt-088", "post-029", "内定承諾前の候補者で反応が鈍い方を別途一覧にします。", "emp-021", "山崎", "202604171015"),
                    comment("cmt-089", "post-029", "面談依頼のタイミングも合わせて整理したいです。", "emp-024", "橋本", "202604171023"),
                    comment("cmt-090", "post-029", "部門面談が必要な候補者は今週中に相談してください。", "emp-018", "林", "202604171031")
                )
            ),
            post(
                id = "post-030",
                categoryId = "cat-department",
                targetDepartment1 = "人事部",
                title = "新卒研修コンテンツ案の募集",
                body = "今年の新卒研修で追加したいコンテンツ案を募集します。\nビジネスマナー、業界理解、配属前準備など、良さそうなテーマがあればコメントしてください。",
                startAt = "202604181500",
                endAt = "202605102359",
                allowComments = true,
                authorUserId = "emp-020",
                authorName = "清水",
                createdAt = "202604181500",
                comments = listOf(
                    comment("cmt-091", "post-030", "配属前に社内システムの使い方紹介があると良さそうです。", "emp-026", "石川", "202604181515"),
                    comment("cmt-092", "post-030", "営業部からは提案書の読み方入門があると良いと思います。", "emp-002", "鈴木", "202604181523"),
                    comment("cmt-093", "post-030", "総務からは来客対応や備品申請の基本も入れたいです。", "emp-010", "加藤", "202604181531")
                )
            ),
            post(
                id = "post-031",
                categoryId = "cat-department",
                targetDepartment1 = "人事部",
                title = "求人媒体別の応募傾向まとめ",
                body = "4月前半の応募傾向を媒体別に整理しました。\n歩留まりが良い媒体と改善余地のある媒体を一覧にしています。\n5月予算配分の参考にしてください。",
                startAt = "202604201030",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-021",
                authorName = "山崎",
                createdAt = "202604201030",
                comments = listOf(
                    comment("cmt-094", "post-031", "媒体ごとの面談化率も入ると次回比較しやすいです。", "emp-019", "斎藤", "202604201045"),
                    comment("cmt-095", "post-031", "説明会経由の歩留まりも別で見てみたいです。", "emp-018", "林", "202604201053"),
                    comment("cmt-096", "post-031", "5月予算配分のたたき台作成時に参照します。", "emp-023", "池田", "202604201101")
                )
            ),
            post(
                id = "post-032",
                categoryId = "cat-department",
                targetDepartment1 = "情報システム部",
                title = "資産管理台帳 棚卸のお願い",
                body = "4月末締めでPC・モニター・貸与スマホの棚卸を行います。\n各自の保有資産を台帳で確認し、相違がある場合はコメントしてください。\n未使用機器は返却予定日も記載をお願いします。",
                startAt = "202604131400",
                endAt = "202604302359",
                allowComments = true,
                authorUserId = "emp-027",
                authorName = "山下",
                createdAt = "202604131400",
                comments = listOf(
                    comment("cmt-097", "post-032", "PC更新対象の一覧を本日中に掲示します。", "emp-032", "岡田", "202604131910"),
                    comment("cmt-098", "post-032", "未返却機器の確認を進めます。結果は明日共有します。", "emp-033", "長谷川", "202604131918"),
                    comment("cmt-099", "post-032", "貸与スマホの棚卸は来週前半で完了見込みです。", "emp-027", "山下", "202604131926")
                )
            ),
            post(
                id = "post-033",
                categoryId = "cat-department",
                targetDepartment1 = "情報システム部",
                title = "定例保守作業の確認",
                body = "4月第4週の定例保守作業内容をまとめました。\n影響範囲が広いものは事前告知の文面も合わせて確認してください。\n手順書修正がある場合はコメントをお願いします。",
                startAt = "202604151500",
                endAt = "202604262359",
                allowComments = true,
                authorUserId = "emp-026",
                authorName = "石川",
                createdAt = "202604151500",
                comments = listOf(
                    comment("cmt-100", "post-033", "影響範囲の大きい作業から先に告知文を作ります。", "emp-031", "前田", "202604151515"),
                    comment("cmt-101", "post-033", "VPN影響のある作業は営業部にも事前共有をお願いします。", "emp-026", "石川", "202604151523"),
                    comment("cmt-102", "post-033", "手順書の表現を少し平易にして掲載します。", "emp-028", "中島", "202604151531")
                )
            ),
            post(
                id = "post-034",
                categoryId = "cat-department",
                targetDepartment1 = "情報システム部",
                title = "社内ポータル改善候補の整理",
                body = "ポータル改善候補を一覧化しました。\n優先度、想定効果、対応工数の3軸で仮評価しています。\n抜けている観点があれば追記をお願いします。",
                startAt = "202604171230",
                endAt = "202605202359",
                allowComments = true,
                authorUserId = "emp-028",
                authorName = "中島",
                createdAt = "202604171230",
                comments = listOf(
                    comment("cmt-103", "post-034", "申請導線はモバイル表示も意識したいです。", "emp-029", "石井", "202604171245"),
                    comment("cmt-104", "post-034", "掲示板の検索性も改善候補に入れたいです。", "emp-032", "岡田", "202604171253"),
                    comment("cmt-105", "post-034", "営業からはスケジュール共有の見やすさ改善要望が多いです。", "emp-003", "高橋", "202604171301")
                )
            ),
            post(
                id = "post-035",
                categoryId = "cat-department",
                targetDepartment1 = "情報システム部",
                title = "監視アラート見直し案",
                body = "夜間通知が多い監視項目について、見直し案を作成しました。\n優先度の低い通知は抑制し、一次対応が必要なものを目立たせたいです。\nインシデント履歴も参考にしています。",
                startAt = "202604181200",
                endAt = "202605102359",
                allowComments = true,
                authorUserId = "emp-031",
                authorName = "前田",
                createdAt = "202604181200",
                comments = listOf(
                    comment("cmt-106", "post-035", "夜間通知のうち再通知不要なものはまとめて良さそうです。", "emp-029", "石井", "202604181215"),
                    comment("cmt-107", "post-035", "一次対応が必要なアラートだけ音を変える案も賛成です。", "emp-033", "長谷川", "202604181223"),
                    comment("cmt-108", "post-035", "月次で件数推移も見たいです。", "emp-026", "石川", "202604181231")
                )
            ),
            post(
                id = "post-036",
                categoryId = "cat-department",
                targetDepartment1 = "情報システム部",
                title = "認証基盤更新の事前確認",
                body = "SSO設定の更新を5月中旬に予定しています。\n影響しそうなアプリケーション一覧を掲載したので、担当者は確認をお願いします。\n連携漏れがある場合はコメントで共有してください。",
                startAt = "202604201400",
                endAt = "202605202359",
                allowComments = true,
                authorUserId = "emp-030",
                authorName = "小川",
                createdAt = "202604201400",
                comments = listOf(
                    comment("cmt-109", "post-036", "人事システム連携は影響確認済みです。", "emp-023", "池田", "202604201415"),
                    comment("cmt-110", "post-036", "営業支援ツール側のSAML設定も確認しておきます。", "emp-032", "岡田", "202604201423"),
                    comment("cmt-111", "post-036", "連携漏れがないか一覧を再チェックします。", "emp-027", "山下", "202604201431")
                )
            ),
            post(
                id = "post-037",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "おすすめランチ情報を共有してください",
                body = "新しく入ったメンバー向けに、会社周辺のお店情報を集めたいです。\n価格帯・混雑具合・テイクアウト可否などもあるとうれしいです。",
                startAt = "202604131430",
                endAt = "202605102359",
                allowComments = true,
                authorUserId = "emp-004",
                authorName = "田中",
                createdAt = "202604131430",
                comments = listOf(
                    comment("cmt-112", "post-037", "総務部は庁舎裏の定食屋が人気です。テイクアウトもできます。", "emp-013", "佐々木", "202604131940"),
                    comment("cmt-113", "post-037", "人事部は駅前のベーカリーカフェ推しです。朝の打ち合わせにも使えます。", "emp-024", "橋本", "202604131948"),
                    comment("cmt-114", "post-037", "営業部ではカレー店の回転が早くて助かっています。", "emp-002", "鈴木", "202604131956"),
                    comment("cmt-115", "post-037", "情報システム部は少し遠いですが定食屋の焼き魚が人気です。", "emp-031", "前田", "202604132004")
                )
            ),
            post(
                id = "post-038",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "Kotlin勉強会を開催します",
                body = "4月23日（木）18:00から30分ほど、Androidの小さな勉強会をやります。\nテーマはState管理とデバッグの小ネタです。\n興味がある方はコメントください。",
                startAt = "202604131500",
                endAt = "202605052359",
                allowComments = true,
                authorUserId = "emp-028",
                authorName = "中島",
                createdAt = "202604131500",
                comments = listOf(
                    comment("cmt-116", "post-038", "Android実機確認の小ネタも共有したいです。", "emp-029", "石井", "202604131510"),
                    comment("cmt-117", "post-038", "ネットワーク周りの補足もできるので、必要なら5分ください。", "emp-031", "前田", "202604131518"),
                    comment("cmt-118", "post-038", "営業でも議事録の残し方など応用できそうで興味あります。", "emp-008", "中村", "202604131526"),
                    comment("cmt-119", "post-038", "リモート参加できるなら聞きたいです。", "emp-020", "清水", "202604131534")
                )
            ),
            post(
                id = "post-039",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "会議室の観葉植物お世話当番募集",
                body = "総務部で管理している観葉植物の水やり当番を月替わりで回したいです。\n気軽に手伝える方はコメントください。\n小さなことですが働きやすい雰囲気づくりにしたいです。",
                startAt = "202604131530",
                endAt = "202605052359",
                allowComments = true,
                authorUserId = "emp-012",
                authorName = "山田",
                createdAt = "202604131530",
                comments = listOf(
                    comment("cmt-120", "post-039", "総務2課で水やり当番を回せます。1週目を担当します。", "emp-015", "松本", "202604131540"),
                    comment("cmt-121", "post-039", "情シス側で自動給水器を試作できるか見てみます。", "emp-027", "山下", "202604131548"),
                    comment("cmt-122", "post-039", "人事部でも2週目担当できます。", "emp-020", "清水", "202604131556"),
                    comment("cmt-123", "post-039", "営業部でも手伝えます。昼休み前後なら対応しやすいです。", "emp-005", "伊藤", "202604131604")
                )
            ),
            post(
                id = "post-040",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "最近読んでよかった本を教えてください",
                body = "通勤時間や休日に読んで良かった本があれば共有してほしいです。\nビジネス書でも小説でも歓迎です。\nおすすめポイントもあるとうれしいです。",
                startAt = "202604151730",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-024",
                authorName = "橋本",
                createdAt = "202604151730",
                comments = listOf(
                    comment("cmt-124", "post-040", "最近だと『イシューからはじめよ』を読み返しました。会議の持ち方にも効きます。", "emp-031", "前田", "202604151745"),
                    comment("cmt-125", "post-040", "小説なら短編が多いものが通勤時間に読みやすいです。", "emp-013", "佐々木", "202604151753"),
                    comment("cmt-126", "post-040", "採用広報の参考でデザイン本をよく見ています。", "emp-021", "山崎", "202604151801")
                )
            ),
            post(
                id = "post-041",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "社内勉強会で扱ってほしいテーマ募集",
                body = "今後の社内勉強会で扱ってほしいテーマを募集します。\n業務効率化、営業ノウハウ、採用広報、開発周りなど、ジャンルは問いません。",
                startAt = "202604161700",
                endAt = "202605202359",
                allowComments = true,
                authorUserId = "emp-029",
                authorName = "石井",
                createdAt = "202604161700",
                comments = listOf(
                    comment("cmt-127", "post-041", "営業向けだと提案書レビューの観点整理があるとうれしいです。", "emp-004", "田中", "202604161715"),
                    comment("cmt-128", "post-041", "採用広報の写真の撮り方とかもニーズありそうです。", "emp-018", "林", "202604161723"),
                    comment("cmt-129", "post-041", "ショートカットや自動化系の小ネタ回があると参加しやすいです。", "emp-016", "井上", "202604161731")
                )
            ),
            post(
                id = "post-042",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "雨の日に便利だった通勤アイテム共有",
                body = "梅雨前に、通勤で便利だったアイテムや工夫を共有したいです。\n折りたたみ傘、撥水バッグ、靴対策など何でも歓迎です。",
                startAt = "202604181700",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-017",
                authorName = "木村",
                createdAt = "202604181700",
                comments = listOf(
                    comment("cmt-130", "post-042", "折りたたみ傘を2本持ちにして、1本は会社置きにしています。", "emp-011", "吉田", "202604181715"),
                    comment("cmt-131", "post-042", "撥水のリュックカバーがかなり便利でした。", "emp-029", "石井", "202604181723"),
                    comment("cmt-132", "post-042", "替えの靴下をロッカーに置いておく派です。", "emp-024", "橋本", "202604181731")
                )
            ),
            post(
                id = "post-043",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "歓迎会のお店候補を募集します",
                body = "来月の歓迎会に向けて、お店候補を集めています。\n人数が多くても入りやすいこと、料理の好みが分かれにくいことを重視しています。\nおすすめがあれば教えてください。",
                startAt = "202604201700",
                endAt = "202605152359",
                allowComments = true,
                authorUserId = "emp-008",
                authorName = "中村",
                createdAt = "202604201700",
                comments = listOf(
                    comment("cmt-133", "post-043", "駅前の和食店は大人数でも入りやすかったです。", "emp-013", "佐々木", "202604201715"),
                    comment("cmt-134", "post-043", "席間が広い店だと話しやすくて良かったです。", "emp-007", "山本", "202604201723"),
                    comment("cmt-135", "post-043", "ベジタリアン対応がある店も候補に入れたいです。", "emp-020", "清水", "202604201731")
                )
            ),
            post(
                id = "post-044",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "デスク周りの便利グッズ共有",
                body = "最近導入して良かったデスク周りグッズがあれば教えてください。\nケーブル整理、姿勢改善、作業効率アップ系が気になっています。",
                startAt = "202604221700",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-032",
                authorName = "岡田",
                createdAt = "202604221700",
                comments = listOf(
                    comment("cmt-136", "post-044", "モニター台兼収納みたいなものが地味に便利でした。", "emp-033", "長谷川", "202604221715"),
                    comment("cmt-137", "post-044", "ケーブル整理はマグネット式が使いやすいです。", "emp-028", "中島", "202604221723"),
                    comment("cmt-138", "post-044", "姿勢改善ならフットレストもかなり効きました。", "emp-017", "木村", "202604221731")
                )
            ),
            post(
                id = "post-045",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "社内で使っているショートカット術を共有しませんか",
                body = "Excel、Google Workspace、エディタ、チャットなどで使っている便利なショートカットや小技があれば知りたいです。\n業務が少し楽になるネタ歓迎です。",
                startAt = "202604241700",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-020",
                authorName = "清水",
                createdAt = "202604241700",
                comments = listOf(
                    comment("cmt-139", "post-045", "Googleスプレッドシートのフィルタ系ショートカットをよく使っています。", "emp-019", "斎藤", "202604241715"),
                    comment("cmt-140", "post-045", "ブラウザのタブ移動系は地味に時短効果が大きいです。", "emp-031", "前田", "202604241723"),
                    comment("cmt-141", "post-045", "チャットの未読ジャンプ系もまとめたいです。", "emp-021", "山崎", "202604241731")
                )
            ),
            post(
                id = "post-046",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "在宅勤務時の気分転換アイデア募集",
                body = "在宅勤務の日にリフレッシュするための工夫があれば共有してください。\n散歩、ストレッチ、飲み物、作業環境の整え方など何でも大丈夫です。",
                startAt = "202605011200",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-005",
                authorName = "伊藤",
                createdAt = "202605011200",
                comments = listOf(
                    comment("cmt-142", "post-046", "昼休みに10分だけ散歩すると切り替えやすいです。", "emp-012", "山田", "202605011215"),
                    comment("cmt-143", "post-046", "温かい飲み物を淹れ直すだけでもかなり変わります。", "emp-024", "橋本", "202605011223"),
                    comment("cmt-144", "post-046", "午後一に短いストレッチを入れるようにしています。", "emp-029", "石井", "202605011231")
                )
            ),
            post(
                id = "post-047",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "社内で流行っているお菓子情報",
                body = "部署で話題になったお菓子や差し入れで好評だったものを共有しませんか。\n手土産選びの参考にもしたいです。",
                startAt = "202605021230",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-016",
                authorName = "井上",
                createdAt = "202605021230",
                comments = listOf(
                    comment("cmt-145", "post-047", "最近だと個包装の焼き菓子が配りやすくて好評でした。", "emp-015", "松本", "202605021245"),
                    comment("cmt-146", "post-047", "塩系スナックも甘い物が苦手な人に刺さります。", "emp-008", "中村", "202605021253"),
                    comment("cmt-147", "post-047", "個包装で日持ちするものだと総務としても助かります。", "emp-011", "吉田", "202605021301")
                )
            ),
            post(
                id = "post-048",
                categoryId = "cat-free",
                targetDepartment1 = null,
                title = "朝会で使える一言ネタを募集",
                body = "朝会の冒頭で少し話せる軽い話題や小ネタを募集しています。\n最近の気づきやおすすめ情報など、場が和む内容だとうれしいです。",
                startAt = "202605071000",
                endAt = "202605312359",
                allowComments = true,
                authorUserId = "emp-027",
                authorName = "山下",
                createdAt = "202605071000",
                comments = listOf(
                    comment("cmt-148", "post-048", "最近だと『週末に見た展示が良かった』みたいな軽い話題が使いやすかったです。", "emp-020", "清水", "202605071015"),
                    comment("cmt-149", "post-048", "気候ネタと業務小ネタを一緒にすると話しやすいです。", "emp-013", "佐々木", "202605071023"),
                    comment("cmt-150", "post-048", "おすすめランチの新店情報も朝会で盛り上がりました。", "emp-007", "山本", "202605071031")
                )
            )
        )
    }
}