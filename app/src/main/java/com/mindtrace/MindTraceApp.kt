package com.mindtrace

import android.app.Application
import com.mindtrace.data.AppContainer
import com.mindtrace.notification.NotificationHelper

class MindTraceApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createMedicationChannel(this)
        container = AppContainer(this)
    }
}
