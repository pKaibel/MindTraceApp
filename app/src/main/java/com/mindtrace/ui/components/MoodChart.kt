package com.mindtrace.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.ui.i18n.LocalMindTraceStrings

@Composable
fun MoodChart(
    entries: List<MoodEntryEntity>,
    valueFor: (MoodEntryEntity) -> Int,
    modifier: Modifier = Modifier
) {
    val strings = LocalMindTraceStrings.current
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    if (entries.isEmpty()) {
        Box(modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            Text(strings.noMoodValues)
        }
        return
    }
    Canvas(modifier.fillMaxWidth().height(220.dp)) {
        val left = 36f
        val right = size.width - 12f
        val top = 12f
        val bottom = size.height - 28f
        repeat(5) { index ->
            val y = bottom - index * ((bottom - top) / 4f)
            drawLine(outline, Offset(left, y), Offset(right, y), strokeWidth = 1.5f)
        }
        val sorted = entries.sortedBy { it.recordedAt }
        val min = sorted.first().recordedAt.toEpochMilli()
        val span = (sorted.last().recordedAt.toEpochMilli() - min).coerceAtLeast(1)
        fun point(entry: MoodEntryEntity): Offset {
            val x = left + ((entry.recordedAt.toEpochMilli() - min).toFloat() / span) * (right - left)
            val y = bottom - ((valueFor(entry) - 1).toFloat() / 4f) * (bottom - top)
            return Offset(x, y)
        }
        if (sorted.size == 1) {
            drawCircle(primary, radius = 7f, center = point(sorted.first()))
        } else {
            val path = Path()
            sorted.forEachIndexed { index, entry ->
                val p = point(entry)
                if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, primary, style = Stroke(width = 5f))
            sorted.forEach { drawCircle(primary, radius = 5f, center = point(it)) }
        }
    }
}
