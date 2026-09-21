package com.mindtrace.alarm

import com.mindtrace.data.local.entity.MedicationReminderEntity

interface MedicationAlarmScheduler {
    fun schedule(reminder: MedicationReminderEntity): Boolean
    fun cancel(reminderId: Long)
}
