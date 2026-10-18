package com.mindtrace.data

import android.content.Context
import com.mindtrace.alarm.AndroidMedicationAlarmScheduler
import com.mindtrace.alarm.AndroidMoodAlarmScheduler
import com.mindtrace.data.local.MindTraceDatabase
import com.mindtrace.data.repository.MedicationRepository
import com.mindtrace.data.repository.MoodRepository
import com.mindtrace.data.repository.TestDataRepository
import com.mindtrace.util.MindTraceSettingsStore

class AppContainer(context: Context) {
    private val database = MindTraceDatabase.create(context)
    val settingsStore = MindTraceSettingsStore(context)
    val alarmScheduler = AndroidMedicationAlarmScheduler(context)
    val moodAlarmScheduler = AndroidMoodAlarmScheduler(context)
    val moodRepository = MoodRepository(database.moodEntryDao())
    val medicationRepository = MedicationRepository(
        reminderDao = database.medicationReminderDao(),
        intakeDao = database.medicationIntakeDao(),
        alarmScheduler = alarmScheduler
    )
    val testDataRepository = TestDataRepository(database, alarmScheduler)
}
