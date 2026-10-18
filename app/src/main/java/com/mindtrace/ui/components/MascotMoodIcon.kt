package com.mindtrace.ui.components

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mindtrace.util.Mascot
import kotlin.math.min
import kotlin.math.roundToInt

enum class MoodDimension {
    MENTAL,
    PHYSICAL
}

@Composable
fun MascotMoodIcon(
    mascot: Mascot,
    moodValue: Int,
    dimension: MoodDimension,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spriteSheet = remember(mascot) {
        BitmapFactory.decodeResource(context.resources, mascot.moodLevelsResourceId)
    }
    val column = (moodValue - 1).coerceIn(0, MOOD_LEVELS - 1)
    val row = if (dimension == MoodDimension.MENTAL) MENTAL_ROW else PHYSICAL_ROW
    val sprite = remember(mascot, column, row) {
        cropVisibleSprite(spriteSheet, column, row).asImageBitmap()
    }

    Canvas(modifier = modifier) {
        val scale = min(size.width / sprite.width, size.height / sprite.height)
        val destinationWidth = (sprite.width * scale).roundToInt()
        val destinationHeight = (sprite.height * scale).roundToInt()
        drawImage(
            image = sprite,
            dstOffset = IntOffset(
                (size.width.roundToInt() - destinationWidth) / 2,
                (size.height.roundToInt() - destinationHeight) / 2
            ),
            dstSize = IntSize(destinationWidth, destinationHeight),
            filterQuality = FilterQuality.None
        )
    }
}

private fun cropVisibleSprite(spriteSheet: Bitmap, column: Int, row: Int): Bitmap {
    val cellWidth = spriteSheet.width / MOOD_LEVELS
    val cellHeight = spriteSheet.height / MOOD_DIMENSIONS
    val left = column * cellWidth
    val top = row * cellHeight
    val right = if (column == MOOD_LEVELS - 1) spriteSheet.width else left + cellWidth
    val bottom = if (row == MOOD_DIMENSIONS - 1) spriteSheet.height else top + cellHeight
    var visibleLeft = right
    var visibleTop = bottom
    var visibleRight = left
    var visibleBottom = top

    for (y in top until bottom) {
        for (x in left until right) {
            if ((spriteSheet.getPixel(x, y) ushr 24) > ALPHA_THRESHOLD) {
                visibleLeft = min(visibleLeft, x)
                visibleTop = min(visibleTop, y)
                visibleRight = maxOf(visibleRight, x)
                visibleBottom = maxOf(visibleBottom, y)
            }
        }
    }

    if (visibleLeft > visibleRight || visibleTop > visibleBottom) {
        return Bitmap.createBitmap(spriteSheet, left, top, right - left, bottom - top)
    }

    val cropLeft = maxOf(left, visibleLeft - SPRITE_PADDING)
    val cropTop = maxOf(top, visibleTop - SPRITE_PADDING)
    val cropRight = min(right, visibleRight + SPRITE_PADDING + 1)
    val cropBottom = min(bottom, visibleBottom + SPRITE_PADDING + 1)
    return Bitmap.createBitmap(spriteSheet, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop)
}

private const val MOOD_LEVELS = 5
private const val MOOD_DIMENSIONS = 2
private const val MENTAL_ROW = 0
private const val PHYSICAL_ROW = 1
private const val ALPHA_THRESHOLD = 24
private const val SPRITE_PADDING = 4
