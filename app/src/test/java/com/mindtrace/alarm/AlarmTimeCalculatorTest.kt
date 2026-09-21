package com.mindtrace.alarm

import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.domain.model.MedicationRecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class AlarmTimeCalculatorTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    @Test
    fun nextOccurrenceUsesTodayWhenTimeIsStillAhead() {
        val clock = Clock.fixed(Instant.parse("2026-07-31T08:00:00Z"), berlin)
        val next = AlarmTimeCalculator.nextOccurrence(LocalTime.of(12, 0), clock, berlin)
        assertEquals("2026-07-31T12:00+02:00[Europe/Berlin]", next.toString())
    }

    @Test
    fun nextOccurrenceMovesAcrossDayBoundary() {
        val clock = Clock.fixed(Instant.parse("2026-07-31T21:30:00Z"), berlin)
        val next = AlarmTimeCalculator.nextOccurrence(LocalTime.of(8, 0), clock, berlin)
        assertEquals("2026-08-01T08:00+02:00[Europe/Berlin]", next.toString())
    }

    @Test
    fun nextOccurrenceKeepsLocalTimeOverWinterDstChange() {
        val clock = Clock.fixed(Instant.parse("2026-10-24T22:30:00Z"), berlin)
        val next = AlarmTimeCalculator.nextOccurrence(LocalTime.of(8, 0), clock, berlin)
        assertEquals("2026-10-25T08:00+01:00[Europe/Berlin]", next.toString())
    }

    @Test
    fun nextOccurrenceKeepsLocalTimeOverSummerDstChange() {
        val clock = Clock.fixed(Instant.parse("2026-03-28T22:30:00Z"), berlin)
        val next = AlarmTimeCalculator.nextOccurrence(LocalTime.of(8, 0), clock, berlin)
        assertEquals("2026-03-29T08:00+02:00[Europe/Berlin]", next.toString())
    }

    @Test
    fun nextOccurrenceSkipsToTheNextSelectedWeekday() {
        val reminder = MedicationReminderEntity(
            medicationName = "Test",
            dosage = null,
            reminderTime = LocalTime.of(8, 0),
            enabled = true,
            createdAt = Instant.now(),
            recurrenceType = MedicationRecurrenceType.WEEKDAYS,
            recurrenceWeekdays = "1,3"
        )
        val clock = Clock.fixed(Instant.parse("2026-07-31T08:00:00Z"), berlin)

        val next = AlarmTimeCalculator.nextOccurrence(reminder, clock, berlin)

        assertEquals("2026-08-03T08:00+02:00[Europe/Berlin]", next.toString())
    }

    @Test
    fun intervalReminderOnlyOccursOnItsConfiguredDays() {
        val reminder = MedicationReminderEntity(
            medicationName = "Test",
            dosage = null,
            reminderTime = LocalTime.of(8, 0),
            enabled = true,
            createdAt = Instant.now(),
            recurrenceType = MedicationRecurrenceType.INTERVAL,
            recurrenceIntervalDays = 3,
            recurrenceStartDate = java.time.LocalDate.of(2026, 7, 30)
        )

        assertEquals(true, reminder.occursOn(java.time.LocalDate.of(2026, 8, 2)))
        assertEquals(false, reminder.occursOn(java.time.LocalDate.of(2026, 8, 1)))
    }
}
