package com.mindtrace.ui.state

import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.local.entity.MoodEntryEntity

data class TodayUiState(
    val reminders: List<MedicationReminderEntity> = emptyList(),
    val intakes: List<MedicationIntakeEntity> = emptyList(),
    val latestMood: MoodEntryEntity? = null,
    val message: String? = null
)

data class MoodUiState(
    val entries: List<MoodEntryEntity> = emptyList(),
    val days: Int = 7,
    val average: Double? = null,
    val message: String? = null
)

data class MedicationUiState(
    val reminders: List<MedicationReminderEntity> = emptyList(),
    val intakesToday: List<MedicationIntakeEntity> = emptyList(),
    val message: String? = null,
    val exactAlarmAllowed: Boolean = true,
    val notificationsAllowed: Boolean = true
)
