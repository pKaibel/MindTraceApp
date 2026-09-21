package com.mindtrace.data.local.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class DateTimeConverters {
    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
    @TypeConverter fun localDateToString(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
    @TypeConverter fun localTimeToMinute(value: LocalTime?): Int? = value?.let { it.hour * 60 + it.minute }
    @TypeConverter fun minuteToLocalTime(value: Int?): LocalTime? =
        value?.let { LocalTime.of(it / 60, it % 60) }
}
