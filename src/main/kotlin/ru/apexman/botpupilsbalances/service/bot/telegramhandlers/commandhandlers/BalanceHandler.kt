package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Для родителя:
 * - Выводит текстовое сообщение о количестве предоплаченных дней всех привязанных к его ID детей
 * Для ученика:
 * - Выводит текстовое сообщение о количестве предоплаченных дней данного ребёнка и сгенерированный QR код, который показывает на входе
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
        val contactsIfParent =
            contactRepository.findByContactTypeAndContactValue(ContactType.PARENT_ID.name, tgId.toString())
        val contactsIfStudent =
            contactRepository.findByContactTypeAndContactValue(ContactType.CHILD_ID.name, tgId.toString())
        if (contactsIfParent.isEmpty() && contactsIfStudent.isEmpty()) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    "Ваш аккаунт не привязан ни к одному ученику.\n " +
                            "Если вы родитель, то выполните команду /link_parent <public_id>\n" +
                            "Если вы ученик, то выполните команду /link_child <public_id>"
                )
                .build()
        }
        if (contactsIfParent.isNotEmpty()) {
            val text = contactsIfParent.map { it.student }.joinToString("\n\n") { "${it.fullUserName}: ${it.balance}" }
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text(text)
                .build()
        }
        val text = contactsIfStudent.map { it.student }.joinToString("\n\n") { "${it.fullUserName}: ${it.balance}" }
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text(text)
            .build()
    }
}