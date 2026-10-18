package com.mindtrace.ui.screens.medication

import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.domain.model.IntakeStatus
import com.mindtrace.domain.model.MedicationSkipReason
import com.mindtrace.domain.model.MedicationRecurrenceType
import com.mindtrace.ui.components.formatTime
import com.mindtrace.ui.components.MedicationSkipDialog
import com.mindtrace.ui.components.statusText
import com.mindtrace.ui.components.label
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.ui.state.MedicationStatisticsPeriod
import com.mindtrace.ui.theme.MindTraceTheme
import com.mindtrace.ui.viewmodel.MedicationViewModel
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun MedicationScreen(viewModel: MedicationViewModel, openExactAlarmSettings: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var editReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    var deleteReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    var dialogOpen by remember { mutableStateOf(false) }
    var skipReminderId by remember { mutableStateOf<Long?>(null) }
    MedicationContent(
        reminders = state.reminders,
        intakesToday = state.intakesToday,
        intakesInStatisticsPeriod = state.intakesInStatisticsPeriod,
        statisticsPeriod = state.statisticsPeriod,
        exactAlarmAllowed = state.exactAlarmAllowed,
        notificationsAllowed = state.notificationsAllowed,
        onAdd = { dialogOpen = true },
        onEdit = { editReminder = it },
        onEnabled = viewModel::setEnabled,
        onTaken = viewModel::markTaken,
        onSkip = { skipReminderId = it },
        onStatisticsPeriodChanged = viewModel::setStatisticsPeriod,
        openExactAlarmSettings = openExactAlarmSettings
    )
    if (dialogOpen) {
        ReminderDialog(
            reminder = null,
            onDismiss = { dialogOpen = false },
            onDelete = null,
            onSave = { name, dosage, times, enabled, recurrenceType, weekdays, intervalDays ->
                viewModel.create(name, dosage, times, enabled, recurrenceType, weekdays, intervalDays)
                dialogOpen = false
            }
        )
    }
    skipReminderId?.let { reminderId ->
        MedicationSkipDialog(
            onDismiss = { skipReminderId = null },
            onConfirm = { reason, note ->
                viewModel.markSkipped(reminderId, reason, note)
                skipReminderId = null
            }
        )
    }
    deleteReminder?.let { reminder ->
        AlertDialog(
            onDismissRequest = { deleteReminder = null },
            title = { Text(LocalMindTraceStrings.current.deleteMedicationTitle) },
            text = { Text(LocalMindTraceStrings.current.deleteMedicationText.format(reminder.medicationName)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.delete(reminder)
                        deleteReminder = null
                    }
                ) { Text(LocalMindTraceStrings.current.delete) }
            },
            dismissButton = {
                TextButton(onClick = { deleteReminder = null }) { Text(LocalMindTraceStrings.current.cancel) }
            }
        )
    }
    editReminder?.let { current ->
        ReminderDialog(
            reminder = current,
            onDismiss = { editReminder = null },
            onDelete = {
                deleteReminder = current
                editReminder = null
            },
            onSave = { name, dosage, times, enabled, recurrenceType, weekdays, intervalDays ->
                viewModel.update(
                    current.copy(
                        medicationName = name,
                        dosage = dosage,
                        reminderTime = times.single(),
                        enabled = enabled,
                        recurrenceType = recurrenceType,
                        recurrenceWeekdays = weekdays.sortedBy { it.value }.joinToString(",") { it.value.toString() }.ifBlank { null },
                        recurrenceIntervalDays = intervalDays?.takeIf { recurrenceType == MedicationRecurrenceType.INTERVAL },
                        recurrenceStartDate = if (recurrenceType == MedicationRecurrenceType.INTERVAL) {
                            current.recurrenceStartDate ?: LocalDate.now()
                        } else {
                            null
                        }
                    )
                )
                editReminder = null
            }
        )
    }
}

@Composable
private fun MedicationContent(
    reminders: List<MedicationReminderEntity>,
    intakesToday: List<MedicationIntakeEntity>,
    intakesInStatisticsPeriod: List<MedicationIntakeEntity>,
    statisticsPeriod: MedicationStatisticsPeriod,
    exactAlarmAllowed: Boolean,
    notificationsAllowed: Boolean,
    onAdd: () -> Unit,
    onEdit: (MedicationReminderEntity) -> Unit,
    onEnabled: (MedicationReminderEntity, Boolean) -> Unit,
    onTaken: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onStatisticsPeriodChanged: (MedicationStatisticsPeriod) -> Unit,
    openExactAlarmSettings: () -> Unit
) {
    val strings = LocalMindTraceStrings.current
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.medications, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Button(onClick = onAdd) { Text(strings.addNew) }
            }
        }
        item {
            MedicationStatistics(
                reminders = reminders,
                intakes = intakesInStatisticsPeriod,
                statisticsPeriod = statisticsPeriod,
                onStatisticsPeriodChanged = onStatisticsPeriodChanged
            )
        }
        if (!notificationsAllowed) item {
            Card(Modifier.fillMaxWidth()) {
                Text(strings.notificationsDenied, Modifier.padding(14.dp))
            }
        }
        if (!exactAlarmAllowed) item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.exactAlarmDenied)
                    TextButton(onClick = openExactAlarmSettings) { Text(strings.openSystemSettings) }
                }
            }
        }
        if (reminders.isEmpty()) item {
            Card(Modifier.fillMaxWidth()) { Text(strings.noReminders, Modifier.padding(16.dp)) }
        }
        items(reminders, key = { it.id }) { reminder ->
            val intake = intakesToday.firstOrNull { it.reminderId == reminder.id }
            val scheduledToday = reminder.occursOn(LocalDate.now())
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(reminder.medicationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text("${reminder.reminderTime.formatTime()}${reminder.dosage?.let { " · $it" } ?: ""}")
                            Text(reminder.recurrenceLabel(strings), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                when {
                                    !scheduledToday -> strings.notScheduledToday
                                    intake != null -> intake.statusText(strings)
                                    else -> strings.openToday
                                }
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(onClick = { onEdit(reminder) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = strings.edit
                                )
                            }
                            Switch(checked = reminder.enabled, onCheckedChange = { onEnabled(reminder, it) })
                        }
                    }
                    if (scheduledToday && intake == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onSkip(reminder.id) }
                            ) { Text(strings.skipMedication) }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { onTaken(reminder.id) }
                            ) { Text(strings.markDone) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationStatistics(
    reminders: List<MedicationReminderEntity>,
    intakes: List<MedicationIntakeEntity>,
    statisticsPeriod: MedicationStatisticsPeriod,
    onStatisticsPeriodChanged: (MedicationStatisticsPeriod) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    val today = LocalDate.now()
    val startDate = statisticsPeriod.startDate(today)
    val scheduledIntakes = generateSequence(startDate) { date ->
        date.plusDays(1).takeIf { !it.isAfter(today) }
    }.flatMap { date ->
        reminders.asSequence()
            .filter { reminder -> reminder.isScheduledOn(date) }
            .map { reminder -> reminder to date }
    }.toList()
    val intakesByReminderAndDate = intakes.associateBy { it.reminderId to it.scheduledDate }
    val taken = scheduledIntakes.mapNotNull { scheduledIntake ->
        intakesByReminderAndDate[scheduledIntake.first.id to scheduledIntake.second]
            ?.takeIf { it.status == IntakeStatus.TAKEN }
            ?.let { scheduledIntake to it }
    }
    val skipped = scheduledIntakes.mapNotNull { scheduledIntake ->
        intakesByReminderAndDate[scheduledIntake.first.id to scheduledIntake.second]
            ?.takeIf { it.status == IntakeStatus.SKIPPED }
            ?.let { scheduledIntake to it }
    }
    val onTimeTaken = taken.count { (_, intake) ->
        intake.completedAt != null && !intake.completedAt.isAfter(intake.scheduledDateTime.plus(ON_TIME_GRACE_MINUTES, ChronoUnit.MINUTES))
    }
    val open = scheduledIntakes.size - taken.size - skipped.size

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.medicationStatistics, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedicationStatisticsPeriod.values().forEach { period ->
                    FilterChip(
                        selected = statisticsPeriod == period,
                        onClick = { onStatisticsPeriodChanged(period) },
                        label = { Text(period.label(strings)) }
                    )
                }
            }
            if (scheduledIntakes.isEmpty()) {
                Text(strings.noScheduledIntakes, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                if (onTimeTaken == scheduledIntakes.size) {
                    Text(strings.allIntakesOnTime, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                } else {
                    Text(strings.onTimeIntakes.format(onTimeTaken, scheduledIntakes.size))
                }
                if (open > 0) {
                    val openLabel = if (statisticsPeriod == MedicationStatisticsPeriod.TODAY) {
                        strings.openIntakes
                    } else {
                        strings.undocumentedIntakes
                    }
                    Text(openLabel.format(open), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text("${strings.takenIntakes}: ${taken.size}")
                Text("${strings.skippedIntakes}: ${skipped.size}")

                if (statisticsPeriod == MedicationStatisticsPeriod.TODAY && taken.isNotEmpty()) {
                    Text(strings.takenIntakes, style = MaterialTheme.typography.labelLarge)
                    taken.forEach { (scheduledIntake, intake) ->
                        val reminder = scheduledIntake.first
                        Text("${reminder.medicationName} · ${strings.scheduledAt.format(reminder.reminderTime.formatTime())} · ${intake.completedAt?.let { strings.takenAt.format(it.formatLocalTime()) } ?: strings.statusTaken}")
                    }
                }
                if (statisticsPeriod == MedicationStatisticsPeriod.TODAY && skipped.isNotEmpty()) {
                    Text(strings.skippedIntakes, style = MaterialTheme.typography.labelLarge)
                    skipped.forEach { (scheduledIntake, intake) ->
                        val reminder = scheduledIntake.first
                        val reason = intake.skipReason?.label(strings).orEmpty().let { label ->
                            if (intake.skipReason == MedicationSkipReason.OTHER && !intake.skipNote.isNullOrBlank()) "$label: ${intake.skipNote}" else label
                        }
                        Text("${reminder.medicationName} · ${strings.scheduledAt.format(reminder.reminderTime.formatTime())}")
                        Text(
                            "${intake.completedAt?.let { strings.notTakenAt.format(it.formatLocalTime(), reason) } ?: strings.notTaken} · ${strings.reason.format(reason)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun MedicationStatisticsPeriod.startDate(today: LocalDate): LocalDate = when (this) {
    MedicationStatisticsPeriod.TODAY -> today
    MedicationStatisticsPeriod.WEEK -> today.minusDays(6)
    MedicationStatisticsPeriod.MONTH -> today.minusDays(29)
}

private fun MedicationStatisticsPeriod.label(strings: com.mindtrace.ui.i18n.MindTraceStrings): String = when (this) {
    MedicationStatisticsPeriod.TODAY -> strings.today
    MedicationStatisticsPeriod.WEEK -> strings.lastWeek
    MedicationStatisticsPeriod.MONTH -> strings.lastMonth
}

private fun MedicationReminderEntity.isScheduledOn(date: LocalDate): Boolean =
    enabled && occursOn(date) && !date.isBefore(createdAt.atZone(ZoneId.systemDefault()).toLocalDate())

private fun Instant.formatLocalTime(): String = atZone(ZoneId.systemDefault()).toLocalTime().formatTime()

private const val ON_TIME_GRACE_MINUTES = 30L

@Composable
private fun ReminderDialog(
    reminder: MedicationReminderEntity?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String?, List<LocalTime>, Boolean, MedicationRecurrenceType, Set<DayOfWeek>, Int?) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(reminder?.medicationName ?: "") }
    var dosage by rememberSaveable { mutableStateOf(reminder?.dosage ?: "") }
    var reminderTimes by remember(reminder?.id) {
        mutableStateOf(listOf(reminder?.reminderTime ?: LocalTime.of(8, 0)))
    }
    var recurrenceType by remember(reminder?.id) { mutableStateOf(reminder?.recurrenceType ?: MedicationRecurrenceType.DAILY) }
    var selectedWeekdays by remember(reminder?.id) { mutableStateOf(reminder?.selectedWeekdays().orEmpty()) }
    var intervalDays by rememberSaveable { mutableStateOf(reminder?.recurrenceIntervalDays?.toString() ?: "2") }
    var enabled by rememberSaveable { mutableStateOf(reminder?.enabled ?: true) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder == null) strings.createReminder else strings.editReminder) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.medicationName) })
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text(strings.dosageOptional) })
                reminderTimes.forEachIndexed { index, time ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        reminderTimes = reminderTimes.toMutableList().apply {
                                            set(index, LocalTime.of(hour, minute))
                                        }
                                    },
                                    time.hour,
                                    time.minute,
                                    true
                                ).show()
                            }
                        ) {
                            Text("${strings.timeNumber.format(index + 1)}: ${time.formatTime()}")
                        }
                        if (reminder == null && index > 0) {
                            TextButton(onClick = { reminderTimes = reminderTimes.filterIndexed { currentIndex, _ -> currentIndex != index } }) {
                                Text(strings.removeTime)
                            }
                        }
                    }
                }
                if (reminder == null) {
                    if (reminderTimes.size < MAX_REMINDERS_PER_MEDICATION) {
                        TextButton(onClick = { reminderTimes = reminderTimes + reminderTimes.last().plusHours(1) }) {
                            Text(strings.addTime)
                        }
                    } else {
                        Text(strings.maximumDailyReminders, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(strings.recurrence, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = recurrenceType == MedicationRecurrenceType.DAILY,
                        onClick = { recurrenceType = MedicationRecurrenceType.DAILY },
                        label = { Text(strings.recurrenceDaily) }
                    )
                    FilterChip(
                        selected = recurrenceType == MedicationRecurrenceType.WEEKDAYS,
                        onClick = { recurrenceType = MedicationRecurrenceType.WEEKDAYS },
                        label = { Text(strings.recurrenceWeekdays) }
                    )
                }
                FilterChip(
                    selected = recurrenceType == MedicationRecurrenceType.INTERVAL,
                    onClick = { recurrenceType = MedicationRecurrenceType.INTERVAL },
                    label = { Text(strings.recurrenceInterval) }
                )
                if (recurrenceType == MedicationRecurrenceType.WEEKDAYS) {
                    Text(strings.selectWeekdays, style = MaterialTheme.typography.titleSmall)
                    weekdayChoices(strings).chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (weekday, label) ->
                                FilterChip(
                                    selected = weekday in selectedWeekdays,
                                    onClick = {
                                        selectedWeekdays = if (weekday in selectedWeekdays) {
                                            selectedWeekdays - weekday
                                        } else {
                                            selectedWeekdays + weekday
                                        }
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
                if (recurrenceType == MedicationRecurrenceType.INTERVAL) {
                    OutlinedTextField(
                        value = intervalDays,
                        onValueChange = { intervalDays = it },
                        label = { Text(strings.intervalDays) }
                    )
                    Text(strings.intervalStartsToday, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.enabled, modifier = Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedInterval = intervalDays.toIntOrNull()
                when {
                    name.isBlank() -> error = strings.invalidMedicationName
                    recurrenceType == MedicationRecurrenceType.WEEKDAYS && selectedWeekdays.isEmpty() -> error = strings.invalidWeekdays
                    recurrenceType == MedicationRecurrenceType.INTERVAL && (parsedInterval == null || parsedInterval <= 0) -> error = strings.invalidInterval
                    else -> onSave(
                        name.trim(),
                        dosage.trim().takeUnless { it.isBlank() },
                        reminderTimes.distinct().sorted(),
                        enabled,
                        recurrenceType,
                        selectedWeekdays,
                        parsedInterval
                    )
                }
            }) { Text(strings.save) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onDelete?.let { delete ->
                    TextButton(onClick = delete) { Text(strings.delete) }
                }
                TextButton(onClick = onDismiss) { Text(strings.cancel) }
            }
        }
    )
}

private const val MAX_REMINDERS_PER_MEDICATION = 4

private fun weekdayChoices(strings: com.mindtrace.ui.i18n.MindTraceStrings): List<Pair<DayOfWeek, String>> = listOf(
    DayOfWeek.MONDAY to strings.mondayShort,
    DayOfWeek.TUESDAY to strings.tuesdayShort,
    DayOfWeek.WEDNESDAY to strings.wednesdayShort,
    DayOfWeek.THURSDAY to strings.thursdayShort,
    DayOfWeek.FRIDAY to strings.fridayShort,
    DayOfWeek.SATURDAY to strings.saturdayShort,
    DayOfWeek.SUNDAY to strings.sundayShort
)

private fun MedicationReminderEntity.recurrenceLabel(strings: com.mindtrace.ui.i18n.MindTraceStrings): String = when (recurrenceType) {
    MedicationRecurrenceType.DAILY -> strings.recurrenceDaily
    MedicationRecurrenceType.WEEKDAYS -> selectedWeekdays()
        .sortedBy { it.value }
        .joinToString(", ") { weekday -> weekdayChoices(strings).first { it.first == weekday }.second }
    MedicationRecurrenceType.INTERVAL -> strings.everyXDays.format(recurrenceIntervalDays ?: 1)
}

@Preview
@Composable
private fun MedicationPreview() {
    MindTraceTheme {
        MedicationContent(
            reminders = listOf(MedicationReminderEntity(1, "Beispielmedikament", "10 mg", LocalTime.of(8, 0), true, Instant.now())),
            intakesToday = emptyList(),
            intakesInStatisticsPeriod = emptyList(),
            statisticsPeriod = MedicationStatisticsPeriod.TODAY,
            exactAlarmAllowed = false,
            notificationsAllowed = true,
            onAdd = {},
            onEdit = {},
            onEnabled = { _, _ -> },
            onTaken = {},
            onSkip = {},
            onStatisticsPeriodChanged = {},
            openExactAlarmSettings = {}
        )
    }
}
