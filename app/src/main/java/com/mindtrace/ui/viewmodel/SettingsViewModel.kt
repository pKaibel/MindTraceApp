package com.mindtrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.data.repository.TestDataRepository
import com.mindtrace.util.LanguageMode
import com.mindtrace.util.MindTraceSettingsStore
import com.mindtrace.util.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: MindTraceSettingsStore,
    private val testDataRepository: TestDataRepository
) : ViewModel() {
    val settings = settingsStore.settings
    private val messageFlow = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = messageFlow

    fun setNotificationsEnabled(enabled: Boolean) = settingsStore.setNotificationsEnabled(enabled)
    fun setSoundEnabled(enabled: Boolean) = settingsStore.setSoundEnabled(enabled)
    fun setVibrationEnabled(enabled: Boolean) = settingsStore.setVibrationEnabled(enabled)
    fun setThemeMode(mode: ThemeMode) = settingsStore.setThemeMode(mode)
    fun setLanguageMode(mode: LanguageMode) = settingsStore.setLanguageMode(mode)

    fun addTestData() = viewModelScope.launch {
        val language = settingsStore.read().languageMode
        runCatching { testDataRepository.addTestData() }
            .onSuccess {
                messageFlow.value = if (language == LanguageMode.EN) "Test data was added." else "Testdaten wurden hinzugefügt."
            }
            .onFailure {
                messageFlow.value = if (language == LanguageMode.EN) "Test data could not be added." else "Testdaten konnten nicht hinzugefügt werden."
            }
    }

    fun resetAllData() = viewModelScope.launch {
        val language = settingsStore.read().languageMode
        runCatching { testDataRepository.resetAllData() }
            .onSuccess {
                messageFlow.value = if (language == LanguageMode.EN) "All data was reset." else "Alle Daten wurden zurückgesetzt."
            }
            .onFailure {
                messageFlow.value = if (language == LanguageMode.EN) "Data could not be reset." else "Daten konnten nicht zurückgesetzt werden."
            }
    }
}
