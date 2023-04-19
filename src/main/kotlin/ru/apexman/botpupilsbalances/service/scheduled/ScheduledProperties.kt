package ru.apexman.botpupilsbalances.service.scheduled

import org.springframework.boot.context.properties.ConfigurationProperties

//@ConfigurationProperties(prefix = "scheduled")
data class ScheduledProperties(
    val telegramApiUrl: String,
    val token: String,
    val isMonitoring: Boolean,
    val monitoringChatId: String,
    val collectingReceiptsChatId: String,
    val adminsChatId: String,
)
