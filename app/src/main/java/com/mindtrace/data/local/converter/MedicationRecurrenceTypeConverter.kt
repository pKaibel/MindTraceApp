package com.mindtrace.data.local.converter

import androidx.room.TypeConverter
import com.mindtrace.domain.model.MedicationRecurrenceType

class MedicationRecurrenceTypeConverter {
    @TypeConverter
    fun toType(value: String?): MedicationRecurrenceType? = value?.let(MedicationRecurrenceType::valueOf)

    @TypeConverter
    fun fromType(value: MedicationRecurrenceType?): String? = value?.name
}
