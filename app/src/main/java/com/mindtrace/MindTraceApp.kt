package com.mindtrace

import android.app.Application
import com.mindtrace.data.AppContainer
import com.mindtrace.notification.NotificationHelper

class MindTraceApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)
        val settings = container.settingsStore.read()
        if (settings.moodReminderEnabled) {
            container.moodAlarmScheduler.schedule(settings.moodReminderTime)
        }
    }
}
