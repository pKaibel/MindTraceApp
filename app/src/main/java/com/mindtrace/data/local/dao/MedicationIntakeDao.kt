package com.mindtrace.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.domain.model.IntakeStatus
import com.mindtrace.domain.model.MedicationSkipReason
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface MedicationIntakeDao {
    @Query("SELECT * FROM medication_intakes WHERE scheduledDate = :date")
    fun observeForDate(date: LocalDate): Flow<List<MedicationIntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE scheduledDate BETWEEN :startDate AND :endDate")
    fun observeForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<MedicationIntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE reminderId = :reminderId AND scheduledDateTime = :scheduledDateTime LIMIT 1")
    suspend fun getForSchedule(reminderId: Long, scheduledDateTime: Instant): MedicationIntakeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: MedicationIntakeEntity): Long

    @Update suspend fun update(entity: MedicationIntakeEntity)

    @Transaction
    suspend fun markTaken(reminderId: Long, date: LocalDate, scheduledDateTime: Instant, completedAt: Instant) {
        val existing = getForSchedule(reminderId, scheduledDateTime)
        if (existing == null) {
            insertIgnore(
                MedicationIntakeEntity(
                    reminderId = reminderId,
                    scheduledDate = date,
                    scheduledDateTime = scheduledDateTime,
                    status = IntakeStatus.TAKEN,
                    completedAt = completedAt
                )
            )
        } else if (existing.status != IntakeStatus.TAKEN) {
            update(existing.copy(status = IntakeStatus.TAKEN, completedAt = completedAt, skipReason = null, skipNote = null))
        }
    }

    @Transaction
    suspend fun markSkipped(
        reminderId: Long,
        date: LocalDate,
        scheduledDateTime: Instant,
        completedAt: Instant,
        reason: MedicationSkipReason,
        note: String?
    ) {
        val existing = getForSchedule(reminderId, scheduledDateTime)
        if (existing == null) {
            insertIgnore(
                MedicationIntakeEntity(
                    reminderId = reminderId,
                    scheduledDate = date,
                    scheduledDateTime = scheduledDateTime,
                    status = IntakeStatus.SKIPPED,
                    completedAt = completedAt,
                    skipReason = reason,
                    skipNote = note
                )
            )
        } else if (existing.status != IntakeStatus.SKIPPED || existing.skipReason != reason || existing.skipNote != note) {
            update(existing.copy(status = IntakeStatus.SKIPPED, completedAt = completedAt, skipReason = reason, skipNote = note))
        }
    }
}
