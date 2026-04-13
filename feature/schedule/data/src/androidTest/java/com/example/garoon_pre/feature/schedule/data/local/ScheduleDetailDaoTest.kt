package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduleDetailDaoTest {

    private lateinit var database: ScheduleDatabase
    private lateinit var dao: ScheduleDetailDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ScheduleDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.scheduleDetailDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeById_returns_null_when_not_inserted() = runBlocking {
        val item = dao.observeById("not-found").first()
        assertNull(item)
    }

    @Test
    fun upsert_and_observeById_returns_inserted_item() = runBlocking {
        dao.upsert(
            ScheduleDetailEntity(
                scheduleId = "s1",
                title = "定例会議",
                startAt = "202604130900",
                endAt = "202604131000",
                repeatRule = "なし",
                location = "会議室A",
                description = "weekly",
                ownerUserId = "u1",
                organizerName = "田中",
                syncedAtEpochMillis = 1L
            )
        )

        val item = dao.observeById("s1").first()

        assertNotNull(item)
        assertEquals("定例会議", item?.title)
        assertEquals("u1", item?.ownerUserId)
        assertEquals("202604130900", item?.startAt)
    }

    @Test
    fun upsert_replaces_existing_item_with_same_schedule_id() = runBlocking {
        dao.upsert(
            ScheduleDetailEntity(
                scheduleId = "s1",
                title = "旧タイトル",
                startAt = "202604130900",
                endAt = "202604131000",
                repeatRule = "なし",
                location = "会議室A",
                description = "old",
                ownerUserId = "u1",
                organizerName = "田中",
                syncedAtEpochMillis = 1L
            )
        )

        dao.upsert(
            ScheduleDetailEntity(
                scheduleId = "s1",
                title = "新タイトル",
                startAt = "202604131100",
                endAt = "202604131200",
                repeatRule = "毎週",
                location = "会議室B",
                description = "new",
                ownerUserId = "u1",
                organizerName = "田中",
                syncedAtEpochMillis = 2L
            )
        )

        val item = dao.observeById("s1").first()

        assertNotNull(item)
        assertEquals("新タイトル", item?.title)
        assertEquals("202604131100", item?.startAt)
        assertEquals("会議室B", item?.location)
        assertEquals("new", item?.description)
        assertEquals(2L, item?.syncedAtEpochMillis)
    }
}