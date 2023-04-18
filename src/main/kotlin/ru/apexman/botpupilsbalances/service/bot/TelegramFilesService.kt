package ru.apexman.botpupilsbalances.service.bot

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Document
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.api.objects.Update
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment
import ru.apexman.botpupilsbalances.repository.DocumentRepository
import ru.apexman.botpupilsbalances.repository.PendingBalancePaymentRepository
import ru.apexman.botpupilsbalances.service.notification.TelegramConfiguration
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService
import java.io.File
import java.time.LocalDateTime

//@Service
//TODO: handle dependecy injection
class TelegramFilesService(
    private val thisTelegramBot: ThisTelegramBot,
    private val pendingBalancePaymentRepository: PendingBalancePaymentRepository,
    private val documentRepository: DocumentRepository,
    private val telegramNotificationService: TelegramNotificationService,
    private val telegramConfiguration: TelegramConfiguration,
) {
    private val logger = LoggerFactory.getLogger(ThisTelegramBot::class.java)

    fun saveDocument(update: Update, tDocument: Document, contact: Contact, commandRequester: String) {
        try {
            val docBytes = download(tDocument.fileId)!!.readBytes()
            val pendingBalancePayment =
                save(
                    tDocument.fileName ?: LocalDateTime.now().toString(),
                    docBytes,
                    tDocument.mimeType ?: MediaType.TEXT_PLAIN_VALUE,
                    contact,
                    commandRequester
                )
            val forwardedMessage = forwardMessage(update)
            //todo:
        //            ✅+28
        } catch (e: Throwable) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e, update)
            )
        }
    }

    fun savePhoto(update: Update, photos: MutableList<PhotoSize?>, contact: Contact, commandRequester: String) {
        try {
            val photo = photos.maxBy { it?.fileSize ?: 0 }!!
            val docBytes = download(photo.fileId)!!.readBytes()
            val pendingBalancePayment =
                save(LocalDateTime.now().toString(), docBytes, MediaType.IMAGE_JPEG_VALUE, contact, commandRequester)
            val forwardedMessage = forwardMessage(update)
        } catch (e: Throwable) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e, update)
            )
        }
    }

    private fun save(
        fileName: String,
        docBytes: ByteArray,
        mimeTypeValue: String,
        contact: Contact,
        commandRequester: String,
    ): PendingBalancePayment {
        val contentHashCode = docBytes.contentHashCode()
        val document = ru.apexman.botpupilsbalances.entity.Document(
            documentName = fileName,
            documentValue = docBytes,
            documentHash = contentHashCode,
            documentType = mimeTypeValue,
        )
        val pendingBalancePayment = PendingBalancePayment(contact, commandRequester, contact.student, document)
        documentRepository.save(document)
        return pendingBalancePaymentRepository.save(pendingBalancePayment)
    }

    private fun download(fileId: String): File? {
        val uploadFileRequest = GetFile.builder()
            .fileId(fileId)
            .build()
        val execute = thisTelegramBot.execute(uploadFileRequest)
        return thisTelegramBot.downloadFile(execute)
    }

    private fun forwardMessage(update: Update): Message? {
        val forwardMessage = ForwardMessage.builder()
            .fromChatId(update.message.chatId)
            .messageId(update.message.messageId)
            .chatId(telegramConfiguration.collectingReceiptsChatId)
            .protectContent(false)
            .build()
        return thisTelegramBot.execute(forwardMessage)
    }

}