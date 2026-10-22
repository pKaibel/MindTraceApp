package com.mindtrace.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.ui.i18n.LocalMindTraceStrings

@Composable
fun MoodChart(
    entries: List<MoodEntryEntity>,
    physicalLabel: String,
    mentalLabel: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalMindTraceStrings.current
    val physicalColor = MaterialTheme.colorScheme.primary
    val mentalColor = MaterialTheme.colorScheme.tertiary
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ChartLegendItem(physicalLabel, physicalColor)
            ChartLegendItem(mentalLabel, mentalColor)
        }
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text(strings.noMoodValues)
            }
            return@Column
        }
        Canvas(Modifier.fillMaxWidth().height(220.dp)) {
            val left = 36f
            val right = size.width - 12f
            val top = 12f
            val bottom = size.height - 28f
            repeat(5) { index ->
                val y = bottom - index * ((bottom - top) / 4f)
                drawLine(outline, Offset(left, y), Offset(right, y), strokeWidth = 1.5f)
            }

            val sorted = entries.sortedBy { it.recordedAt }
            val start = sorted.first().recordedAt.toEpochMilli()
            val span = (sorted.last().recordedAt.toEpochMilli() - start).coerceAtLeast(1)
            fun point(entry: MoodEntryEntity, value: Int): Offset {
                val x = left + ((entry.recordedAt.toEpochMilli() - start).toFloat() / span) * (right - left)
                val y = bottom - ((value - 1).toFloat() / 4f) * (bottom - top)
                return Offset(x, y)
            }
            fun drawSeries(color: Color, valueFor: (MoodEntryEntity) -> Int) {
                if (sorted.size == 1) {
                    drawCircle(color, radius = 7f, center = point(sorted.first(), valueFor(sorted.first())))
                    return
                }
                val path = Path()
                sorted.forEachIndexed { index, entry ->
                    val seriesPoint = point(entry, valueFor(entry))
                    if (index == 0) path.moveTo(seriesPoint.x, seriesPoint.y) else path.lineTo(seriesPoint.x, seriesPoint.y)
                }
                drawPath(path, color, style = Stroke(width = 5f))
                sorted.forEach { entry -> drawCircle(color, radius = 5f, center = point(entry, valueFor(entry))) }
            }

            drawSeries(physicalColor) { it.physicalWellbeing }
            drawSeries(mentalColor) { it.mentalWellbeing }
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.size(10.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
