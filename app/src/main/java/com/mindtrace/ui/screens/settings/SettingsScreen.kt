package com.mindtrace.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.SettingsViewModel
import com.mindtrace.util.LanguageMode
import com.mindtrace.util.Mascot
import com.mindtrace.util.MindTraceSettings
import com.mindtrace.util.ThemeMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    SettingsContent(
        settings = settings,
        message = message,
        onLanguageModeChanged = viewModel::setLanguageMode,
        onThemeModeChanged = viewModel::setThemeMode,
        onUserNameChanged = viewModel::setUserName,
        onMascotChanged = viewModel::setMascot,
        onNotificationsChanged = viewModel::setNotificationsEnabled,
        onSoundChanged = viewModel::setSoundEnabled,
        onVibrationChanged = viewModel::setVibrationEnabled,
        onAddTestData = viewModel::addTestData,
        onResetAllData = { showResetDialog = true }
    )

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(LocalMindTraceStrings.current.resetAllDataTitle) },
            text = { Text(LocalMindTraceStrings.current.resetAllDataText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                    }
                ) { Text(LocalMindTraceStrings.current.reset) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(LocalMindTraceStrings.current.cancel) }
            }
        )
    }
}

@Composable
private fun SettingsContent(
    settings: MindTraceSettings,
    message: String?,
    onLanguageModeChanged: (LanguageMode) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onMascotChanged: (Mascot) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onAddTestData: () -> Unit,
    onResetAllData: () -> Unit
) {
    val strings = LocalMindTraceStrings.current
    var userName by rememberSaveable(settings.userName) { mutableStateOf(settings.userName.orEmpty()) }
    val context = LocalContext.current
    val appVersion = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
    ) {
        item {
            Text(strings.settings, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.profile, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(strings.profileDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.onboardingNameLabel) },
                        singleLine = true
                    )
                    Button(
                        onClick = { onUserNameChanged(userName) },
                        enabled = userName.trim().isNotEmpty() && userName.trim() != settings.userName
                    ) {
                        Text(strings.save)
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.appearance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                            SelectableChip(
                                selected = settings.themeMode == mode,
                                onClick = { onThemeModeChanged(mode) },
                                text = mode.label(strings)
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.mascot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(strings.mascotDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Mascot.values().toList().chunked(2).forEach { mascots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            mascots.forEach { mascot ->
                                MascotChoice(
                                    mascot = mascot,
                                    selected = settings.mascot == mascot,
                                    onClick = { onMascotChanged(mascot) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (mascots.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.language, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(LanguageMode.DE, LanguageMode.EN).forEach { mode ->
                            SelectableChip(
                                selected = settings.languageMode == mode,
                                onClick = { onLanguageModeChanged(mode) },
                                text = mode.label(strings)
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingSwitch(
                        title = strings.notifications,
                        description = strings.notificationsDescription,
                        checked = settings.notificationsEnabled,
                        onCheckedChange = onNotificationsChanged
                    )
                    SettingSwitch(
                        title = strings.sound,
                        description = strings.soundDescription,
                        checked = settings.soundEnabled,
                        enabled = settings.notificationsEnabled,
                        onCheckedChange = onSoundChanged
                    )
                    SettingSwitch(
                        title = strings.vibration,
                        description = strings.vibrationDescription,
                        checked = settings.vibrationEnabled,
                        enabled = settings.notificationsEnabled,
                        onCheckedChange = onVibrationChanged
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.testMaintenance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Button(onClick = onAddTestData, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.addTestData)
                    }
                    OutlinedButton(onClick = onResetAllData, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.resetAllData)
                    }
                    message?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.disclaimer)
                    Text(strings.privacy)
                }
            }
        }
        item {
            Text(
                text = "${strings.appVersion} $appVersion",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MascotChoice(
    mascot: Mascot,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(128.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(mascot.portraitResourceId),
                contentDescription = mascot.displayName,
                modifier = Modifier.size(72.dp)
            )
            Text(mascot.displayName, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
        }
    }
}

@Composable
private fun SelectableChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

private fun ThemeMode.label(strings: com.mindtrace.ui.i18n.MindTraceStrings): String = when (this) {
    ThemeMode.SYSTEM -> strings.system
    ThemeMode.LIGHT -> strings.light
    ThemeMode.DARK -> strings.dark
}

private fun LanguageMode.label(strings: com.mindtrace.ui.i18n.MindTraceStrings): String = when (this) {
    LanguageMode.DE -> strings.german
    LanguageMode.EN -> strings.english
}

@Preview
@Composable
private fun SettingsPreview() {
    MindTraceTheme {
        SettingsContent(
            settings = MindTraceSettings(),
            message = null,
            onLanguageModeChanged = {},
            onThemeModeChanged = {},
            onUserNameChanged = {},
            onMascotChanged = {},
            onNotificationsChanged = {},
            onSoundChanged = {},
            onVibrationChanged = {},
            onAddTestData = {},
            onResetAllData = {}
        )
    }
}
