package com.mindtrace.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindtrace.data.local.converter.DateTimeConverters
import com.mindtrace.data.local.converter.IntakeStatusConverter
import com.mindtrace.data.local.converter.MedicationRecurrenceTypeConverter
import com.mindtrace.data.local.converter.MedicationSkipReasonConverter
import com.mindtrace.data.local.dao.MedicationIntakeDao
import com.mindtrace.data.local.dao.MedicationReminderDao
import com.mindtrace.data.local.dao.MoodEntryDao
import com.mindtrace.data.local.entity.MedicationIntakeEntity
import com.mindtrace.data.local.entity.MedicationReminderEntity
import com.mindtrace.data.local.entity.MoodEntryEntity

@Database(
    entities = [
        MoodEntryEntity::class,
        MedicationReminderEntity::class,
        MedicationIntakeEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(
    DateTimeConverters::class,
    IntakeStatusConverter::class,
    MedicationRecurrenceTypeConverter::class,
    MedicationSkipReasonConverter::class
)
abstract class MindTraceDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun medicationReminderDao(): MedicationReminderDao
    abstract fun medicationIntakeDao(): MedicationIntakeDao

    companion object {
        fun create(context: Context): MindTraceDatabase =
            Room.databaseBuilder(context, MindTraceDatabase::class.java, "mindtrace.db")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration(false)
                .build()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE medication_reminders ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'DAILY'")
                database.execSQL("ALTER TABLE medication_reminders ADD COLUMN recurrenceWeekdays TEXT")
                database.execSQL("ALTER TABLE medication_reminders ADD COLUMN recurrenceIntervalDays INTEGER")
                database.execSQL("ALTER TABLE medication_reminders ADD COLUMN recurrenceStartDate TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE medication_intakes ADD COLUMN skipReason TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE medication_intakes ADD COLUMN skipNote TEXT")
            }
        }
    }
}
