package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.repository.DocumentRepository
import ru.apexman.botpupilsbalances.repository.PendingBalancePaymentRepository
import ru.apexman.botpupilsbalances.service.bot.TelegramFilesService
import ru.apexman.botpupilsbalances.service.bot.ThisTelegramBot
import ru.apexman.botpupilsbalances.service.notification.TelegramConfiguration
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService

/**
 * Хендлер для загрузки файлов в систему с последующим оповещением чата менеджеров для обработки запроса
 */
@Component
class PendingBalancePaymentHandler(
    private val contactRepository: ContactRepository,
    private val pendingBalancePaymentRepository: PendingBalancePaymentRepository,
    private val documentRepository: DocumentRepository,
    private val telegramNotificationService: TelegramNotificationService,
    private val telegramConfiguration: TelegramConfiguration,
) : TelegramMessageHandler, PrivateChatHandler {

    private var bot: ThisTelegramBot? = null
    private var telegramFilesService: TelegramFilesService? = null

    fun setBot(thisTelegramBot: ThisTelegramBot) {
        this.bot = thisTelegramBot
        this.telegramFilesService = TelegramFilesService(
            bot!!,
            pendingBalancePaymentRepository,
            documentRepository,
            telegramNotificationService,
            telegramConfiguration,
        )
    }

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun canHandle(update: Update, botSession: Session?, botUsername: String, telegramConfiguration: TelegramConfiguration): Boolean {
        if (update.hasMessage()) {
            return checkPermissions(update, botUsername, telegramConfiguration)
                    && (update.message.hasPhoto()
                    || update.message.hasDocument())
        }
        return false
    }

    override fun handle(update: Update, botSession: Session?): PartialBotApiMethod<Message> {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: отправляет скриншот/документ квитанции об оплате с комментарием <public_id> ученика")
                .build()
        }
        val tgId = update.message.from.id
        val contacts =
            contactRepository.findByContactTypeAndContactValue(ContactType.PARENT_ID.name, tgId.toString())
        if (contacts.isEmpty()) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    "По вашему аккаунту не найден ни один ученик. " +
                            "Выполните команду /link_parent <public_id> для привязки вашего телеграм айди как родителя к ученику"
                )
                .build()
        }
        val publicId = args[0]
        val contact = (contacts.find { it.student.publicId == publicId }
            ?: return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("К вашему аккаунту не привязан ученик с таким public_id")
                .build())

        if (update.message.document != null) {
            telegramFilesService!!.saveDocument(update, update.message.document, contact, getCommandRequester(update))
        } else if (update.message.photo != null && update.message.photo.isNotEmpty()) {
            telegramFilesService!!.savePhoto(update, update.message.photo, contact, getCommandRequester(update))
        } else {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: отправляет скриншот/документ квитанции об оплате с комментарием <public_id> ученика")
                .build()
        }
        return SendMessage.builder()
            .chatId(update.message.chatId)
            //todo: change text
            .text("ЗДЕСЬ ДОЛЖЕН БЫТЬ ТЕКСТ БЛАГОДАРНОСТИ ЗА ОПЛАТУ")
            .build()
    }
}