package com.mindtrace.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mindtrace.MainActivity
import com.mindtrace.R
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.receiver.MedicationActionReceiver
import com.mindtrace.util.LanguageMode
import com.mindtrace.util.MindTraceSettingsStore

object NotificationHelper {
    const val CHANNEL_ID = "medication_reminders"
    private const val CHANNEL_SOUND_VIBRATION = "medication_reminders_sound_vibration"
    private const val CHANNEL_SOUND = "medication_reminders_sound"
    private const val CHANNEL_VIBRATION = "medication_reminders_vibration"
    private const val CHANNEL_SILENT = "medication_reminders_silent"
    const val ACTION_TAKEN = "com.mindtrace.action.TAKEN"
    const val EXTRA_REMINDER_ID = "reminder_id"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(
                listOf(
                    NotificationChannel(CHANNEL_SOUND_VIBRATION, "Medikamentenerinnerungen", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Erinnerungen mit Ton und Vibration"
                        enableVibration(true)
                        setSound(sound, attrs)
                    },
                    NotificationChannel(CHANNEL_SOUND, "Medikamentenerinnerungen mit Ton", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Erinnerungen mit Ton ohne Vibration"
                        enableVibration(false)
                        setSound(sound, attrs)
                    },
                    NotificationChannel(CHANNEL_VIBRATION, "Medikamentenerinnerungen mit Vibration", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Erinnerungen ohne Ton mit Vibration"
                        enableVibration(true)
                        setSound(null, null)
                    },
                    NotificationChannel(CHANNEL_SILENT, "Stille Medikamentenerinnerungen", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Erinnerungen ohne Ton und Vibration"
                        enableVibration(false)
                        setSound(null, null)
                    },
                    NotificationChannel(MOOD_CHANNEL_SOUND_VIBRATION, "Stimmungserinnerungen", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Erinnerungen mit Ton und Vibration"
                        enableVibration(true)
                        setSound(sound, attrs)
                    },
                    NotificationChannel(MOOD_CHANNEL_SOUND, "Stimmungserinnerungen mit Ton", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Erinnerungen mit Ton ohne Vibration"
                        enableVibration(false)
                        setSound(sound, attrs)
                    },
                    NotificationChannel(MOOD_CHANNEL_VIBRATION, "Stimmungserinnerungen mit Vibration", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Erinnerungen ohne Ton mit Vibration"
                        enableVibration(true)
                        setSound(null, null)
                    },
                    NotificationChannel(MOOD_CHANNEL_SILENT, "Stille Stimmungserinnerungen", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Erinnerungen ohne Ton und Vibration"
                        enableVibration(false)
                        setSound(null, null)
                    }
                )
            )
        }
    }

    fun showMoodReminder(context: Context) {
        val settings = MindTraceSettingsStore(context).read()
        if (!settings.notificationsEnabled) return
        val english = settings.languageMode == LanguageMode.EN
        val channelId = moodChannelId(settings.soundEnabled, settings.vibrationEnabled)
        val contentIntent = PendingIntent.getActivity(
            context,
            MOOD_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_OPEN_MOOD, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val defaults = (if (settings.soundEnabled) NotificationCompat.DEFAULT_SOUND else 0) or
            (if (settings.vibrationEnabled) NotificationCompat.DEFAULT_VIBRATE else 0)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (english) "How are you feeling?" else "Wie geht es dir?")
            .setContentText(if (english) "Take a moment to record your mood." else "Nimm dir einen Moment, um deine Stimmung einzutragen.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(defaults)
            .setSilent(!settings.soundEnabled && !settings.vibrationEnabled)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(MOOD_NOTIFICATION_ID, notification)
    }

    fun showMedicationReminder(context: Context, reminder: MedicationReminderEntity) {
        val settings = MindTraceSettingsStore(context).read()
        if (!settings.notificationsEnabled) return
        val english = settings.languageMode == LanguageMode.EN
        val contentIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            Intent(context, MainActivity::class.java).putExtra(EXTRA_REMINDER_ID, reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val takenIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id + 10_000).toInt(),
            Intent(context, MedicationActionReceiver::class.java).apply {
                action = ACTION_TAKEN
                putExtra(EXTRA_REMINDER_ID, reminder.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = reminder.dosage?.takeIf { it.isNotBlank() } ?: if (english) "Time for your dose." else "Zeit für deine Einnahme."
        val channelId = when {
            settings.soundEnabled && settings.vibrationEnabled -> CHANNEL_SOUND_VIBRATION
            settings.soundEnabled -> CHANNEL_SOUND
            settings.vibrationEnabled -> CHANNEL_VIBRATION
            else -> CHANNEL_SILENT
        }
        val defaults = (if (settings.soundEnabled) NotificationCompat.DEFAULT_SOUND else 0) or
            (if (settings.vibrationEnabled) NotificationCompat.DEFAULT_VIBRATE else 0)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(reminder.medicationName)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(defaults)
            .setSilent(!settings.soundEnabled && !settings.vibrationEnabled)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(0, if (english) "Taken" else "Eingenommen", takenIntent)
            .build()
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
    }

    fun dismiss(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
    }

    private fun moodChannelId(soundEnabled: Boolean, vibrationEnabled: Boolean): String = when {
        soundEnabled && vibrationEnabled -> MOOD_CHANNEL_SOUND_VIBRATION
        soundEnabled -> MOOD_CHANNEL_SOUND
        vibrationEnabled -> MOOD_CHANNEL_VIBRATION
        else -> MOOD_CHANNEL_SILENT
    }

    const val EXTRA_OPEN_MOOD = "open_mood"

    private const val MOOD_NOTIFICATION_ID = 80_001
    private const val MOOD_CHANNEL_SOUND_VIBRATION = "mood_reminders_sound_vibration"
    private const val MOOD_CHANNEL_SOUND = "mood_reminders_sound"
    private const val MOOD_CHANNEL_VIBRATION = "mood_reminders_vibration"
    private const val MOOD_CHANNEL_SILENT = "mood_reminders_silent"
}
