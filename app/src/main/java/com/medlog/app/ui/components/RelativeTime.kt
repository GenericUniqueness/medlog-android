package com.medlog.app.ui.components

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

fun formatRelativeTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(dateTime, now)

    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
        duration.toHours() < 24 -> "${duration.toHours()}h ago"
        duration.toDays() < 7 -> "${duration.toDays()}d ago"
        else -> dateTime.format(shortDateFormatter)
    }
}

fun formatRelativeDate(date: LocalDate): String {
    val today = LocalDate.now()
    val period = Period.between(date, today)

    return when {
        period.days == 0 -> "Today"
        period.days == 1 -> "Yesterday"
        period.days < 7 -> "${period.days} days ago"
        period.months == 0 -> "${period.days / 7} weeks ago"
        else -> date.format(shortDateFormatter)
    }
}
