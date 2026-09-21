package com.mindtrace.alarm

import com.mindtrace.data.local.entity.MedicationReminderEntity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object AlarmTimeCalculator {
    fun nextOccurrence(
        reminderTime: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = clock.zone
    ): ZonedDateTime {
        val now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId)
        val today = LocalDate.now(clock.withZone(zoneId))
        val candidate = ZonedDateTime.of(today, reminderTime, zoneId)
        return if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
    }

    fun nextOccurrence(
        reminder: MedicationReminderEntity,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = clock.zone
    ): ZonedDateTime {
        val now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId)
        val today = LocalDate.now(clock.withZone(zoneId))
        val date = generateSequence(today) { it.plusDays(1) }
            .first { candidateDate ->
                reminder.occursOn(candidateDate) && ZonedDateTime.of(candidateDate, reminder.reminderTime, zoneId).isAfter(now)
            }
        return ZonedDateTime.of(date, reminder.reminderTime, zoneId)
    }

    fun scheduledInstantFor(date: LocalDate, reminderTime: LocalTime, zoneId: ZoneId = ZoneId.systemDefault()): Instant =
        ZonedDateTime.of(date, reminderTime, zoneId).toInstant()
}
