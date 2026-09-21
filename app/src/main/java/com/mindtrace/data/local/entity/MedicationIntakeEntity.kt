package com.mindtrace.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mindtrace.domain.model.IntakeStatus
import com.mindtrace.domain.model.MedicationSkipReason
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "medication_intakes",
    foreignKeys = [
        ForeignKey(
            entity = MedicationReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("reminderId"),
        Index(value = ["reminderId", "scheduledDateTime"], unique = true)
    ]
)
data class MedicationIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reminderId: Long,
    val scheduledDate: LocalDate,
    val scheduledDateTime: Instant,
    val status: IntakeStatus,
    val completedAt: Instant?,
    val skipReason: MedicationSkipReason? = null,
    val skipNote: String? = null
)
