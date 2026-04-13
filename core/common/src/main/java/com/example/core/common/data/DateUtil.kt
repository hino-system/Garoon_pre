package com.example.core.common.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val tokyoTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Tokyo")

fun todayString(): String {
    return SimpleDateFormat("yyyyMMdd", Locale.JAPAN).apply {
        timeZone = tokyoTimeZone
    }.format(Date())
}