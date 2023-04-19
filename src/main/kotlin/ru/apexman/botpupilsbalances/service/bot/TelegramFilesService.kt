package ru.apexman.botpupilsbalances.service.bot

import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Document
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment
import ru.apexman.botpupilsbalances.repository.DocumentRepository
import ru.apexman.botpupilsbalances.repository.PendingBalancePaymentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.IncreaseBalanceHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramProperties
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
    private val telegramProperties: TelegramProperties,
    private val increaseBalanceHandler: IncreaseBalanceHandler,
) {
    private val logger = LoggerFactory.getLogger(ThisTelegramBot::class.java)

    fun saveDocument(
        update: Update,
        botSession: Session?,
        tDocument: Document,
        contact: Contact,
        commandRequester: String,
    ) {
        saveFileAndForward(
            tDocument.fileId,
            tDocument.fileName ?: LocalDateTime.now().toString(),
            tDocument.mimeType ?: MediaType.TEXT_PLAIN_VALUE,
            contact,
            commandRequester,
            update,
            botSession
        )
    }

    fun savePhoto(
        update: Update,
        botSession: Session?,
        photos: MutableList<PhotoSize?>,
        contact: Contact,
        commandRequester: String,
    ) {
        val photo = photos.maxBy { it?.fileSize ?: 0 }!!
        saveFileAndForward(
            photo.fileId,
            LocalDateTime.now().toString(),
            MediaType.IMAGE_JPEG_VALUE,
            contact,
            commandRequester,
            update,
            botSession
        )
    }

    private fun saveFileAndForward(
        fileId: String,
        fileName: String,
        mimeTypeValue: String,
        contact: Contact,
        commandRequester: String,
        update: Update,
        botSession: Session?,
    ) {
        try {
            val docBytes = download(fileId)!!.readBytes()
            val pendingBalancePayment =
                save(
                    fileName,
                    docBytes,
                    mimeTypeValue,
                    contact,
                    commandRequester
                )
            sendToCollectionChat(update, pendingBalancePayment)
        } catch (e: Throwable) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e, update, botSession)
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

    private fun sendToCollectionChat(update: Update, pendingBalancePayment: PendingBalancePayment) {
        val forwardMessage = forwardMessage(update)
        val student = pendingBalancePayment.student
        val confirmationButton = InlineKeyboardButton.builder()
            .text("✅+28")
            .callbackData("${increaseBalanceHandler.getCommandName()} ${student.googleId} ${pendingBalancePayment.id}")
            .build()
        val inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
            .keyboard(listOf(listOf(confirmationButton)))
            .build()

        val message = SendMessage.builder()
            .chatId(telegramProperties.collectingReceiptsChatId)
            .text(
                """
                ID: ${student.googleId}
                Имя: ${student.fullUserName}
            """.trimIndent()
            )
            .replyToMessageId(forwardMessage.messageId)
            .replyMarkup(inlineKeyboardMarkup)
            .build()
        thisTelegramBot.execute(message)
    }

    private fun forwardMessage(update: Update): Message {
        val forwardMessage = ForwardMessage.builder()
            .fromChatId(update.message.chatId)
            .messageId(update.message.messageId)
            .chatId(telegramProperties.collectingReceiptsChatId)
            .protectContent(false)
            .build()
        return thisTelegramBot.execute(forwardMessage)
    }

}