package com.mindtrace.ui.navigation

sealed class MindTraceRoute(val route: String) {
    data object Today : MindTraceRoute("today")
    data object Mood : MindTraceRoute("mood")
    data object NewMoodEntry : MindTraceRoute("mood/new")
    data object Medication : MindTraceRoute("medication")
    data object Settings : MindTraceRoute("settings")
}
