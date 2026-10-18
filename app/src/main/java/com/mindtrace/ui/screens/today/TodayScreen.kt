package com.mindtrace.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindtrace.ui.components.MedicationSkipDialog
import com.mindtrace.ui.components.AnimatedMascot
import com.mindtrace.ui.components.formatDateTime
import com.mindtrace.ui.components.formatFullDate
import com.mindtrace.ui.components.formatTime
import com.mindtrace.ui.components.moodLabel
import com.mindtrace.ui.components.moodSymbol
import com.mindtrace.ui.components.statusText
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.screens.mood.MoodDialog
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.TodayViewModel
import com.mindtrace.util.Mascot
import java.time.Instant
import java.time.LocalDate

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    userName: String?,
    mascot: Mascot,
    onSaveMood: (Int, Int, String?) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalMindTraceStrings.current
    var skipReminderId by remember { mutableStateOf<Long?>(null) }
    var moodDialogOpen by remember { mutableStateOf(false) }
    TodayContent(
        reminders = state.reminders,
        intakesByReminderId = state.intakes.associateBy { it.reminderId },
        latestMoodText = state.latestMood?.let {
            "${strings.physicalWellbeing}: ${moodSymbol(it.physicalWellbeing)} ${moodLabel(it.physicalWellbeing, strings)} · " +
                "${strings.mentalWellbeing}: ${moodSymbol(it.mentalWellbeing)} ${moodLabel(it.mentalWellbeing, strings)}"
        },
        latestMoodRecordedAt = state.latestMood?.recordedAt,
        userName = userName,
        mascot = mascot,
        onOpenMood = { moodDialogOpen = true },
        onTaken = viewModel::markTaken,
        onSkip = { skipReminderId = it }
    )
    skipReminderId?.let { reminderId ->
        MedicationSkipDialog(
            onDismiss = { skipReminderId = null },
            onConfirm = { reason, note ->
                viewModel.markSkipped(reminderId, reason, note)
                skipReminderId = null
            }
        )
    }
    if (moodDialogOpen) {
        MoodDialog(
            mascot = mascot,
            onDismiss = { moodDialogOpen = false },
            onSave = { physicalWellbeing, mentalWellbeing, note ->
                onSaveMood(physicalWellbeing, mentalWellbeing, note)
                moodDialogOpen = false
            }
        )
    }
}

@Composable
private fun TodayContent(
    reminders: List<com.mindtrace.data.local.entity.MedicationReminderEntity>,
    intakesByReminderId: Map<Long, com.mindtrace.data.local.entity.MedicationIntakeEntity>,
    latestMoodText: String?,
    latestMoodRecordedAt: Instant?,
    userName: String?,
    mascot: Mascot,
    onOpenMood: () -> Unit,
    onTaken: (Long) -> Unit,
    onSkip: (Long) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    val medicationCardColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF25232B)
    } else {
        Color(0xFFDAD4E7)
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = userName?.let { "${strings.greeting}, $it" } ?: strings.greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(LocalDate.now().formatFullDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MascotTile(mascot = mascot)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = medicationCardColor)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.todaysMedications, style = MaterialTheme.typography.titleLarge)
                if (reminders.isEmpty()) {
                    Text(strings.noReminders)
                } else {
                    reminders.forEach { reminder ->
                        val intake = intakesByReminderId[reminder.id]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${reminder.reminderTime.formatTime()}  ${reminder.medicationName}", fontWeight = FontWeight.Medium)
                                    Text(intake?.statusText(strings) ?: strings.statusOpen, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (intake == null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Button(onClick = { onTaken(reminder.id) }) { Text(strings.markDone) }
                                        OutlinedButton(onClick = { onSkip(reminder.id) }) { Text(strings.skipMedication) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.quickMood, style = MaterialTheme.typography.titleLarge)
                Text(latestMoodText ?: strings.noMood)
                latestMoodRecordedAt?.let { recordedAt ->
                    Text(
                        strings.moodRecordedAt.format(recordedAt.formatDateTime()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onOpenMood) { Text(strings.recordMood) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MascotTile(mascot: Mascot) {
    Card(
        modifier = Modifier.size(128.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            AnimatedMascot(mascot = mascot, modifier = Modifier.size(72.dp))
            Text(mascot.displayName, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview
@Composable
private fun TodayPreview() {
    MindTraceTheme {
        TodayContent(emptyList(), emptyMap(), " :) Gut", Instant.now(), "Alex", Mascot.LUMI, {}, {}, {})
    }
}
