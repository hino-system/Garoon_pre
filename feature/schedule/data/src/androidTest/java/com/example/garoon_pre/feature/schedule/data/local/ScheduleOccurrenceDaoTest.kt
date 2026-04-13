package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduleOccurrenceDaoTest {

    private lateinit var database: ScheduleDatabase
    private lateinit var dao: ScheduleOccurrenceDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ScheduleDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.scheduleOccurrenceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_and_observeByDate_returns_inserted_items() = runBlocking {
        dao.upsertAll(
            listOf(
                ScheduleOccurrenceEntity(
                    occurrenceId = "s1#202604130900",
                    scheduleId = "s1",
                    dateKey = "20260413",
                    title = "定例会議",
                    startAt = "202604130900",
                    endAt = "202604131000",
                    repeatRule = "なし",
                    location = "会議室A",
                    description = "memo",
                    ownerUserId = "u1",
                    organizerName = "田中",
                    syncedAtEpochMillis = 1L
                )
            )
        )

        val items = dao.observeByDate("20260413").first()

        assertEquals(1, items.size)
        assertEquals("定例会議", items.first().title)
        assertEquals("s1", items.first().scheduleId)
    }

    @Test
    fun observeByDateForUsers_filters_by_owner_user_id() = runBlocking {
        dao.upsertAll(
            listOf(
                ScheduleOccurrenceEntity(
                    occurrenceId = "s1#202604130900",
                    scheduleId = "s1",
                    dateKey = "20260413",
                    title = "Aさん予定",
                    startAt = "202604130900",
                    endAt = "202604131000",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u1",
                    organizerName = "田中",
                    syncedAtEpochMillis = 1L
                ),
                ScheduleOccurrenceEntity(
                    occurrenceId = "s2#202604131100",
                    scheduleId = "s2",
                    dateKey = "20260413",
                    title = "Bさん予定",
                    startAt = "202604131100",
                    endAt = "202604131200",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u2",
                    organizerName = "佐藤",
                    syncedAtEpochMillis = 1L
                )
            )
        )

        val items = dao.observeByDateForUsers(
            dateKey = "20260413",
            ownerUserIds = listOf("u1")
        ).first()

        assertEquals(1, items.size)
        assertEquals("Aさん予定", items.first().title)
        assertEquals("u1", items.first().ownerUserId)
    }

    @Test
    fun observeRangeForUsers_returns_only_target_dates_and_users() = runBlocking {
        dao.upsertAll(
            listOf(
                ScheduleOccurrenceEntity(
                    occurrenceId = "s1#202604130900",
                    scheduleId = "s1",
                    dateKey = "20260413",
                    title = "対象1",
                    startAt = "202604130900",
                    endAt = "202604131000",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u1",
                    organizerName = "田中",
                    syncedAtEpochMillis = 1L
                ),
                ScheduleOccurrenceEntity(
                    occurrenceId = "s2#202604141100",
                    scheduleId = "s2",
                    dateKey = "20260414",
                    title = "対象2",
                    startAt = "202604141100",
                    endAt = "202604141200",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u1",
                    organizerName = "田中",
                    syncedAtEpochMillis = 1L
                ),
                ScheduleOccurrenceEntity(
                    occurrenceId = "s3#202604151300",
                    scheduleId = "s3",
                    dateKey = "20260415",
                    title = "対象外ユーザー",
                    startAt = "202604151300",
                    endAt = "202604151400",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u2",
                    organizerName = "佐藤",
                    syncedAtEpochMillis = 1L
                ),
                ScheduleOccurrenceEntity(
                    occurrenceId = "s4#202604161300",
                    scheduleId = "s4",
                    dateKey = "20260416",
                    title = "対象外日付",
                    startAt = "202604161300",
                    endAt = "202604161400",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u1",
                    organizerName = "鈴木",
                    syncedAtEpochMillis = 1L
                )
            )
        )

        val items = dao.observeRangeForUsers(
            startDateKey = "20260413",
            endDateKey = "20260415",
            ownerUserIds = listOf("u1")
        ).first()

        assertEquals(2, items.size)
        assertEquals(listOf("対象1", "対象2"), items.map { it.title })
    }

    @Test
    fun deleteByDateAndOwnerUsers_deletes_only_target_user_rows() = runBlocking {
        dao.upsertAll(
            listOf(
                ScheduleOccurrenceEntity(
                    occurrenceId = "s1#202604130900",
                    scheduleId = "s1",
                    dateKey = "20260413",
                    title = "削除対象",
                    startAt = "202604130900",
                    endAt = "202604131000",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u1",
                    organizerName = "田中",
                    syncedAtEpochMillis = 1L
                ),
                ScheduleOccurrenceEntity(
                    occurrenceId = "s2#202604131100",
                    scheduleId = "s2",
                    dateKey = "20260413",
                    title = "残る予定",
                    startAt = "202604131100",
                    endAt = "202604131200",
                    repeatRule = "なし",
                    location = null,
                    description = null,
                    ownerUserId = "u2",
                    organizerName = "佐藤",
                    syncedAtEpochMillis = 1L
                )
            )
        )

        dao.deleteByDateAndOwnerUsers(
            dateKey = "20260413",
            ownerUserIds = listOf("u1")
        )

        val remaining = dao.observeByDate("20260413").first()

        assertEquals(1, remaining.size)
        assertEquals("残る予定", remaining.first().title)
        assertEquals("u2", remaining.first().ownerUserId)
    }
}