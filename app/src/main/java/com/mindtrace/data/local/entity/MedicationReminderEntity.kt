package com.mindtrace.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.mindtrace.domain.model.MedicationRecurrenceType
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Entity(tableName = "medication_reminders")
data class MedicationReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationName: String,
    val dosage: String?,
    val reminderTime: LocalTime,
    val enabled: Boolean,
    val createdAt: Instant,
    @ColumnInfo(defaultValue = "'DAILY'")
    val recurrenceType: MedicationRecurrenceType = MedicationRecurrenceType.DAILY,
    val recurrenceWeekdays: String? = null,
    val recurrenceIntervalDays: Int? = null,
    val recurrenceStartDate: LocalDate? = null
) {
    init {
        require(medicationName.isNotBlank()) { "Medikamentenname darf nicht leer sein." }
    }

    fun occursOn(date: LocalDate): Boolean = when (recurrenceType) {
        MedicationRecurrenceType.DAILY -> true
        MedicationRecurrenceType.WEEKDAYS -> date.dayOfWeek in selectedWeekdays()
        MedicationRecurrenceType.INTERVAL -> {
            val startDate = recurrenceStartDate ?: return false
            val interval = recurrenceIntervalDays ?: return false
            !date.isBefore(startDate) && interval > 0 && ChronoUnit.DAYS.between(startDate, date) % interval == 0L
        }
    }

    fun selectedWeekdays(): Set<DayOfWeek> = recurrenceWeekdays
        ?.split(',')
        ?.mapNotNull { value -> runCatching { DayOfWeek.of(value.toInt()) }.getOrNull() }
        ?.toSet()
        .orEmpty()
}
