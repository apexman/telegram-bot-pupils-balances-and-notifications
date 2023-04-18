package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Выводит текстовое сообщение о количестве предоплаченных дней всех привязанных к его ID детей
 */
@Component
class BalanceHandler(
    private val contactRepository: ContactRepository,
) : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/balance", "Количество предоплаченных дней для каждого ребенка")
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
        val text = contacts.map { it.student }.joinToString("\n\n") { "${it.fullUserName}: ${it.balance}" }
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text(text)
            .build()
    }
}