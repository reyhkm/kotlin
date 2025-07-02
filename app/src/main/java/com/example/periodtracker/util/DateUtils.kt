package com.example.periodtracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun daysBetween(start: Long, end: Long): Long {
        val diff = end - start
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }
}