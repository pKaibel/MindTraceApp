package com.mindtrace.data.repository

import com.mindtrace.data.local.dao.MoodEntryDao
import com.mindtrace.data.local.entity.MoodEntryEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class MoodRepository(private val dao: MoodEntryDao) {
    fun observeAll(): Flow<List<MoodEntryEntity>> = dao.observeAll()
    fun observeSince(from: Instant): Flow<List<MoodEntryEntity>> = dao.observeSince(from)

    suspend fun save(moodValue: Int, note: String?) {
        require(moodValue in 1..5) { "Bitte wähle einen Stimmungswert zwischen 1 und 5." }
        dao.insert(MoodEntryEntity(moodValue = moodValue, note = note?.trim().takeUnless { it.isNullOrBlank() }, recordedAt = Instant.now()))
    }

    suspend fun update(entry: MoodEntryEntity) = dao.update(entry)
    suspend fun delete(entry: MoodEntryEntity) = dao.delete(entry)
}
