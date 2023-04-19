package ru.apexman.botpupilsbalances.service.notification

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram")
data class TelegramProperties(
    val telegramApiUrl: String,
    val token: String,
    val isMonitoring: Boolean,
    val monitoringChatId: String,
    val collectingReceiptsChatId: String,
    val adminsChatId: String,
)
