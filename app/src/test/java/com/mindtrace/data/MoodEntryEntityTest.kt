package com.mindtrace.data

import com.mindtrace.data.local.entity.MoodEntryEntity
import org.junit.Test
import java.time.Instant

class MoodEntryEntityTest {
    @Test(expected = IllegalArgumentException::class)
    fun moodValueBelowRangeIsRejected() {
        MoodEntryEntity(moodValue = 0, note = null, recordedAt = Instant.now())
    }

    @Test(expected = IllegalArgumentException::class)
    fun moodValueAboveRangeIsRejected() {
        MoodEntryEntity(moodValue = 6, note = null, recordedAt = Instant.now())
    }

    @Test(expected = IllegalArgumentException::class)
    fun physicalWellbeingBelowRangeIsRejected() {
        MoodEntryEntity(moodValue = 3, physicalWellbeing = 0, note = null, recordedAt = Instant.now())
    }

    @Test(expected = IllegalArgumentException::class)
    fun mentalWellbeingAboveRangeIsRejected() {
        MoodEntryEntity(moodValue = 3, mentalWellbeing = 6, note = null, recordedAt = Instant.now())
    }
}
