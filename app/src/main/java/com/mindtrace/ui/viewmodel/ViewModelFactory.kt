package com.mindtrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindtrace.alarm.AndroidMedicationAlarmScheduler
import com.mindtrace.alarm.AndroidMoodAlarmScheduler
import com.mindtrace.data.repository.MedicationRepository
import com.mindtrace.data.repository.MoodRepository
import com.mindtrace.data.repository.TestDataRepository
import com.mindtrace.util.MindTraceSettingsStore

class ViewModelFactory(
    private val moodRepository: MoodRepository,
    private val medicationRepository: MedicationRepository,
    private val scheduler: AndroidMedicationAlarmScheduler,
    private val moodAlarmScheduler: AndroidMoodAlarmScheduler,
    private val settingsStore: MindTraceSettingsStore,
    private val testDataRepository: TestDataRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        TodayViewModel::class.java -> TodayViewModel(moodRepository, medicationRepository)
        MoodViewModel::class.java -> MoodViewModel(moodRepository, settingsStore, moodAlarmScheduler)
        MedicationViewModel::class.java -> MedicationViewModel(medicationRepository, scheduler, settingsStore)
        SettingsViewModel::class.java -> SettingsViewModel(settingsStore, testDataRepository)
        else -> error("Unbekanntes ViewModel: ${modelClass.name}")
    } as T
}
