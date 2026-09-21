package com.mindtrace.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mindtrace.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE recordedAt >= :from ORDER BY recordedAt ASC")
    fun observeSince(from: Instant): Flow<List<MoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: MoodEntryEntity): Long

    @Update suspend fun update(entry: MoodEntryEntity)
    @Delete suspend fun delete(entry: MoodEntryEntity)
}
