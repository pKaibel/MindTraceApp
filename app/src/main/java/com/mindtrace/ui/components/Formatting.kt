package com.mindtrace.ui.components

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.mindtrace.ui.i18n.MindTraceStrings

private val german = Locale.GERMANY
private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(german)
private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.", german)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", german)

fun LocalDate.formatFullDate(): String = format(dateFormatter)
fun LocalDate.formatShortDate(): String = format(shortDateFormatter)
fun LocalTime.formatTime(): String = format(timeFormatter)
fun Instant.formatDateTime(): String = atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", german))
fun moodLabel(value: Int): String = when (value) {
    1 -> "Sehr schlecht"
    2 -> "Schlecht"
    3 -> "Neutral"
    4 -> "Gut"
    else -> "Sehr gut"
}

fun moodLabel(value: Int, strings: MindTraceStrings): String = when (value) {
    1 -> strings.veryBad
    2 -> strings.bad
    3 -> strings.neutral
    4 -> strings.good
    else -> strings.veryGood
}

fun moodSymbol(value: Int): String = when (value) {
    1 -> ":("
    2 -> ":/"
    3 -> ":|"
    4 -> ":)"
    else -> ":D"
}
