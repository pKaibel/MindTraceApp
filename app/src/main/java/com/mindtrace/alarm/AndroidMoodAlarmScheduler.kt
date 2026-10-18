package com.mindtrace.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.mindtrace.receiver.MedicationAlarmReceiver
import java.time.LocalTime

class AndroidMoodAlarmScheduler(private val context: Context) : MoodAlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(time: LocalTime): Boolean {
        val triggerAt = AlarmTimeCalculator.nextOccurrence(time).toInstant().toEpochMilli()
        val pendingIntent = pendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)
        return try {
            if (canScheduleExactAlarms()) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
            true
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            false
        }
    }

    override fun cancel() {
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, MedicationAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let(alarmManager::cancel)
    }

    private fun pendingIntent(flags: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, MedicationAlarmReceiver::class.java).putExtra(MedicationAlarmReceiver.EXTRA_MOOD_REMINDER, true),
        flags or PendingIntent.FLAG_IMMUTABLE
    )

    private fun canScheduleExactAlarms(): Boolean =
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private companion object {
        const val REQUEST_CODE = 80_001
    }
}
