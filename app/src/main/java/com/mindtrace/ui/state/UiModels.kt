package com.mindtrace.ui.state

import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.local.entity.MoodEntryEntity
import java.time.LocalTime

data class TodayUiState(
    val reminders: List<MedicationReminderEntity> = emptyList(),
    val intakes: List<MedicationIntakeEntity> = emptyList(),
    val latestMood: MoodEntryEntity? = null,
    val message: String? = null
)

data class MoodUiState(
    val entries: List<MoodEntryEntity> = emptyList(),
    val days: Int = 7,
    val physicalAverage: Double? = null,
    val mentalAverage: Double? = null,
    val moodReminderEnabled: Boolean = false,
    val moodReminderTime: LocalTime = LocalTime.of(20, 0),
    val message: String? = null
)

data class MedicationUiState(
    val reminders: List<MedicationReminderEntity> = emptyList(),
    val intakesToday: List<MedicationIntakeEntity> = emptyList(),
    val statisticsPeriod: MedicationStatisticsPeriod = MedicationStatisticsPeriod.TODAY,
    val intakesInStatisticsPeriod: List<MedicationIntakeEntity> = emptyList(),
    val message: String? = null,
    val exactAlarmAllowed: Boolean = true,
    val notificationsAllowed: Boolean = true
)

enum class MedicationStatisticsPeriod {
    TODAY,
    WEEK,
    MONTH
}
