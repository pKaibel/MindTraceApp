package com.mindtrace.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindtrace.MindTraceApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (context.applicationContext as MindTraceApp).container.medicationRepository.rescheduleEnabled()
            } finally {
                pending.finish()
            }
        }
    }
}
