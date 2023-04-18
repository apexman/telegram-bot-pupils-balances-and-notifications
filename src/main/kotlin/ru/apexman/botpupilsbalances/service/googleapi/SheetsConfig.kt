package ru.apexman.botpupilsbalances.service.googleapi

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.security.GeneralSecurityException

@Configuration
class SheetsConfig {
    private val applicationName = "students google sheets"

    @Bean
    @Throws(IOException::class, GeneralSecurityException::class)
    fun getSheets(): Sheets {
        val googleCredentials = getCredentials()
        return Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(googleCredentials)
        )
            .setApplicationName(applicationName)
            .build()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun getCredentials(): GoogleCredentials {
        val inputStream =
            SheetsConfig::class.java.getResourceAsStream("/google-sa-credentials-local.json")
                ?: SheetsConfig::class.java.getResourceAsStream("/google-sa-credentials.json")
        val scopes = listOf(SheetsScopes.SPREADSHEETS)
        return GoogleCredentials.fromStream(inputStream).createScoped(scopes)
    }
}