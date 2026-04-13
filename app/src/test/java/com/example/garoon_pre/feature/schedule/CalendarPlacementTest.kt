package com.example.garoon_pre.feature.schedule

import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.presentation.common.buildCalendarPlacements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CalendarPlacementTest {

    @Test
    fun overlapping_events_are_split_into_multiple_columns() {
        val first = Schedule(
            id = "a",
            title = "A",
            startAt = "2026-04-08T09:00:00+09:00",
            endAt = "2026-04-08T10:30:00+09:00",
            location = null,
            description = null,
            ownerUserId = "emp-001",
            organizerName = "田中"
        )
        val second = Schedule(
            id = "b",
            title = "B",
            startAt = "2026-04-08T09:30:00+09:00",
            endAt = "2026-04-08T10:00:00+09:00",
            location = null,
            description = null,
            ownerUserId = "emp-001",
            organizerName = "田中"
        )
        val third = Schedule(
            id = "c",
            title = "C",
            startAt = "2026-04-08T11:00:00+09:00",
            endAt = "2026-04-08T12:00:00+09:00",
            location = null,
            description = null,
            ownerUserId = "emp-001",
            organizerName = "田中"
        )

        val placements = buildCalendarPlacements(listOf(first, second, third))
            .associateBy { it.schedule.id }

        val placementA = placements.getValue("a")
        val placementB = placements.getValue("b")
        val placementC = placements.getValue("c")

        assertEquals(2, placementA.totalColumns)
        assertEquals(2, placementB.totalColumns)
        assertNotEquals(placementA.column, placementB.column)

        assertEquals(1, placementC.totalColumns)
    }
}