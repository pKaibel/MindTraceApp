package com.mindtrace.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moodValue: Int,
    val note: String?,
    val recordedAt: Instant,
    val physicalWellbeing: Int = moodValue,
    val mentalWellbeing: Int = moodValue
) {
    init {
        require(moodValue in 1..5) { "Stimmungswert muss zwischen 1 und 5 liegen." }
        require(physicalWellbeing in 1..5) { "Körperliches Wohlbefinden muss zwischen 1 und 5 liegen." }
        require(mentalWellbeing in 1..5) { "Seelisches Wohlbefinden muss zwischen 1 und 5 liegen." }
    }
}
