package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.repository.DocumentRepository
import ru.apexman.botpupilsbalances.service.notification.TelegramConfiguration

/**
 * Хендлер для загрузки файлов в систему с последующим оповещением чата менеджеров для обработки запроса
 */
@Component
class PendingBalancePaymentHandler(
    private val contactRepository: ContactRepository,
    private val documentRepository: DocumentRepository,
) : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun canHandle(update: Update, botUsername: String, telegramConfiguration: TelegramConfiguration): Boolean {
        if (update.hasMessage()) {
            return checkPermissions(update, botUsername, telegramConfiguration)
                    && (update.message.photo != null
                    || update.message.document != null)
        }
        return false
    }

    override fun handle(update: Update): PartialBotApiMethod<Message> {
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
        val student = (contacts.find { it.student.publicId == publicId }?.student
            ?: return SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    "Ваш аккаунт не привязан ни к одному ученику.\n " +
                            "Выполните команду /link_parent <public_id>"
                )
                .build())

        ByteArray(1).contentHashCode()

        if (update.message.document != null) {
            val uploadedFileId = update.message.document.fileId
            val uploadedFile = GetFile()
            uploadedFile.setFileId(uploadedFileId)
//            val uploadedFilePath: String = getFile(uploadedFile).getFilePath()
//            return uploadedFile
        }

        return SendMessage.builder()
            .chatId(update.message.chatId)
            //todo: change text
            .text("ЗДЕСЬ ДОЛЖЕН БЫТЬ ТЕКСТ БЛАГОДАРНОСТИ ЗА ОПЛАТУ")
            .build()
    }
}