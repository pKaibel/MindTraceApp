package com.mindtrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.alarm.AndroidMedicationAlarmScheduler
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.repository.MedicationRepository
import com.mindtrace.domain.model.MedicationRecurrenceType
import com.mindtrace.domain.model.MedicationSkipReason
import com.mindtrace.ui.state.MedicationStatisticsPeriod
import com.mindtrace.ui.state.MedicationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.LocalTime

class MedicationViewModel(
    private val repository: MedicationRepository,
    private val scheduler: AndroidMedicationAlarmScheduler
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)
    private val statisticsPeriod = MutableStateFlow(MedicationStatisticsPeriod.TODAY)
    private val statisticsIntakes = statisticsPeriod.flatMapLatest { period ->
        val today = LocalDate.now()
        repository.observeIntakes(period.startDate(today), today)
    }
    private val remindersAndTodayIntakes = combine(
        repository.observeReminders(),
        repository.observeIntakes(LocalDate.now())
    ) { reminders, intakesToday -> reminders to intakesToday }
    val state = combine(
        remindersAndTodayIntakes,
        statisticsPeriod,
        statisticsIntakes,
        message
    ) { remindersAndIntakes, period, intakesInPeriod, msg ->
        val (reminders, intakesToday) = remindersAndIntakes
        MedicationUiState(
            reminders = reminders,
            intakesToday = intakesToday,
            statisticsPeriod = period,
            intakesInStatisticsPeriod = intakesInPeriod,
            message = msg,
            exactAlarmAllowed = scheduler.canScheduleExactAlarms(),
            notificationsAllowed = scheduler.canPostNotifications()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicationUiState())

    fun create(
        name: String,
        dosage: String?,
        times: List<LocalTime>,
        enabled: Boolean,
        recurrenceType: MedicationRecurrenceType,
        weekdays: Set<DayOfWeek>,
        intervalDays: Int?
    ) = viewModelScope.launch {
        runCatching { repository.create(name, dosage, times, enabled, recurrenceType, weekdays, intervalDays) }
            .onSuccess {
                message.value = if (times.distinct().size == 1) {
                    "Erinnerung wurde gespeichert."
                } else {
                    "${times.distinct().size} Erinnerungen wurden gespeichert."
                }
            }
            .onFailure { message.value = it.message ?: "Erinnerung konnte nicht gespeichert werden." }
    }

    fun update(reminder: MedicationReminderEntity) = viewModelScope.launch { repository.update(reminder) }
    fun delete(reminder: MedicationReminderEntity) = viewModelScope.launch { repository.delete(reminder) }
    fun setEnabled(reminder: MedicationReminderEntity, enabled: Boolean) = viewModelScope.launch { repository.setEnabled(reminder, enabled) }
    fun setStatisticsPeriod(period: MedicationStatisticsPeriod) {
        statisticsPeriod.value = period
    }
    fun markTaken(reminderId: Long) = viewModelScope.launch { repository.markTaken(reminderId) }
    fun markSkipped(reminderId: Long, reason: MedicationSkipReason, note: String?) = viewModelScope.launch {
        repository.markSkipped(reminderId, reason, note)
    }
}

private fun MedicationStatisticsPeriod.startDate(today: LocalDate): LocalDate = when (this) {
    MedicationStatisticsPeriod.TODAY -> today
    MedicationStatisticsPeriod.WEEK -> today.minusDays(6)
    MedicationStatisticsPeriod.MONTH -> today.minusDays(29)
}
