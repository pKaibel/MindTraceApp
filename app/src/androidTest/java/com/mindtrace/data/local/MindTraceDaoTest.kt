package com.mindtrace.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.local.entity.MoodEntryEntity
import com.mindtrace.domain.model.IntakeStatus
import com.mindtrace.domain.model.MedicationSkipReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class MindTraceDaoTest {
    private lateinit var db: MindTraceDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MindTraceDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun savesAndLoadsMoodEntries() = runTest {
        db.moodEntryDao().insert(MoodEntryEntity(moodValue = 4, note = "Gut", recordedAt = Instant.now()))
        assertEquals(1, db.moodEntryDao().observeAll().first().size)
    }

    @Test
    fun savesAndLoadsMedicationReminders() = runTest {
        db.medicationReminderDao().insert(MedicationReminderEntity(medicationName = "Test", dosage = "5 mg", reminderTime = LocalTime.of(9, 0), enabled = true, createdAt = Instant.now()))
        assertEquals(1, db.medicationReminderDao().observeAll().first().size)
    }

    @Test
    fun savesMultipleDailyTimesForTheSameMedication() = runTest {
        db.medicationReminderDao().insertAll(
            listOf(
                MedicationReminderEntity(medicationName = "Test", dosage = "5 mg", reminderTime = LocalTime.of(8, 0), enabled = true, createdAt = Instant.now()),
                MedicationReminderEntity(medicationName = "Test", dosage = "5 mg", reminderTime = LocalTime.of(20, 0), enabled = true, createdAt = Instant.now())
            )
        )

        val reminders = db.medicationReminderDao().observeAll().first()
        assertEquals(2, reminders.size)
        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)), reminders.map { it.reminderTime })
    }

    @Test
    fun preventsDuplicateIntakesForSameSchedule() = runTest {
        val reminderId = db.medicationReminderDao().insert(MedicationReminderEntity(medicationName = "Test", dosage = null, reminderTime = LocalTime.of(9, 0), enabled = true, createdAt = Instant.now()))
        val scheduled = Instant.parse("2026-07-31T07:00:00Z")
        val first = MedicationIntakeEntity(reminderId = reminderId, scheduledDate = LocalDate.of(2026, 7, 31), scheduledDateTime = scheduled, status = IntakeStatus.PENDING, completedAt = null)
        val second = first.copy(status = IntakeStatus.TAKEN, completedAt = Instant.now())
        assertEquals(1L, db.medicationIntakeDao().insertIgnore(first))
        assertEquals(-1L, db.medicationIntakeDao().insertIgnore(second))
    }

    @Test
    fun markTakenUpdatesExistingPendingIntake() = runTest {
        val reminderId = db.medicationReminderDao().insert(MedicationReminderEntity(medicationName = "Test", dosage = null, reminderTime = LocalTime.of(9, 0), enabled = true, createdAt = Instant.now()))
        val date = LocalDate.of(2026, 7, 31)
        val scheduled = Instant.parse("2026-07-31T07:00:00Z")
        db.medicationIntakeDao().insertIgnore(MedicationIntakeEntity(reminderId = reminderId, scheduledDate = date, scheduledDateTime = scheduled, status = IntakeStatus.PENDING, completedAt = null))
        db.medicationIntakeDao().markTaken(reminderId, date, scheduled, Instant.now())
        assertEquals(IntakeStatus.TAKEN, db.medicationIntakeDao().observeForDate(date).first().single().status)
    }

    @Test
    fun markSkippedStoresReasonAndCompletionTime() = runTest {
        val reminderId = db.medicationReminderDao().insert(MedicationReminderEntity(medicationName = "Test", dosage = null, reminderTime = LocalTime.of(9, 0), enabled = true, createdAt = Instant.now()))
        val date = LocalDate.of(2026, 7, 31)
        val scheduled = Instant.parse("2026-07-31T07:00:00Z")
        val completedAt = Instant.parse("2026-07-31T08:15:00Z")

        db.medicationIntakeDao().markSkipped(reminderId, date, scheduled, completedAt, MedicationSkipReason.OTHER, "War auf Reisen")

        val intake = db.medicationIntakeDao().observeForDate(date).first().single()
        assertEquals(IntakeStatus.SKIPPED, intake.status)
        assertEquals(MedicationSkipReason.OTHER, intake.skipReason)
        assertEquals("War auf Reisen", intake.skipNote)
        assertEquals(completedAt, intake.completedAt)
    }
}
