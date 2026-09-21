package com.mindtrace.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MindTraceSettings(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.DE
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

class MindTraceSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("mindtrace_settings", Context.MODE_PRIVATE)
    private val stateFlow = MutableStateFlow(read())

    val settings: StateFlow<MindTraceSettings> = stateFlow

    fun read(): MindTraceSettings = MindTraceSettings(
        notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true),
        soundEnabled = prefs.getBoolean(KEY_SOUND, true),
        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
        themeMode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM,
        languageMode = prefs.getString(KEY_LANGUAGE_MODE, LanguageMode.DE.name)
            ?.let { runCatching { LanguageMode.valueOf(it) }.getOrNull() }
            ?: LanguageMode.DE
    )

    fun setNotificationsEnabled(enabled: Boolean) = update(KEY_NOTIFICATIONS, enabled)
    fun setSoundEnabled(enabled: Boolean) = update(KEY_SOUND, enabled)
    fun setVibrationEnabled(enabled: Boolean) = update(KEY_VIBRATION, enabled)
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        stateFlow.value = read()
    }

    fun setLanguageMode(mode: LanguageMode) {
        prefs.edit().putString(KEY_LANGUAGE_MODE, mode.name).apply()
        stateFlow.value = read()
    }

    fun shouldShowOnboarding(versionCode: Long): Boolean =
        prefs.getLong(KEY_ONBOARDING_VERSION, NO_ONBOARDING_VERSION) != versionCode

    fun markOnboardingShown(versionCode: Long) {
        prefs.edit().putLong(KEY_ONBOARDING_VERSION, versionCode).apply()
    }

    private fun update(key: String, enabled: Boolean) {
        prefs.edit().putBoolean(key, enabled).apply()
        stateFlow.value = read()
    }

    companion object {
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE_MODE = "language_mode"
        private const val KEY_ONBOARDING_VERSION = "onboarding_version"
        private const val NO_ONBOARDING_VERSION = -1L
    }
}
