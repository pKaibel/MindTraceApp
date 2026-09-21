package com.mindtrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.data.repository.MoodRepository
import com.mindtrace.ui.state.MoodUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class MoodViewModel(private val repository: MoodRepository) : ViewModel() {
    private val days = MutableStateFlow(7)
    val state = days.flatMapLatest { selected ->
        repository.observeSince(Instant.now().minus(selected.toLong(), ChronoUnit.DAYS)).map { entries ->
            MoodUiState(entries = entries, days = selected, average = entries.takeIf { it.isNotEmpty() }?.map { it.moodValue }?.average())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MoodUiState())

    fun setDays(value: Int) { days.value = value }

    fun save(value: Int, note: String?) = viewModelScope.launch {
        runCatching { repository.save(value, note) }
    }

    fun delete(entry: MoodEntryEntity) = viewModelScope.launch { repository.delete(entry) }
}
