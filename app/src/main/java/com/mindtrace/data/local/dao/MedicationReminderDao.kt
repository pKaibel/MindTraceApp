package com.mindtrace.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mindtrace.data.local.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders ORDER BY reminderTime ASC, medicationName ASC")
    fun observeAll(): Flow<List<MedicationReminderEntity>>

    @Query("SELECT * FROM medication_reminders WHERE enabled = 1 ORDER BY reminderTime ASC")
    suspend fun enabledReminders(): List<MedicationReminderEntity>

    @Query("SELECT * FROM medication_reminders WHERE id = :id")
    suspend fun getById(id: Long): MedicationReminderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reminder: MedicationReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(reminders: List<MedicationReminderEntity>): List<Long>

    @Update suspend fun update(reminder: MedicationReminderEntity)
    @Delete suspend fun delete(reminder: MedicationReminderEntity)
}
