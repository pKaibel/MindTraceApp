package com.mindtrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.data.repository.MedicationRepository
import com.mindtrace.data.repository.MoodRepository
import com.mindtrace.domain.model.MedicationSkipReason
import com.mindtrace.ui.state.TodayUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(
    private val moodRepository: MoodRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)
    val state = combine(
        medicationRepository.observeReminders(),
        medicationRepository.observeIntakes(LocalDate.now()),
        moodRepository.observeAll(),
        message
    ) { reminders, intakes, moods, msg ->
        TodayUiState(
            reminders = reminders.filter { it.occursOn(LocalDate.now()) }.sortedBy { it.reminderTime },
            intakes = intakes,
            latestMood = moods.firstOrNull(),
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun markTaken(reminderId: Long) = viewModelScope.launch {
        medicationRepository.markTaken(reminderId)
        message.value = "Einnahme wurde gespeichert."
    }

    fun markSkipped(reminderId: Long, reason: MedicationSkipReason, note: String?) = viewModelScope.launch {
        medicationRepository.markSkipped(reminderId, reason, note)
        message.value = "Nicht eingenommene Tablette wurde gespeichert."
    }
}
