package com.example.garoon_pre.feature.schedule.presentation.common

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.common.data.minuteOfDay
import com.example.garoon_pre.feature.schedule.domain.model.Schedule

internal const val CALENDAR_START_HOUR = 8
internal const val CALENDAR_END_HOUR = 21

internal data class CalendarPlacement(
    val schedule: Schedule,
    val startMinute: Int,
    val endMinute: Int,
    val column: Int,
    val totalColumns: Int,
    val topDp: Dp,
    val heightDp: Dp
)

private data class AssignedCalendarItem(
    val schedule: Schedule,
    val startMinute: Int,
    val endMinute: Int,
    val column: Int
)

internal fun buildCalendarPlacements(items: List<Schedule>): List<CalendarPlacement> {
    val visibleItems = items
        .mapNotNull { schedule ->
            val startMinute = minuteOfDay(schedule.startAt) ?: return@mapNotNull null
            val endMinute = minuteOfDay(schedule.endAt) ?: return@mapNotNull null
            val clampedStart = startMinute.coerceAtLeast(CALENDAR_START_HOUR * 60)
            val clampedEnd = endMinute.coerceAtMost(CALENDAR_END_HOUR * 60)

            if (clampedEnd <= clampedStart) return@mapNotNull null

            schedule to (clampedStart to clampedEnd)
        }
        .sortedBy { (_, minutes) -> minutes.first }

    val activeItems = mutableListOf<AssignedCalendarItem>()
    val assignedItems = mutableListOf<AssignedCalendarItem>()

    visibleItems.forEach { (schedule, minutes) ->
        val startMinute = minutes.first
        val endMinute = minutes.second
        activeItems.removeAll { it.endMinute <= startMinute }

        val usedColumns = activeItems.map { it.column }.toSet()
        val nextColumn = generateSequence(0) { it + 1 }
            .first { column -> column !in usedColumns }

        val assigned = AssignedCalendarItem(
            schedule = schedule,
            startMinute = startMinute,
            endMinute = endMinute,
            column = nextColumn
        )
        activeItems += assigned
        assignedItems += assigned
    }

    return assignedItems.map { item ->
        val overlappingItems = assignedItems.filter { other ->
            other.startMinute < item.endMinute && other.endMinute > item.startMinute
        }
        val totalColumns = ((overlappingItems.maxOfOrNull { it.column } ?: 0) + 1).coerceAtLeast(1)
        val topMinutes = item.startMinute - (CALENDAR_START_HOUR * 60)
        val durationMinutes = (item.endMinute - item.startMinute).coerceAtLeast(30)

        CalendarPlacement(
            schedule = item.schedule,
            startMinute = item.startMinute,
            endMinute = item.endMinute,
            column = item.column,
            totalColumns = totalColumns,
            topDp = 64.dp * (topMinutes / 60f),
            heightDp = 64.dp * (durationMinutes / 60f)
        )
    }
}