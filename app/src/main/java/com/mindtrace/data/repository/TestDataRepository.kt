package com.mindtrace.data.repository

import com.mindtrace.alarm.AlarmTimeCalculator
import com.mindtrace.alarm.MedicationAlarmScheduler
import com.mindtrace.data.local.MindTraceDatabase
import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.domain.model.IntakeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TestDataRepository(
    private val database: MindTraceDatabase,
    private val alarmScheduler: MedicationAlarmScheduler
) {
    suspend fun addTestData() {
        val moodDao = database.moodEntryDao()
        val reminderDao = database.medicationReminderDao()
        val intakeDao = database.medicationIntakeDao()
        val now = Instant.now()
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        val moods = listOf(
            4 to "Gut geschlafen, ruhiger Start.",
            3 to "Etwas angespannt vor dem Termin.",
            5 to "Spaziergang hat geholfen.",
            2 to "Unruhiger Abend.",
            4 to "Konzentrierter Arbeitstag.",
            3 to null,
            5 to "Schöner Besuch am Nachmittag."
        )
        moods.forEachIndexed { index, (value, note) ->
            moodDao.insert(
                MoodEntryEntity(
                    moodValue = value,
                    note = note,
                    recordedAt = now.minusSeconds((index * 28L + 3L) * 60L * 60L)
                )
            )
        }

        val morningId = reminderDao.insert(
            MedicationReminderEntity(
                medicationName = "Vitamin D",
                dosage = "1000 IE",
                reminderTime = LocalTime.of(8, 0),
                enabled = true,
                createdAt = now
            )
        )
        val eveningId = reminderDao.insert(
            MedicationReminderEntity(
                medicationName = "Magnesium",
                dosage = "1 Tablette",
                reminderTime = LocalTime.of(20, 30),
                enabled = true,
                createdAt = now
            )
        )
        reminderDao.insert(
            MedicationReminderEntity(
                medicationName = "Bedarfsmedikation",
                dosage = null,
                reminderTime = LocalTime.of(13, 15),
                enabled = false,
                createdAt = now
            )
        )

        intakeDao.insertIgnore(
            MedicationIntakeEntity(
                reminderId = morningId,
                scheduledDate = today,
                scheduledDateTime = AlarmTimeCalculator.scheduledInstantFor(today, LocalTime.of(8, 0), zoneId),
                status = IntakeStatus.TAKEN,
                completedAt = now.minusSeconds(60L * 45L)
            )
        )
        intakeDao.insertIgnore(
            MedicationIntakeEntity(
                reminderId = eveningId,
                scheduledDate = today,
                scheduledDateTime = AlarmTimeCalculator.scheduledInstantFor(today, LocalTime.of(20, 30), zoneId),
                status = IntakeStatus.PENDING,
                completedAt = null
            )
        )

        reminderDao.getById(morningId)?.let(alarmScheduler::schedule)
        reminderDao.getById(eveningId)?.let(alarmScheduler::schedule)
    }

    suspend fun resetAllData() {
        withContext(Dispatchers.IO) {
            database.medicationReminderDao().enabledReminders().forEach { alarmScheduler.cancel(it.id) }
            database.clearAllTables()
        }
    }
}
