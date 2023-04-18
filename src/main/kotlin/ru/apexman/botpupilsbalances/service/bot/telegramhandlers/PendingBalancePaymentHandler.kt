package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
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

    override fun handle(update: Update): BotApiMethodMessage {
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
        if (contacts.size > 1) {
            TODO("implement when parent has several children")
        }
        TODO()
    }
}