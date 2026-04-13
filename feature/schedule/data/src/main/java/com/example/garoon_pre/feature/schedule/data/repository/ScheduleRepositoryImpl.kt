package com.example.garoon_pre.feature.schedule.data.repository

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.data.local.ScheduleDetailDao
import com.example.garoon_pre.feature.schedule.data.local.ScheduleOccurrenceDao
import com.example.garoon_pre.feature.schedule.data.local.toDetailEntity
import com.example.garoon_pre.feature.schedule.data.local.toDomain
import com.example.garoon_pre.feature.schedule.data.local.toOccurrenceEntity
import com.example.garoon_pre.feature.schedule.data.remote.ScheduleApi
import com.example.garoon_pre.feature.schedule.data.remote.toRequest
import com.example.garoon_pre.feature.schedule.domain.model.ScheduleInput
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import com.example.garoon_pre.feature.user.data.remote.UserApi
import com.example.garoon_pre.feature.user.data.remote.toDomain as userDtoToDomain
import com.example.garoon_pre.core.model.user.GaroonUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleApi: ScheduleApi,
    private val userApi: UserApi,
    private val sessionStore: SessionStore,
    private val scheduleOccurrenceDao: ScheduleOccurrenceDao,
    private val scheduleDetailDao: ScheduleDetailDao
) : ScheduleRepository {

    override fun observeSchedulesByDate(
        date: String,
        userIds: List<String>
    ) = if (normalizeUserIds(userIds).isEmpty()) {
        scheduleOccurrenceDao.observeByDate(date)
    } else {
        scheduleOccurrenceDao.observeByDateForUsers(date, normalizeUserIds(userIds))
    }.map { items ->
        items.map { it.toDomain() }
    }

    override fun observeSchedulesInRange(
        startDate: String,
        endDate: String,
        userIds: List<String>
    ) = if (normalizeUserIds(userIds).isEmpty()) {
        scheduleOccurrenceDao.observeRange(startDate, endDate)
    } else {
        scheduleOccurrenceDao.observeRangeForUsers(
            startDateKey = startDate,
            endDateKey = endDate,
            ownerUserIds = normalizeUserIds(userIds)
        )
    }.map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun syncSchedulesByDate(
        date: String,
        userIds: List<String>
    ) {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) return

        val normalizedUserIds = normalizeUserIds(userIds)
        val currentAuthUserId = sessionStore.authUserIdFlow.first().trim()

        val ownerIdsToReplace = if (normalizedUserIds.isNotEmpty()) {
            normalizedUserIds
        } else {
            listOf(currentAuthUserId).filter { it.isNotBlank() }
        }

        val response = scheduleApi.getSchedules(
            authorization = "Bearer $token",
            date = date,
            userIds = normalizedUserIds.takeIf { it.isNotEmpty() }?.joinToString(",")
        )

        if (ownerIdsToReplace.isNotEmpty()) {
            scheduleOccurrenceDao.deleteByDateAndOwnerUsers(
                dateKey = date,
                ownerUserIds = ownerIdsToReplace
            )
        } else {
            scheduleOccurrenceDao.deleteByDate(date)
        }

        scheduleOccurrenceDao.upsertAll(
            response.items.map { dto ->
                dto.toOccurrenceEntity(requestedDateKey = date)
            }
        )
    }

    override fun observeScheduleDetail(id: String) =
        scheduleDetailDao.observeById(id).map { entity ->
            entity?.toDomain()
        }

    override suspend fun refreshScheduleDetail(id: String) {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) {
            throw IllegalStateException("ログイン情報がありません")
        }

        val item = scheduleApi.getScheduleById(
            authorization = "Bearer $token",
            id = id
        )

        scheduleDetailDao.upsert(item.toDetailEntity())
    }

    override suspend fun createSchedule(input: ScheduleInput) {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) {
            throw IllegalStateException("ログイン情報がありません")
        }

        val created = scheduleApi.createSchedule(
            authorization = "Bearer $token",
            request = input.toRequest()
        )

        scheduleDetailDao.upsert(created.toDetailEntity())

        val currentAuthUserId = sessionStore.authUserIdFlow.first().trim()
        val ownerIds = normalizeUserIds(
            listOf(
                created.ownerUserId.orEmpty(),
                currentAuthUserId
            )
        )

        val dateKey = created.startAt.takeIf { it.length >= 8 }?.take(8)
        if (!dateKey.isNullOrBlank()) {
            syncSchedulesByDate(
                date = dateKey,
                userIds = ownerIds
            )
        }
    }

    override suspend fun getUsers(): List<GaroonUser> {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) return emptyList()

        return userApi.getUsers(
            authorization = "Bearer $token"
        ).items.map { it.userDtoToDomain() }
    }

    override suspend fun updateSchedule(
        scheduleId: String,
        input: ScheduleInput
    ) {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) {
            throw IllegalStateException("ログイン情報がありません")
        }

        val before = observeScheduleDetail(scheduleId).first()

        val updated = scheduleApi.updateSchedule(
            authorization = "Bearer $token",
            id = scheduleId,
            request = input.toRequest()
        )

        scheduleDetailDao.upsert(updated.toDetailEntity())

        val currentAuthUserId = sessionStore.authUserIdFlow.first().trim()
        val ownerIds = normalizeUserIds(
            listOf(
                updated.ownerUserId.orEmpty(),
                before?.ownerUserId.orEmpty(),
                currentAuthUserId
            )
        )

        buildSet {
            before?.startAt?.takeIf { it.length >= 8 }?.take(8)?.let(::add)
            updated.startAt.takeIf { it.length >= 8 }?.take(8)?.let(::add)
        }.forEach { dateKey ->
            syncSchedulesByDate(
                date = dateKey,
                userIds = ownerIds
            )
        }
    }

    private fun normalizeUserIds(userIds: List<String>): List<String> {
        return userIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}