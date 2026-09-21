package com.mindtrace.ui.screens.mood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.ui.components.MoodChart
import com.mindtrace.ui.components.formatDateTime
import com.mindtrace.ui.components.moodLabel
import com.mindtrace.ui.components.moodSymbol
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.MoodViewModel
import java.time.Instant

@Composable
fun MoodScreen(viewModel: MoodViewModel) {
    val state by viewModel.state.collectAsState()
    var dialogOpen by remember { mutableStateOf(false) }
    MoodContent(
        entries = state.entries,
        days = state.days,
        average = state.average,
        onDays = viewModel::setDays,
        onAdd = { dialogOpen = true },
        onDelete = viewModel::delete
    )
    if (dialogOpen) {
        MoodDialog(
            onDismiss = { dialogOpen = false },
            onSave = { value, note ->
                viewModel.save(value, note)
                dialogOpen = false
            }
        )
    }
}

@Composable
private fun MoodContent(
    entries: List<MoodEntryEntity>,
    days: Int,
    average: Double?,
    onDays: (Int) -> Unit,
    onAdd: () -> Unit,
    onDelete: (MoodEntryEntity) -> Unit
) {
    val strings = LocalMindTraceStrings.current
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30, 90).forEach {
                    FilterChip(selected = days == it, onClick = { onDays(it) }, label = { Text("$it ${strings.days}") })
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.moodTrendValues, style = MaterialTheme.typography.titleMedium)
                    MoodChart(entries)
                    Text("${strings.average}: ${average?.let { "%.1f".format(it) } ?: "-"}")
                    Text("${strings.entries}: ${entries.size}")
                }
            }
        }
        item { Text(strings.latestEntries, style = MaterialTheme.typography.titleLarge) }
        items(entries.sortedByDescending { it.recordedAt }, key = { it.id }) { entry ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${moodSymbol(entry.moodValue)} ${moodLabel(entry.moodValue, strings)}", fontWeight = FontWeight.Medium)
                    Text(entry.recordedAt.formatDateTime(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    entry.note?.let { Text(it) }
                    TextButton(onClick = { onDelete(entry) }) { Text(strings.delete) }
                }
            }
        }
    }
}

@Composable
private fun MoodDialog(onDismiss: () -> Unit, onSave: (Int, String?) -> Unit) {
    val strings = LocalMindTraceStrings.current
    var value by rememberSaveable { mutableIntStateOf(3) }
    var note by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.moodDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.selected, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { mood ->
                        val modifier = Modifier.weight(1f)
                        if (mood == value) {
                            Button(
                                modifier = modifier,
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                onClick = { value = mood }
                            ) { Text(moodSymbol(mood)) }
                        } else {
                            OutlinedButton(
                                modifier = modifier,
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                onClick = { value = mood }
                            ) { Text(moodSymbol(mood)) }
                        }
                    }
                }
                Text(moodLabel(value, strings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(strings.optionalNote) }, minLines = 2)
            }
        },
        confirmButton = { Button(onClick = { onSave(value, note) }) { Text(strings.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Preview
@Composable
private fun MoodPreview() {
    MindTraceTheme {
        MoodContent(
            entries = listOf(MoodEntryEntity(1, 4, "Ruhiger Nachmittag", Instant.now())),
            days = 7,
            average = 4.0,
            onDays = {},
            onAdd = {},
            onDelete = {}
        )
    }
}
