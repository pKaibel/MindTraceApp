package com.mindtrace.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

data class MindTraceSettings(
    val userName: String? = null,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val moodReminderEnabled: Boolean = false,
    val moodReminderTime: LocalTime = LocalTime.of(20, 0),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.DE,
    val hasSelectedMascot: Boolean = false,
    val mascot: Mascot = Mascot.LUMI,
    val moodTrendDays: Int = 7,
    val medicationStatisticsPeriod: String = "TODAY"
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class LanguageMode {
    DE,
    EN
}

enum class OnboardingReason {
    FIRST_INSTALL,
    APP_UPDATE
}

class MindTraceSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("mindtrace_settings", Context.MODE_PRIVATE)
    private val stateFlow = MutableStateFlow(read())

    val settings: StateFlow<MindTraceSettings> = stateFlow

    fun read(): MindTraceSettings {
        val selectedMascot = prefs.getString(KEY_MASCOT, null)
            ?.let { runCatching { Mascot.valueOf(it) }.getOrNull() }
        return MindTraceSettings(
            userName = prefs.getString(KEY_USER_NAME, null)?.trim()?.takeIf { it.isNotEmpty() },
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            moodReminderEnabled = prefs.getBoolean(KEY_MOOD_REMINDER_ENABLED, false),
            moodReminderTime = LocalTime.of(
                prefs.getInt(KEY_MOOD_REMINDER_HOUR, DEFAULT_MOOD_REMINDER_HOUR),
                prefs.getInt(KEY_MOOD_REMINDER_MINUTE, DEFAULT_MOOD_REMINDER_MINUTE)
            ),
            themeMode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            languageMode = prefs.getString(KEY_LANGUAGE_MODE, LanguageMode.DE.name)
                ?.let { runCatching { LanguageMode.valueOf(it) }.getOrNull() }
                ?: LanguageMode.DE,
            hasSelectedMascot = selectedMascot != null,
            mascot = selectedMascot ?: Mascot.LUMI,
            moodTrendDays = prefs.getInt(KEY_MOOD_TREND_DAYS, DEFAULT_MOOD_TREND_DAYS)
                .takeIf { it in MOOD_TREND_DAYS }
                ?: DEFAULT_MOOD_TREND_DAYS,
            medicationStatisticsPeriod = prefs.getString(
                KEY_MEDICATION_STATISTICS_PERIOD,
                DEFAULT_MEDICATION_STATISTICS_PERIOD
            ) ?: DEFAULT_MEDICATION_STATISTICS_PERIOD
        )
    }

    fun setNotificationsEnabled(enabled: Boolean) = update(KEY_NOTIFICATIONS, enabled)
    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
        stateFlow.value = read()
    }
    fun setSoundEnabled(enabled: Boolean) = update(KEY_SOUND, enabled)
    fun setVibrationEnabled(enabled: Boolean) = update(KEY_VIBRATION, enabled)
    fun setMoodReminder(enabled: Boolean, time: LocalTime) {
        prefs.edit()
            .putBoolean(KEY_MOOD_REMINDER_ENABLED, enabled)
            .putInt(KEY_MOOD_REMINDER_HOUR, time.hour)
            .putInt(KEY_MOOD_REMINDER_MINUTE, time.minute)
            .apply()
        stateFlow.value = read()
    }
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        stateFlow.value = read()
    }

    fun setLanguageMode(mode: LanguageMode) {
        prefs.edit().putString(KEY_LANGUAGE_MODE, mode.name).apply()
        stateFlow.value = read()
    }

    fun setMascot(mascot: Mascot) {
        prefs.edit().putString(KEY_MASCOT, mascot.name).apply()
        stateFlow.value = read()
    }

    fun setMoodTrendDays(days: Int) {
        require(days in MOOD_TREND_DAYS)
        prefs.edit().putInt(KEY_MOOD_TREND_DAYS, days).apply()
        stateFlow.value = read()
    }

    fun setMedicationStatisticsPeriod(period: String) {
        prefs.edit().putString(KEY_MEDICATION_STATISTICS_PERIOD, period).apply()
        stateFlow.value = read()
    }

    fun onboardingReason(versionCode: Long): OnboardingReason? {
        return when (prefs.getLong(KEY_ONBOARDING_VERSION, NO_ONBOARDING_VERSION)) {
            versionCode -> null
            NO_ONBOARDING_VERSION -> OnboardingReason.FIRST_INSTALL
            else -> OnboardingReason.APP_UPDATE
        }
    }

    fun markOnboardingShown(versionCode: Long) {
        prefs.edit().putLong(KEY_ONBOARDING_VERSION, versionCode).apply()
    }

    private fun update(key: String, enabled: Boolean) {
        prefs.edit().putBoolean(key, enabled).apply()
        stateFlow.value = read()
    }

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_MOOD_REMINDER_ENABLED = "mood_reminder_enabled"
        private const val KEY_MOOD_REMINDER_HOUR = "mood_reminder_hour"
        private const val KEY_MOOD_REMINDER_MINUTE = "mood_reminder_minute"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE_MODE = "language_mode"
        private const val KEY_MASCOT = "mascot"
        private const val KEY_MOOD_TREND_DAYS = "mood_trend_days"
        private const val KEY_MEDICATION_STATISTICS_PERIOD = "medication_statistics_period"
        private const val KEY_ONBOARDING_VERSION = "onboarding_version"
        private const val NO_ONBOARDING_VERSION = -1L
        private const val DEFAULT_MOOD_REMINDER_HOUR = 20
        private const val DEFAULT_MOOD_REMINDER_MINUTE = 0
        private const val DEFAULT_MOOD_TREND_DAYS = 7
        private const val DEFAULT_MEDICATION_STATISTICS_PERIOD = "TODAY"
        private val MOOD_TREND_DAYS = setOf(7, 30, 90)
    }
}
