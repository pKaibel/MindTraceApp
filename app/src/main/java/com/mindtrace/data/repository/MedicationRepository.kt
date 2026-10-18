package com.mindtrace.data.repository

import com.mindtrace.alarm.AlarmTimeCalculator
import com.mindtrace.alarm.MedicationAlarmScheduler
import com.mindtrace.data.local.dao.MedicationIntakeDao
import com.mindtrace.data.local.dao.MedicationReminderDao
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.domain.model.MedicationRecurrenceType
import com.mindtrace.domain.model.MedicationSkipReason
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class MedicationRepository(
    private val reminderDao: MedicationReminderDao,
    private val intakeDao: MedicationIntakeDao,
    private val alarmScheduler: MedicationAlarmScheduler
) {
    fun observeReminders(): Flow<List<MedicationReminderEntity>> = reminderDao.observeAll()
    fun observeIntakes(date: LocalDate): Flow<List<com.mindtrace.data.local.entity.MedicationIntakeEntity>> = intakeDao.observeForDate(date)
    fun observeIntakes(startDate: LocalDate, endDate: LocalDate): Flow<List<com.mindtrace.data.local.entity.MedicationIntakeEntity>> =
        intakeDao.observeForDateRange(startDate, endDate)

    suspend fun create(
        name: String,
        dosage: String?,
        times: List<LocalTime>,
        enabled: Boolean,
        recurrenceType: MedicationRecurrenceType,
        weekdays: Set<DayOfWeek>,
        intervalDays: Int?
    ) {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "Bitte gib einen Medikamentennamen ein." }
        val uniqueTimes = times.distinct().sorted()
        require(uniqueTimes.isNotEmpty()) { "Bitte gib mindestens eine Uhrzeit ein." }
        require(uniqueTimes.size <= MAX_REMINDERS_PER_MEDICATION) { "Es sind maximal 4 Erinnerungen pro Medikament möglich." }
        require(recurrenceType != MedicationRecurrenceType.WEEKDAYS || weekdays.isNotEmpty()) { "Bitte wähle mindestens einen Wochentag aus." }
        require(recurrenceType != MedicationRecurrenceType.INTERVAL || (intervalDays ?: 0) > 0) { "Bitte gib ein gültiges Intervall ein." }
        val recurrenceWeekdays = weekdays.sortedBy { it.value }.joinToString(",") { it.value.toString() }.ifBlank { null }
        val recurrenceStartDate = if (recurrenceType == MedicationRecurrenceType.INTERVAL) LocalDate.now() else null
        val reminders = uniqueTimes.map { time ->
            MedicationReminderEntity(
                medicationName = cleanName,
                dosage = dosage?.trim().takeUnless { it.isNullOrBlank() },
                reminderTime = time,
                enabled = enabled,
                createdAt = Instant.now(),
                recurrenceType = recurrenceType,
                recurrenceWeekdays = recurrenceWeekdays,
                recurrenceIntervalDays = intervalDays?.takeIf { recurrenceType == MedicationRecurrenceType.INTERVAL },
                recurrenceStartDate = recurrenceStartDate
            )
        }
        val ids = reminderDao.insertAll(reminders)
        if (enabled) ids.forEach { id -> reminderDao.getById(id)?.let(alarmScheduler::schedule) }
    }

    suspend fun update(reminder: MedicationReminderEntity) {
        reminderDao.update(reminder)
        alarmScheduler.cancel(reminder.id)
        if (reminder.enabled) alarmScheduler.schedule(reminder)
    }

    suspend fun delete(reminder: MedicationReminderEntity) {
        alarmScheduler.cancel(reminder.id)
        reminderDao.delete(reminder)
    }

    suspend fun setEnabled(reminder: MedicationReminderEntity, enabled: Boolean) =
        update(reminder.copy(enabled = enabled))

    suspend fun markTaken(reminderId: Long, scheduledDate: LocalDate = LocalDate.now()) {
        val reminder = reminderDao.getById(reminderId) ?: return
        if (!reminder.occursOn(scheduledDate)) return
        val scheduled = AlarmTimeCalculator.scheduledInstantFor(scheduledDate, reminder.reminderTime, ZoneId.systemDefault())
        intakeDao.markTaken(reminderId, scheduledDate, scheduled, Instant.now())
    }

    suspend fun markSkipped(
        reminderId: Long,
        reason: MedicationSkipReason,
        note: String?,
        scheduledDate: LocalDate = LocalDate.now()
    ) {
        val reminder = reminderDao.getById(reminderId) ?: return
        if (!reminder.occursOn(scheduledDate)) return
        val scheduled = AlarmTimeCalculator.scheduledInstantFor(scheduledDate, reminder.reminderTime, ZoneId.systemDefault())
        val cleanedNote = note?.trim().takeUnless { it.isNullOrEmpty() }
            ?.takeIf { reason == MedicationSkipReason.OTHER }
        intakeDao.markSkipped(reminderId, scheduledDate, scheduled, Instant.now(), reason, cleanedNote)
    }

    suspend fun rescheduleEnabled() {
        reminderDao.enabledReminders().forEach(alarmScheduler::schedule)
    }

    suspend fun getReminder(id: Long): MedicationReminderEntity? = reminderDao.getById(id)
}

private const val MAX_REMINDERS_PER_MEDICATION = 4
