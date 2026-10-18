package com.mindtrace.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Movie
import android.os.SystemClock
import android.view.View
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mindtrace.util.Mascot
import kotlin.math.min

@Composable
fun AnimatedMascot(mascot: Mascot, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context -> GifMascotView(context).apply { setAnimation(mascot.idleResourceId) } },
        update = { view -> view.setAnimation(mascot.idleResourceId) }
    )
}

private class GifMascotView(context: Context) : View(context) {
    private var animation: Movie? = null
    private var animationResourceId: Int = 0
    private var animationStartedAt = 0L

    fun setAnimation(@RawRes resourceId: Int) {
        if (animationResourceId == resourceId) return
        animationResourceId = resourceId
        animation = resources.openRawResource(resourceId).use { stream -> Movie.decodeStream(stream) }
        animationStartedAt = 0L
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val movie = animation ?: return
        if (movie.width() <= 0 || movie.height() <= 0) return

        val now = SystemClock.uptimeMillis()
        if (animationStartedAt == 0L) animationStartedAt = now
        val duration = movie.duration().takeIf { it > 0 } ?: DEFAULT_FRAME_DURATION_MS
        movie.setTime(((now - animationStartedAt) % duration).toInt())

        val scale = min(width.toFloat() / movie.width(), height.toFloat() / movie.height())
        val contentWidth = movie.width() * scale
        val contentHeight = movie.height() * scale
        canvas.save()
        canvas.translate((width - contentWidth) / 2f, (height - contentHeight) / 2f)
        canvas.scale(scale, scale)
        movie.draw(canvas, 0f, 0f)
        canvas.restore()
        postInvalidateOnAnimation()
    }

    private companion object {
        const val DEFAULT_FRAME_DURATION_MS = 1_000
    }
}
