package com.mindtrace.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindtrace.ui.i18n.LocalMindTraceStrings

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val strings = LocalMindTraceStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.onboardingTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(strings.onboardingIntro, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OnboardingFeature(Icons.Default.Home, strings.onboardingTodayTitle, strings.onboardingTodayDescription)
        OnboardingFeature(Icons.Default.BarChart, strings.onboardingMoodTitle, strings.onboardingMoodDescription)
        OnboardingFeature(Icons.Default.Medication, strings.onboardingMedicationTitle, strings.onboardingMedicationDescription)
        OnboardingFeature(Icons.Default.Settings, strings.onboardingPrivacyTitle, strings.onboardingPrivacyDescription)

        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
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
