package ru.apexman.botpupilsbalances.service.jobs

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalTime
import java.util.*

@ConfigurationProperties(prefix = "scheduled")
data class ScheduledProperties(
    val userTimeZone: TimeZone,
    val informOverdueStartTime: LocalTime,
    val informOverdueIntervalMinutes: Int,
    val balanceDecreaserStartTime: LocalTime,
    val balanceDecreaserIntervalMinutes: Int,
)
