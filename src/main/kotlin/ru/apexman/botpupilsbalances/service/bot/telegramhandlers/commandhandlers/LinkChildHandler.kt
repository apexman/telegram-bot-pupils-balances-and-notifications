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
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Привязывает телеграм айди ученика
 */
@Component
class LinkChildHandler(
    private val contactRepository: ContactRepository,
    private val studentRepository: StudentRepository,
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
        val contacts = contactRepository.findAllByStudent(student)
        savingContacts.add(buildTgIdContact(contacts, student, childTgId))
        savingContacts.add(buildTgChatIdContact(contacts, student, update))
        if (childTgUserName != null) {
            savingContacts.add(buildTgUserNameContact(contacts, student, childTgUserName))
        }
        contactRepository.saveAll(savingContacts)
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("${student.fullUserName}, ВЫ ЗАПИСАНЫ НАШИМ УЧЕНИКОМ")
            .build()
    }

    private fun buildTgIdContact(
        contacts: List<Contact>,
        student: Student,
        childTgId: Long,
    ): Contact {
        val childTgIdContact = (contacts.find { it.contactType == ContactType.CHILD_ID.name }
            ?: Contact(student, ContactType.CHILD_ID.name, childTgId.toString()))
        childTgIdContact.contactValue = childTgId.toString()
        return childTgIdContact
    }

    private fun buildTgChatIdContact(contacts: List<Contact>, student: Student, update: Update): Contact {
        val parentTgChatIdContact = contacts.find { it.contactType == ContactType.CHILD_CHAT_ID.name }
            ?: Contact(student, ContactType.CHILD_CHAT_ID.name, update.message.chatId.toString())
        parentTgChatIdContact.contactValue = update.message.chatId.toString()
        return parentTgChatIdContact
    }

    private fun buildTgUserNameContact(
        contacts: List<Contact>,
        student: Student,
        childTgUserName: String,
    ): Contact {
        val childTgUserNameContact =
            contacts.find { it.contactType == ContactType.CHILD_TELEGRAM_USERNAME.name }
                ?: Contact(student, ContactType.CHILD_TELEGRAM_USERNAME.name, childTgUserName)
        childTgUserNameContact.contactValue = childTgUserName
        return childTgUserNameContact
    }
}