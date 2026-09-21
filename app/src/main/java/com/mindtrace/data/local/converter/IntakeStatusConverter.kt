package com.mindtrace.data.local.converter

import androidx.room.TypeConverter
import com.mindtrace.domain.model.IntakeStatus

class IntakeStatusConverter {
    @TypeConverter fun toStatus(value: String?): IntakeStatus? = value?.let { runCatching { IntakeStatus.valueOf(it) }.getOrNull() }
    @TypeConverter fun fromStatus(value: IntakeStatus?): String? = value?.name
}
