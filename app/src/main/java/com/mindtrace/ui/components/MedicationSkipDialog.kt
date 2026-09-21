package com.mindtrace.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.domain.model.IntakeStatus
import com.mindtrace.domain.model.MedicationSkipReason
import com.mindtrace.ui.i18n.MindTraceStrings
import java.time.Instant
import java.time.ZoneId

@Composable
fun MedicationSkipDialog(onDismiss: () -> Unit, onConfirm: (MedicationSkipReason, String?) -> Unit) {
    val strings = com.mindtrace.ui.i18n.LocalMindTraceStrings.current
    var selectedReason by remember { mutableStateOf<MedicationSkipReason?>(null) }
    var otherReasonNote by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notTaken) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(strings.notTakenReasonTitle)
                skipReasons(strings).forEach { (reason, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        TextButton(onClick = { selectedReason = reason }) { Text(label) }
                    }
                }
                if (selectedReason == MedicationSkipReason.OTHER) {
                    OutlinedTextField(
                        value = otherReasonNote,
                        onValueChange = { otherReasonNote = it },
                        label = { Text(strings.otherReasonOptional) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedReason != null,
                onClick = { selectedReason?.let { reason -> onConfirm(reason, otherReasonNote) } }
            ) { Text(strings.saveNotTaken) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

fun MedicationIntakeEntity.statusText(strings: MindTraceStrings): String = when (status) {
    IntakeStatus.PENDING -> strings.statusOpen
    IntakeStatus.TAKEN -> completedAt?.let { strings.takenAt.format(it.formatTime()) } ?: strings.statusTaken
    IntakeStatus.SKIPPED -> completedAt?.let {
        val reason = skipReason?.label(strings).orEmpty()
        val detail = if (skipReason == MedicationSkipReason.OTHER && !skipNote.isNullOrBlank()) {
            "$reason: $skipNote"
        } else {
            reason
        }
        strings.notTakenAt.format(it.formatTime(), detail)
    } ?: strings.notTaken
}

fun MedicationSkipReason.label(strings: MindTraceStrings): String = when (this) {
    MedicationSkipReason.FORGOTTEN -> strings.reasonForgotten
    MedicationSkipReason.SIDE_EFFECTS -> strings.reasonSideEffects
    MedicationSkipReason.NOT_AVAILABLE -> strings.reasonNotAvailable
    MedicationSkipReason.MEDICAL_INSTRUCTION -> strings.reasonMedicalInstruction
    MedicationSkipReason.OTHER -> strings.reasonOther
}

private fun Instant.formatTime(): String =
    atZone(ZoneId.systemDefault()).toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))

private fun skipReasons(strings: MindTraceStrings): List<Pair<MedicationSkipReason, String>> = listOf(
    MedicationSkipReason.FORGOTTEN to strings.reasonForgotten,
    MedicationSkipReason.SIDE_EFFECTS to strings.reasonSideEffects,
    MedicationSkipReason.NOT_AVAILABLE to strings.reasonNotAvailable,
    MedicationSkipReason.MEDICAL_INSTRUCTION to strings.reasonMedicalInstruction,
    MedicationSkipReason.OTHER to strings.reasonOther
)
