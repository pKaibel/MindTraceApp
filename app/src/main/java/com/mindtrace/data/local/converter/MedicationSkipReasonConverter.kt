package com.mindtrace.data.local.converter

import androidx.room.TypeConverter
import com.mindtrace.domain.model.MedicationSkipReason

class MedicationSkipReasonConverter {
    @TypeConverter
    fun toReason(value: String?): MedicationSkipReason? = value?.let(MedicationSkipReason::valueOf)

    @TypeConverter
    fun fromReason(value: MedicationSkipReason?): String? = value?.name
}
