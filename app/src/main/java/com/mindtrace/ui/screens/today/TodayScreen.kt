package com.mindtrace.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindtrace.ui.components.MedicationSkipDialog
import com.mindtrace.ui.components.formatFullDate
import com.mindtrace.ui.components.formatTime
import com.mindtrace.ui.components.moodLabel
import com.mindtrace.ui.components.moodSymbol
import com.mindtrace.ui.components.statusText
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.TodayViewModel
import java.time.LocalDate

@Composable
fun TodayScreen(viewModel: TodayViewModel, onOpenMood: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val strings = LocalMindTraceStrings.current
    var skipReminderId by remember { mutableStateOf<Long?>(null) }
    TodayContent(
        reminders = state.reminders,
        intakesByReminderId = state.intakes.associateBy { it.reminderId },
        latestMoodText = state.latestMood?.let { "${moodSymbol(it.moodValue)} ${moodLabel(it.moodValue, strings)}" },
        onOpenMood = onOpenMood,
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
}

@Composable
private fun TodayContent(
    reminders: List<com.mindtrace.data.local.entity.MedicationReminderEntity>,
    intakesByReminderId: Map<Long, com.mindtrace.data.local.entity.MedicationIntakeEntity>,
    latestMoodText: String?,
    onOpenMood: () -> Unit,
    onTaken: (Long) -> Unit,
    onSkip: (Long) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(LocalDate.now().formatFullDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.todaysMedications, style = MaterialTheme.typography.titleLarge)
                if (reminders.isEmpty()) {
                    Text(strings.noReminders)
                } else {
                    reminders.forEach { reminder ->
                        val intake = intakesByReminderId[reminder.id]
                        Row(
                            Modifier.fillMaxWidth(),
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
                                    androidx.compose.material3.TextButton(onClick = { onSkip(reminder.id) }) { Text(strings.notTaken) }
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
                OutlinedButton(onClick = onOpenMood) { Text(strings.recordMood) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Preview
@Composable
private fun TodayPreview() {
    MindTraceTheme {
        TodayContent(emptyList(), emptyMap(), " :) Gut", {}, {}, {})
    }
}
