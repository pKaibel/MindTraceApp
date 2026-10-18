package com.mindtrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.alarm.MoodAlarmScheduler
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.data.repository.MoodRepository
import com.mindtrace.ui.state.MoodUiState
import com.mindtrace.util.MindTraceSettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class MoodViewModel(
    private val repository: MoodRepository,
    private val settingsStore: MindTraceSettingsStore,
    private val moodAlarmScheduler: MoodAlarmScheduler
) : ViewModel() {
    private val days = MutableStateFlow(7)
    private val moodState = days.flatMapLatest { selected ->
        repository.observeSince(Instant.now().minus(selected.toLong(), ChronoUnit.DAYS)).map { entries ->
            MoodUiState(
                entries = entries,
                days = selected,
                physicalAverage = entries.takeIf { it.isNotEmpty() }?.map { it.physicalWellbeing }?.average(),
                mentalAverage = entries.takeIf { it.isNotEmpty() }?.map { it.mentalWellbeing }?.average()
            )
        }
    }
    val state = combine(moodState, settingsStore.settings) { moodState, settings ->
        moodState.copy(
            moodReminderEnabled = settings.moodReminderEnabled,
            moodReminderTime = settings.moodReminderTime
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MoodUiState())

    fun setDays(value: Int) { days.value = value }

    fun save(physicalWellbeing: Int, mentalWellbeing: Int, note: String?) = viewModelScope.launch {
        runCatching { repository.save(physicalWellbeing, mentalWellbeing, note) }
    }

    fun delete(entry: MoodEntryEntity) = viewModelScope.launch { repository.delete(entry) }

    fun setMoodReminder(enabled: Boolean, time: LocalTime) {
        settingsStore.setMoodReminder(enabled, time)
        if (enabled) moodAlarmScheduler.schedule(time) else moodAlarmScheduler.cancel()
    }
}
