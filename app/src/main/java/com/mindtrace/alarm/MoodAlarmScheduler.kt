package com.mindtrace.alarm

import java.time.LocalTime

interface MoodAlarmScheduler {
    fun schedule(time: LocalTime): Boolean
    fun cancel()
}
