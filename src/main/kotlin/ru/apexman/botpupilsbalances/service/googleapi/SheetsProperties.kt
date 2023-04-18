package ru.apexman.botpupilsbalances.service.googleapi

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "google-sheets")
data class SheetsProperties(
    val sheetId: String,
    val mainTableName: String,
    val pullTableName: String,
)
