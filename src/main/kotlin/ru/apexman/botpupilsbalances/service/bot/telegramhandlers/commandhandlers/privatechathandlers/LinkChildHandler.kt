package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.privatechathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.ContactService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

/**
 * Привязывает телеграм айди ученика
 */
@Component
class LinkChildHandler(
    private val contactRepository: ContactRepository,
    private val studentRepository: StudentRepository,
    private val contactService: ContactService,
) : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/link_child", "Привязывает телеграм айди ученика")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: /link_child <public_id>")
                .build())
        }
        val publicId = args[0]
        val student = (studentRepository.findByPublicId(publicId)
            ?: return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Ученик с таким public_id не найден")
                .build()))
        val childTgId: Long = update.message.from.id
        val childTgUserName: String? = update.message.from.userName
        val savingContacts = mutableListOf<Contact>()
        savingContacts.add(contactService.buildContact(student, ContactType.CHILD_ID, childTgId.toString()))
        savingContacts.add(contactService.buildContact(student, ContactType.CHILD_CHAT_ID, update.message.chatId.toString()))
        if (childTgUserName != null) {
            savingContacts.add(contactService.buildContact(student, ContactType.CHILD_TELEGRAM_USERNAME, childTgUserName))
        }
        contactRepository.saveAll(savingContacts)
        return listOf(SendMessage.builder()
            .chatId(update.message.chatId)
            .text("${student.fullUserName}, ВЫ ЗАПИСАНЫ НАШИМ УЧЕНИКОМ")
            .build())
    }

}