package com.example.core.common.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val API_DATE_PATTERN = "yyyyMMdd"
private const val API_DATE_TIME_PATTERN = "yyyyMMddHHmm"
private const val LEGACY_DATE_PATTERN = "yyyy-MM-dd"
private const val LEGACY_DATE_TIME_PATTERN_1 = "yyyy-MM-dd'T'HH:mm:ss.SSSX"
private const val LEGACY_DATE_TIME_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ssXXX"

private val tokyoTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Tokyo")

private fun compactDateFormat(): SimpleDateFormat {
    return SimpleDateFormat(API_DATE_PATTERN, Locale.JAPAN).apply {
        isLenient = false
        timeZone = tokyoTimeZone
    }
}

private fun compactDateTimeFormat(): SimpleDateFormat {
    return SimpleDateFormat(API_DATE_TIME_PATTERN, Locale.JAPAN).apply {
        isLenient = false
        timeZone = tokyoTimeZone
    }
}

private fun legacyDateFormat(): SimpleDateFormat {
    return SimpleDateFormat(LEGACY_DATE_PATTERN, Locale.JAPAN).apply {
        isLenient = false
        timeZone = tokyoTimeZone
    }
}

private fun legacyDateTimeFormats(): List<SimpleDateFormat> {
    return listOf(
        SimpleDateFormat(LEGACY_DATE_TIME_PATTERN_1, Locale.JAPAN).apply {
            isLenient = false
            timeZone = tokyoTimeZone
        },
        SimpleDateFormat(LEGACY_DATE_TIME_PATTERN_2, Locale.JAPAN).apply {
            isLenient = false
            timeZone = tokyoTimeZone
        }
    )
}

private fun headerDateFormat(): SimpleDateFormat {
    return SimpleDateFormat("M月d日(E)", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }
}

private fun monthDateFormat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy年M月", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }
}

private fun dayOfMonthFormat(): SimpleDateFormat {
    return SimpleDateFormat("d", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }
}

private fun dayOfWeekFormat(): SimpleDateFormat {
    return SimpleDateFormat("E", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }
}

private fun displayDateTimeFormat(): SimpleDateFormat {
    return SimpleDateFormat("M月d日 HH:mm", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }
}

private fun timeTextFormat(): SimpleDateFormat {
    return SimpleDateFormat("HH:mm", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }
}

private fun parseCompactDateOrNull(value: String): Date? {
    val normalized = value.trim()
    if (!normalized.matches(Regex("""\d{8}"""))) return null
    return runCatching { compactDateFormat().parse(normalized) }.getOrNull()
}

private fun parseLegacyDateOrNull(value: String): Date? {
    val normalized = value.trim()
    if (!normalized.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) return null
    return runCatching { legacyDateFormat().parse(normalized) }.getOrNull()
}

private fun parseCompactDateTimeOrNull(value: String): Date? {
    val normalized = value.trim()
    if (!normalized.matches(Regex("""\d{12}"""))) return null
    return runCatching { compactDateTimeFormat().parse(normalized) }.getOrNull()
}

private fun parseLegacyDateTimeOrNull(value: String): Date? {
    val normalized = value.trim()
    return legacyDateTimeFormats().firstNotNullOfOrNull { format ->
        runCatching { format.parse(normalized) }.getOrNull()
    }
}

private fun parseDateOrNull(value: String): Date? {
    return parseCompactDateOrNull(value) ?: parseLegacyDateOrNull(value)
}

private fun parseDateTimeOrNull(value: String): Date? {
    return parseCompactDateTimeOrNull(value) ?: parseLegacyDateTimeOrNull(value)
}

private fun currentDateStringInternal(): String {
    return compactDateFormat().format(Date())
}

fun nextSevenDateStrings(): List<String> {
    val calendar = Calendar.getInstance(tokyoTimeZone, Locale.JAPAN)
    return (0 until 7).map { offset ->
        val cloned = calendar.clone() as Calendar
        cloned.add(Calendar.DAY_OF_MONTH, offset)
        compactDateFormat().format(cloned.time)
    }
}

fun toWeekHeaderLabel(date: String): String {
    val parsed = parseDateOrNull(date) ?: return date
    return headerDateFormat().format(parsed)
}

fun toTimeText(value: String): String {
    val parsed = parseDateTimeOrNull(value) ?: return value
    return timeTextFormat().format(parsed)
}

fun currentWeekStartDateString(): String {
    return weekStartDateString(currentDateStringInternal())
}

fun weekStartDateString(date: String): String {
    val parsed = parseDateOrNull(date) ?: return date
    val calendar = Calendar.getInstance(tokyoTimeZone, Locale.JAPAN).apply {
        time = parsed
        firstDayOfWeek = Calendar.SUNDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }
    return compactDateFormat().format(calendar.time)
}

fun weekDatesFrom(weekStartDate: String): List<String> {
    val base = parseDateOrNull(weekStartDate) ?: return listOf(weekStartDate)
    val calendar = Calendar.getInstance(tokyoTimeZone, Locale.JAPAN).apply {
        time = base
    }
    return (0 until 7).map { offset ->
        val cloned = calendar.clone() as Calendar
        cloned.add(Calendar.DAY_OF_MONTH, offset)
        compactDateFormat().format(cloned.time)
    }
}

fun shiftWeekDate(weekStartDate: String, weekOffset: Int): String {
    val base = parseDateOrNull(weekStartDate) ?: return weekStartDate
    val calendar = Calendar.getInstance(tokyoTimeZone, Locale.JAPAN).apply {
        time = base
        add(Calendar.WEEK_OF_YEAR, weekOffset)
    }
    return compactDateFormat().format(calendar.time)
}

fun toMonthLabel(date: String): String {
    val parsed = parseDateOrNull(date) ?: return date
    return monthDateFormat().format(parsed)
}

fun toDayOfMonthLabel(date: String): String {
    val parsed = parseDateOrNull(date) ?: return date
    return dayOfMonthFormat().format(parsed)
}

fun toDayOfWeekLabel(date: String): String {
    val parsed = parseDateOrNull(date) ?: return date
    return dayOfWeekFormat().format(parsed)
}

fun isToday(date: String): Boolean {
    return currentDateStringInternal() == date
}

fun isValidApiDateTime(value: String): Boolean {
    return calendarFromApiDateTime(value) != null
}

fun currentCalendar(): Calendar {
    return Calendar.getInstance(tokyoTimeZone, Locale.JAPAN)
}

fun calendarFromApiDateTime(value: String): Calendar? {
    val parsed = parseDateTimeOrNull(value) ?: return null
    return Calendar.getInstance(tokyoTimeZone, Locale.JAPAN).apply {
        time = parsed
    }
}

fun formatApiDateTime(calendar: Calendar): String {
    return compactDateTimeFormat().format(calendar.time)
}

fun formatPickerDateTime(value: String): String {
    val parsed = parseDateTimeOrNull(value) ?: return value
    return displayDateTimeFormat().format(parsed)
}

fun plusMinutes(value: String, minutes: Int): String {
    val calendar = calendarFromApiDateTime(value) ?: return value
    calendar.add(Calendar.MINUTE, minutes)
    return formatApiDateTime(calendar)
}

fun isEndAfterStart(startAt: String, endAt: String): Boolean {
    val start = calendarFromApiDateTime(startAt)?.timeInMillis ?: return false
    val end = calendarFromApiDateTime(endAt)?.timeInMillis ?: return false
    return end > start
}

fun minuteOfDay(dateTime: String): Int? {
    val calendar = calendarFromApiDateTime(dateTime) ?: return null
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

private fun roundedStartCalendar(): Calendar {
    return currentCalendar().apply {
        val minute = get(Calendar.MINUTE)
        when {
            minute == 0 -> Unit
            minute <= 30 -> set(Calendar.MINUTE, 30)
            else -> {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
            }
        }
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

fun defaultStartAt(): String {
    return formatApiDateTime(roundedStartCalendar())
}

fun defaultEndAt(): String {
    val calendar = roundedStartCalendar().apply {
        add(Calendar.MINUTE, 30)
    }
    return formatApiDateTime(calendar)
}