package com.mindtrace.util

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.mindtrace.R

enum class Mascot(
    val displayName: String,
    @DrawableRes val portraitResourceId: Int,
    @RawRes val idleResourceId: Int,
    @DrawableRes val moodLevelsResourceId: Int
) {
    LUMI("Lumi", R.drawable.mascot_lumi_portrait, R.raw.mascot_lumi_idle, R.drawable.mascot_lumi_mood_levels),
    NARI("Nari", R.drawable.mascot_nari_portrait, R.raw.mascot_nari_idle, R.drawable.mascot_nari_mood_levels),
    PIXO("Pixo", R.drawable.mascot_pixo_portrait, R.raw.mascot_pixo_idle, R.drawable.mascot_pixo_mood_levels),
    SPARKY("Sparky", R.drawable.mascot_sparky_portrait, R.raw.mascot_sparky_idle, R.drawable.mascot_sparky_mood_levels),
    ZENN("Zenn", R.drawable.mascot_zenn_portrait, R.raw.mascot_zenn_idle, R.drawable.mascot_zenn_mood_levels)
}
