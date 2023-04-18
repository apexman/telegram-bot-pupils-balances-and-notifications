package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.service.QRCodeGenerator
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.time.LocalDateTime

/**
 * Для родителя:
 * - Выводит текстовое сообщение о количестве предоплаченных дней всех привязанных к его ID детей
 * Для ученика:
 * - Выводит текстовое сообщение о количестве предоплаченных дней данного ребёнка и сгенерированный QR код, который показывает на входе
 */
@Component
class BalanceHandler(
    private val contactRepository: ContactRepository,
    private val qrCodeGeneratorService: QRCodeGenerator,
) : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/balance", "Количество предоплаченных дней для каждого ребенка")
    }

    override fun handle(update: Update, botSession: Session?): Collection<PartialBotApiMethod<Message>> {
        val tgId = update.message.from.id
        val contactsIfParent =
            contactRepository.findByContactTypeAndContactValue(ContactType.PARENT_ID.name, tgId.toString())
        val contactsIfStudent =
            contactRepository.findByContactTypeAndContactValue(ContactType.CHILD_ID.name, tgId.toString())
        if (contactsIfParent.isEmpty() && contactsIfStudent.isEmpty()) {
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    "Ваш аккаунт не привязан ни к одному ученику.\n " +
                            "Если вы родитель, то выполните команду /link_parent <public_id>\n" +
                            "Если вы ученик, то выполните команду /link_child <public_id>"
                )
                .build())
        }
        if (contactsIfParent.isNotEmpty()) {
            val text = contactsIfParent.map { it.student }.joinToString("\n\n") { "${it.fullUserName}: ${it.balance}" }
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text(text)
                .build())
        }
        if (contactsIfStudent.size > 1) {
            //TODO: change relations
            throw RuntimeException("Найдено несколько учеников с TgId='$tgId'")
        }
        val student = contactsIfStudent.first().student

        val bytes = if (student.balance > 0) {
            qrCodeGeneratorService.generate("${student.fullUserName} ACTIVE ${LocalDateTime.now()}", QRCodeGenerator.GREEN)
        } else {
            qrCodeGeneratorService.generate("${student.fullUserName} FORBIDDEN ${LocalDateTime.now()}", QRCodeGenerator.RED)
        }
        return listOf(SendPhoto.builder()
            .chatId(update.message.chatId)
            .caption("${student.fullUserName}: ${student.balance}")
            .photo(InputFile(bytes.inputStream(), "${student.fullUserName}-${LocalDateTime.now()}.png"))
            .allowSendingWithoutReply(true)
            .build())
    }
}