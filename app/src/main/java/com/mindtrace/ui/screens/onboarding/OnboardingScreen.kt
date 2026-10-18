package com.mindtrace.ui.screens.onboarding

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindtrace.ui.i18n.LocalMindTraceStrings
import com.mindtrace.util.Mascot
import com.mindtrace.util.OnboardingReason

@Composable
fun OnboardingScreen(
    reason: OnboardingReason,
    initialName: String?,
    initialMascot: Mascot?,
    onFinish: (String, Mascot) -> Unit
) {
    var showProfilePage by rememberSaveable { mutableStateOf(false) }

    if (showProfilePage) {
        OnboardingProfile(
            initialName = initialName,
            initialMascot = initialMascot,
            onFinish = onFinish
        )
    } else {
        OnboardingOverview(reason = reason, onContinue = { showProfilePage = true })
    }
}

@Composable
private fun OnboardingOverview(
    reason: OnboardingReason,
    onContinue: () -> Unit
) {
    val strings = LocalMindTraceStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.onboardingTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = when (reason) {
                OnboardingReason.FIRST_INSTALL -> strings.onboardingFirstInstallIntro
                OnboardingReason.APP_UPDATE -> strings.onboardingUpdateIntro
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OnboardingFeature(Icons.Default.Home, strings.onboardingTodayTitle, strings.onboardingTodayDescription)
        OnboardingFeature(Icons.Default.BarChart, strings.onboardingMoodTitle, strings.onboardingMoodDescription)
        OnboardingFeature(Icons.Default.Medication, strings.onboardingMedicationTitle, strings.onboardingMedicationDescription)
        OnboardingFeature(Icons.Default.Settings, strings.onboardingPrivacyTitle, strings.onboardingPrivacyDescription)

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.onboardingContinue)
        }
    }
}

@Composable
private fun OnboardingProfile(
    initialName: String?,
    initialMascot: Mascot?,
    onFinish: (String, Mascot) -> Unit
) {
    val strings = LocalMindTraceStrings.current
    var name by rememberSaveable { mutableStateOf(initialName.orEmpty()) }
    var selectedMascotName by rememberSaveable { mutableStateOf(initialMascot?.name) }
    val selectedMascot = Mascot.values().firstOrNull { it.name == selectedMascotName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.onboardingProfileTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(strings.onboardingProfileDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text(strings.onboardingNamePrompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(strings.onboardingNameLabel) },
            singleLine = true
        )

        Text(strings.mascot, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        Text(strings.mascotDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Mascot.values().toList().chunked(2).forEach { mascots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                mascots.forEach { mascot ->
                    OnboardingMascotChoice(
                        mascot = mascot,
                        selected = selectedMascot == mascot,
                        onClick = { selectedMascotName = mascot.name },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (mascots.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Button(
            onClick = { onFinish(name.trim(), selectedMascot!!) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && selectedMascot != null
        ) {
            Text(strings.onboardingStart)
        }
    }
}

@Composable
private fun OnboardingFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OnboardingMascotChoice(
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
