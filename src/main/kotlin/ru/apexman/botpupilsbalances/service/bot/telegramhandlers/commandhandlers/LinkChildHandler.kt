package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.ContactService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

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

    override fun handle(update: Update): BotApiMethodMessage {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: /link_child <public_id>")
                .build()
        }
        val publicId = args[0]
        val student = (studentRepository.findByPublicId(publicId)
            ?: return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Ученик с таким public_id не найден")
                .build())
        val childTgId: Long = update.message.from.id
        val childTgUserName: String? = update.message.from.userName
        val savingContacts = mutableListOf<Contact>()
        savingContacts.add(contactService.buildContact(student, ContactType.CHILD_ID, childTgId.toString()))
        savingContacts.add(contactService.buildContact(student, ContactType.CHILD_CHAT_ID, update.message.chatId.toString()))
        if (childTgUserName != null) {
            savingContacts.add(contactService.buildContact(student, ContactType.CHILD_TELEGRAM_USERNAME, childTgUserName))
        }
        contactRepository.saveAll(savingContacts)
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("${student.fullUserName}, ВЫ ЗАПИСАНЫ НАШИМ УЧЕНИКОМ")
            .build()
    }

}