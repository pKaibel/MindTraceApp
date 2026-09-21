package com.mindtrace.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindtrace.MindTraceApp
import com.mindtrace.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MedicationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_TAKEN) return
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = (context.applicationContext as MindTraceApp).container.medicationRepository
                repo.markTaken(reminderId, LocalDate.now())
                NotificationHelper.dismiss(context, reminderId)
                repo.getReminder(reminderId)?.takeIf { it.enabled }?.let {
                    (context.applicationContext as MindTraceApp).container.alarmScheduler.schedule(it)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
