package com.mindtrace.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moodValue: Int,
    val note: String?,
    val recordedAt: Instant
) {
    init {
        require(moodValue in 1..5) { "Stimmungswert muss zwischen 1 und 5 liegen." }
    }
}
