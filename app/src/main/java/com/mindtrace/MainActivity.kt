package com.mindtrace

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.i18n.stringsFor
import com.mindtrace.ui.navigation.MindTraceRoute
import com.mindtrace.ui.screens.medication.MedicationScreen
import com.mindtrace.ui.screens.mood.MoodScreen
import com.mindtrace.ui.screens.onboarding.OnboardingScreen
import com.mindtrace.ui.screens.settings.SettingsScreen
import com.mindtrace.ui.screens.today.TodayScreen
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.MedicationViewModel
import com.mindtrace.ui.viewmodel.MoodViewModel
import com.mindtrace.ui.viewmodel.SettingsViewModel
import com.mindtrace.ui.viewmodel.TodayViewModel
import com.mindtrace.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as MindTraceApp).container
        val factory = ViewModelFactory(
            container.moodRepository,
            container.medicationRepository,
            container.alarmScheduler,
            container.settingsStore,
            container.testDataRepository
        )
        setContent {
            val settings by container.settingsStore.settings.collectAsState()
            val versionCode = remember { currentVersionCode() }
            var showOnboarding by remember(versionCode) {
                mutableStateOf(container.settingsStore.shouldShowOnboarding(versionCode))
            }
            val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(showOnboarding) {
                if (!showOnboarding && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            MindTraceTheme(themeMode = settings.themeMode) {
                CompositionLocalProvider(LocalMindTraceStrings provides stringsFor(settings.languageMode)) {
                    if (showOnboarding) {
                        OnboardingScreen(
                            onFinish = {
                                container.settingsStore.markOnboardingShown(versionCode)
                                showOnboarding = false
                            }
                        )
                    } else {
                        MindTraceAppContent(
                            factory = factory,
                            openExactAlarmSettings = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    startActivity(Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun currentVersionCode(): Long {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }
}

@Composable
private fun MindTraceAppContent(
    factory: ViewModelFactory,
    openExactAlarmSettings: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(MindTraceRoute.Today, MindTraceRoute.Mood, MindTraceRoute.Medication, MindTraceRoute.Settings)
    val strings = LocalMindTraceStrings.current
    Scaffold(
        bottomBar = {
            val backStack = navController.currentBackStackEntryAsState().value
            NavigationBar {
                items.forEach { route ->
                    val selected = backStack?.destination?.hierarchy?.any { it.route == route.route } == true
                    val label = when (route) {
                        MindTraceRoute.Today -> strings.today
                        MindTraceRoute.Mood -> strings.mood
                        MindTraceRoute.Medication -> strings.medication
                        MindTraceRoute.Settings -> strings.settings
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (route) {
                                    MindTraceRoute.Today -> Icons.Default.Home
                                    MindTraceRoute.Mood -> Icons.Default.BarChart
                                    MindTraceRoute.Medication -> Icons.Default.Medication
                                    MindTraceRoute.Settings -> Icons.Default.Settings
                                },
                                contentDescription = label
                            )
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MindTraceRoute.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MindTraceRoute.Today.route) {
                val vm: TodayViewModel = viewModel(factory = factory)
                TodayScreen(viewModel = vm, onOpenMood = { navController.navigate(MindTraceRoute.Mood.route) })
            }
            composable(MindTraceRoute.Mood.route) {
                val vm: MoodViewModel = viewModel(factory = factory)
                MoodScreen(viewModel = vm)
            }
            composable(MindTraceRoute.Medication.route) {
                val vm: MedicationViewModel = viewModel(factory = factory)
                MedicationScreen(viewModel = vm, openExactAlarmSettings = openExactAlarmSettings)
            }
            composable(MindTraceRoute.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
