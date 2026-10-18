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
import androidx.compose.runtime.mutableIntStateOf
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
import com.mindtrace.util.OnboardingReason
import com.mindtrace.util.Mascot

class MainActivity : ComponentActivity() {
    private var moodEntryRequest by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(com.mindtrace.notification.NotificationHelper.EXTRA_OPEN_MOOD, false)) {
            moodEntryRequest++
        }
        val container = (application as MindTraceApp).container
        val factory = ViewModelFactory(
            container.moodRepository,
            container.medicationRepository,
            container.alarmScheduler,
            container.moodAlarmScheduler,
            container.settingsStore,
            container.testDataRepository
        )
        val startDestination = if (moodEntryRequest > 0) {
            MindTraceRoute.Mood.route
        } else {
            MindTraceRoute.Today.route
        }
        setContent {
            val settings by container.settingsStore.settings.collectAsState()
            val versionCode = remember { currentVersionCode() }
            val onboardingReason = remember(versionCode) {
                container.settingsStore.onboardingReason(versionCode)
            }
            var showOnboarding by remember(versionCode) {
                mutableStateOf(
                    onboardingReason != null ||
                        settings.userName.isNullOrBlank() ||
                        !settings.hasSelectedMascot
                )
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
                            reason = onboardingReason ?: OnboardingReason.APP_UPDATE,
                            initialName = settings.userName,
                            initialMascot = settings.mascot.takeIf { settings.hasSelectedMascot },
                            onFinish = { userName, mascot ->
                                container.settingsStore.setUserName(userName)
                                container.settingsStore.setMascot(mascot)
                                container.settingsStore.markOnboardingShown(versionCode)
                                showOnboarding = false
                            }
                        )
                    } else {
                        MindTraceAppContent(
                            factory = factory,
                            startDestination = startDestination,
                            moodEntryRequest = moodEntryRequest,
                            onMoodEntryRequestHandled = { moodEntryRequest-- },
                            userName = settings.userName,
                            mascot = settings.mascot,
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(com.mindtrace.notification.NotificationHelper.EXTRA_OPEN_MOOD, false)) {
            moodEntryRequest++
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
    startDestination: String,
    moodEntryRequest: Int,
    onMoodEntryRequestHandled: () -> Unit,
    userName: String?,
    mascot: Mascot,
    openExactAlarmSettings: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(MindTraceRoute.Today, MindTraceRoute.Mood, MindTraceRoute.Medication, MindTraceRoute.Settings)
    val strings = LocalMindTraceStrings.current
    LaunchedEffect(moodEntryRequest) {
        if (moodEntryRequest > 0) {
            navController.navigate(MindTraceRoute.Mood.route) {
                popUpTo(MindTraceRoute.Today.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
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
                                popUpTo(MindTraceRoute.Today.route) { inclusive = false }
                                launchSingleTop = true
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MindTraceRoute.Today.route) {
                val todayViewModel: TodayViewModel = viewModel(factory = factory)
                val moodViewModel: MoodViewModel = viewModel(factory = factory)
                TodayScreen(
                    viewModel = todayViewModel,
                    userName = userName,
                    mascot = mascot,
                    onSaveMood = moodViewModel::save
                )
            }
            composable(MindTraceRoute.Mood.route) {
                val vm: MoodViewModel = viewModel(factory = factory)
                MoodScreen(
                    viewModel = vm,
                    mascot = mascot,
                    openNewEntryRequest = moodEntryRequest,
                    onNewEntryRequestHandled = onMoodEntryRequestHandled
                )
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
