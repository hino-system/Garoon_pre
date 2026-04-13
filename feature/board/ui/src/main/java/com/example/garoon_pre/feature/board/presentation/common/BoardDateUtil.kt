package com.example.garoon_pre.feature.board.presentation.common

import com.example.core.common.data.calendarFromApiDateTime
import com.example.core.common.data.currentCalendar
import com.example.core.common.data.formatApiDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val tokyoTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Tokyo")
private val displayDateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN).apply {
    timeZone = tokyoTimeZone
}

fun formatBoardDateTime(value: String): String {
    val parsed = calendarFromApiDateTime(value)?.time ?: return value
    return displayDateTimeFormat.format(parsed)
}

fun formatBoardPeriod(startAt: String, endAt: String): String {
    return "${formatBoardDateTime(startAt)} 〜 ${formatBoardDateTime(endAt)}"
}

fun boardStatusLabel(status: String): String {
    return when (status) {
        "active" -> "掲載中"
        "upcoming" -> "開始前"
        "expired" -> "終了"
        else -> "不明"
    }
}

fun defaultBoardStartAt(): String {
    val calendar = currentCalendar().apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return formatApiDateTime(calendar)
}

fun defaultBoardEndAt(): String {
    val calendar = currentCalendar().apply {
        add(Calendar.DAY_OF_MONTH, 7)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return formatApiDateTime(calendar)
}

fun isValidBoardDateTime(value: String): Boolean {
    return calendarFromApiDateTime(value) != null
}

fun isBoardEndAfterStart(startAt: String, endAt: String): Boolean {
    val start = calendarFromApiDateTime(startAt)?.timeInMillis ?: return false
    val end = calendarFromApiDateTime(endAt)?.timeInMillis ?: return false
    return end > start
}