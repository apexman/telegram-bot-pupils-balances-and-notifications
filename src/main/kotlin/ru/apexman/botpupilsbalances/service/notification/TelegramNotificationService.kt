package ru.apexman.botpupilsbalances.service.notification

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.telegram.telegrambots.meta.api.objects.Update
import ru.apexman.botpupilsbalances.dto.TelegramDocumentDto
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

@Service
class TelegramNotificationService(
    private val telegramConfiguration: TelegramConfiguration,
    private val telegramWebClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(TelegramNotificationService::class.java)

    @PostConstruct
    fun postConstruct() {
        val text = "Application started"
        logger.info(text)
        sendMonitoring(text)
    }

    @PreDestroy
    fun preDestroy() {
        val text = "Application shutdown"
        logger.info(text)
        sendMonitoring(text)
    }

    fun sendToAdminsChat(text: String, document: TelegramDocumentDto? = null) {
        try {
            sendMessage(telegramConfiguration.adminsChatId, text, document)
        } catch (e: Exception) {
            val errorMessage = "Failed to send to chat ${telegramConfiguration.adminsChatId}, text: $text"
            logger.error(errorMessage, e)
            sendMonitoring(errorMessage, buildTelegramDocumentDto(e))
        }
    }

    fun sendToCollectingReceiptsChat(text: String, document: TelegramDocumentDto? = null) {
        try {
            sendMessage(telegramConfiguration.collectingReceiptsChatId, text, document)
        } catch (e: Exception) {
            val errorMessage = "Failed to send to chat ${telegramConfiguration.collectingReceiptsChatId}, text: $text"
            logger.error(errorMessage, e)
            sendMonitoring(errorMessage, buildTelegramDocumentDto(e))
        }
    }

    fun sendMonitoring(woPrefixText: String, document: TelegramDocumentDto? = null) {
        val text = "‼️ $woPrefixText"
        try {
            if (telegramConfiguration.isMonitoring) {
                sendMessage(telegramConfiguration.monitoringChatId, text, document)
            } else {
                logger.info("Monitoring disabled, text: $text")
            }
        } catch (e: Exception) {
            logger.error("‼️ ️Failed to send to monitoring chat, text: $text", e)
        }
    }

    private fun sendMessage(
        chatId: String,
        text: String,
        document: TelegramDocumentDto? = null,
    ): String? {
        logger.debug("Sending message chatId=$chatId, text=$text")
        val response = if (document == null) {
            sendWODocument(chatId, text)
        } else {
            sendWithDocument(chatId, text, document)
        }
        logger.debug(response)
        return response
    }

    private fun sendWODocument(chatId: String, messageText: String): String? {
        return telegramWebClient
            .post()
            .uri {
                it
                    .path("/bot{httpToken}/{method}")
                    .queryParam("text", URLEncoder.encode(messageText, UTF_8))
                    .queryParam("chat_id", chatId)
                it.build(telegramConfiguration.token, "sendMessage")
            }.retrieve()
            .onStatus(
                HttpStatus.BAD_REQUEST::equals
            ) { response -> response.bodyToMono(String::class.java).map { Exception(it) } }
            .bodyToMono(String::class.java)
            .block()
    }

    private fun sendWithDocument(chatId: String, messageText: String, document: TelegramDocumentDto): String? {
        val multipartBodyBuilder = MultipartBodyBuilder()
        multipartBodyBuilder
            .part("document", ByteArrayResource(document.content.toByteArray()), MediaType.APPLICATION_OCTET_STREAM)
            .filename(document.name)
            .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"document\"")
        return telegramWebClient
            .post()
            .uri {
                it
                    .path("/bot{httpToken}/{method}")
                    .queryParam("caption", URLEncoder.encode(messageText, UTF_8))
                    .queryParam("chat_id", chatId)
                it.build(telegramConfiguration.token, "sendDocument")
            }
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build())).retrieve()
            .onStatus(
                HttpStatus.BAD_REQUEST::equals
            ) { response -> response.bodyToMono(String::class.java).map { Exception(it) } }
            .bodyToMono(String::class.java)
            .block()
    }

    companion object {
        fun buildTelegramDocumentDto(exception: Throwable, update: Update? = null): TelegramDocumentDto {
            val updateString = if (update != null) "$update\n\n--------\n\n" else ""
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            exception.printStackTrace(printWriter)
            printWriter.close()
            return TelegramDocumentDto(name = "exception.log", content = updateString + stringWriter.toString())
        }
    }

}
