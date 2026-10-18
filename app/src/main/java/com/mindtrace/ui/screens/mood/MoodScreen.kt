package com.mindtrace.ui.screens.mood

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.ui.components.MoodChart
import com.mindtrace.ui.components.MascotMoodIcon
import com.mindtrace.ui.components.MoodDimension
import com.mindtrace.ui.components.formatDateTime
import com.mindtrace.ui.components.moodLabel
import com.mindtrace.ui.components.moodSymbol
import com.mindtrace.ui.components.formatTime
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.MoodViewModel
import com.mindtrace.util.Mascot
import java.time.Instant
import java.time.LocalTime

@Composable
fun MoodScreen(
    viewModel: MoodViewModel,
    mascot: Mascot = Mascot.LUMI,
    openNewEntryRequest: Int = 0,
    onNewEntryRequestHandled: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var dialogOpen by remember { mutableStateOf(false) }
    LaunchedEffect(openNewEntryRequest) {
        if (openNewEntryRequest > 0) {
            dialogOpen = true
            onNewEntryRequestHandled()
        }
    }
    MoodContent(
        entries = state.entries,
        days = state.days,
        physicalAverage = state.physicalAverage,
        mentalAverage = state.mentalAverage,
        moodReminderEnabled = state.moodReminderEnabled,
        moodReminderTime = state.moodReminderTime,
        onDays = viewModel::setDays,
        onAdd = { dialogOpen = true },
        onMoodReminderChanged = viewModel::setMoodReminder,
        onDelete = viewModel::delete
    )
    if (dialogOpen) {
        MoodDialog(
            mascot = mascot,
            onDismiss = { dialogOpen = false },
            onSave = { physicalWellbeing, mentalWellbeing, note ->
                viewModel.save(physicalWellbeing, mentalWellbeing, note)
                dialogOpen = false
            }
        )
    }
}

@Composable
private fun MoodContent(
    entries: List<MoodEntryEntity>,
    days: Int,
    physicalAverage: Double?,
    mentalAverage: Double?,
    moodReminderEnabled: Boolean,
    moodReminderTime: LocalTime,
    onDays: (Int) -> Unit,
    onAdd: () -> Unit,
    onMoodReminderChanged: (Boolean, LocalTime) -> Unit,
    onDelete: (MoodEntryEntity) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.mood, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Button(onClick = onAdd) { Text(strings.newEntry) }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.moodReminder, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = moodReminderEnabled,
                            onCheckedChange = { onMoodReminderChanged(it, moodReminderTime) }
                        )
                    }
                    Text(strings.moodReminderDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        enabled = moodReminderEnabled,
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    onMoodReminderChanged(moodReminderEnabled, LocalTime.of(hour, minute))
                                },
                                moodReminderTime.hour,
                                moodReminderTime.minute,
                                true
                            ).show()
                        }
                    ) {
                        Text("${strings.reminderTime}: ${moodReminderTime.formatTime()}")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30, 90).forEach {
                    FilterChip(selected = days == it, onClick = { onDays(it) }, label = { Text("$it ${strings.days}") })
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.physicalWellbeing, style = MaterialTheme.typography.titleMedium)
                    MoodChart(entries, valueFor = { it.physicalWellbeing })
                    Text("${strings.average}: ${physicalAverage?.let { "%.1f".format(it) } ?: "-"}")
                    Text(strings.mentalWellbeing, style = MaterialTheme.typography.titleMedium)
                    MoodChart(entries, valueFor = { it.mentalWellbeing })
                    Text("${strings.average}: ${mentalAverage?.let { "%.1f".format(it) } ?: "-"}")
                    Text("${strings.entries}: ${entries.size}")
                }
            }
        }
        item { Text(strings.latestEntries, style = MaterialTheme.typography.titleLarge) }
        items(entries.sortedByDescending { it.recordedAt }, key = { it.id }) { entry ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${strings.physicalWellbeing}: ${moodSymbol(entry.physicalWellbeing)} ${moodLabel(entry.physicalWellbeing, strings)}", fontWeight = FontWeight.Medium)
                    Text("${strings.mentalWellbeing}: ${moodSymbol(entry.mentalWellbeing)} ${moodLabel(entry.mentalWellbeing, strings)}", fontWeight = FontWeight.Medium)
                    Text(entry.recordedAt.formatDateTime(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    entry.note?.let { Text(it) }
                    TextButton(onClick = { onDelete(entry) }) { Text(strings.delete) }
                }
            }
        }
    }
}

@Composable
fun MoodDialog(
    mascot: Mascot = Mascot.LUMI,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String?) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    var physicalWellbeing by rememberSaveable { mutableIntStateOf(3) }
    var mentalWellbeing by rememberSaveable { mutableIntStateOf(3) }
    var note by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.moodDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MoodValueSelector(
                    title = strings.physicalWellbeing,
                    value = physicalWellbeing,
                    mascot = mascot,
                    dimension = MoodDimension.PHYSICAL,
                    onValueChange = { physicalWellbeing = it }
                )
                MoodValueSelector(
                    title = strings.mentalWellbeing,
                    value = mentalWellbeing,
                    mascot = mascot,
                    dimension = MoodDimension.MENTAL,
                    onValueChange = { mentalWellbeing = it }
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(strings.optionalNote) }, minLines = 2)
            }
        },
        confirmButton = { Button(onClick = { onSave(physicalWellbeing, mentalWellbeing, note) }) { Text(strings.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
private fun MoodValueSelector(
    title: String,
    value: Int,
    mascot: Mascot,
    dimension: MoodDimension,
    onValueChange: (Int) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    Text(title, style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { mood ->
            val modifier = Modifier.weight(1f).height(64.dp)
            if (mood == value) {
                Button(
                    modifier = modifier,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    onClick = { onValueChange(mood) }
                ) {
                    MascotMoodIcon(
                        mascot = mascot,
                        moodValue = mood,
                        dimension = dimension,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                OutlinedButton(
                    modifier = modifier,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    onClick = { onValueChange(mood) }
                ) {
                    MascotMoodIcon(
                        mascot = mascot,
                        moodValue = mood,
                        dimension = dimension,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    Text(moodLabel(value, strings), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
}

@Preview
@Composable
private fun MoodPreview() {
    MindTraceTheme {
        MoodContent(
            entries = listOf(MoodEntryEntity(1, 4, "Ruhiger Nachmittag", Instant.now())),
            days = 7,
            physicalAverage = 4.0,
            mentalAverage = 3.0,
            moodReminderEnabled = true,
            moodReminderTime = LocalTime.of(20, 0),
            onDays = {},
            onAdd = {},
            onMoodReminderChanged = { _, _ -> },
            onDelete = {}
        )
    }
}
