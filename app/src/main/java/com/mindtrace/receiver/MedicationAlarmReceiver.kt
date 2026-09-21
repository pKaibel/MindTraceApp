package com.mindtrace.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindtrace.MindTraceApp
import com.mindtrace.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = (context.applicationContext as MindTraceApp).container.medicationRepository
                val reminder = repo.getReminder(reminderId)
                if (reminder != null && reminder.enabled) {
                    NotificationHelper.showMedicationReminder(context, reminder)
                    repo.rescheduleEnabled()
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
