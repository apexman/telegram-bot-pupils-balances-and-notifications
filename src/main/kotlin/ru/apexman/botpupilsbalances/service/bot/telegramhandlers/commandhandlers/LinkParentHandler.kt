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
import ru.apexman.botpupilsbalances.repository.ParentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Привязывает телеграм айди пользователя как родителя к ученику
 */
@Component
class LinkParentHandler(
    private val parentRepository: ParentRepository,
    private val contactRepository: ContactRepository,
) : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/link_parent", "Привязывает телеграм айди пользователя как родителя к указанному ученику")
    }

    override fun handle(update: Update): BotApiMethodMessage {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: /link_parent <public_id>")
                .build()
        }
        val publicId = args[0]
        val parent = (parentRepository.findByPublicId(publicId)
            ?: return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Родитель с таким public_id не найден")
                .build())
        val parentTgId: Long = update.message.from.id
        val parentTgUserName: String? = update.message.from.userName
        val savingContacts = mutableListOf<Contact>()
        for (student in parent.students) {
            val contacts = contactRepository.findAllByStudent(student)
            savingContacts.add(buildTgIdContact(contacts, student, parentTgId))
            savingContacts.add(buildTgChatIdContact(contacts, student, update))
            if (parentTgUserName != null) {
                savingContacts.add(buildTgUserNameContact(contacts, student, parentTgUserName))
            }
        }
        contactRepository.saveAll(savingContacts)
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("ВЫ ЗАПИСАНЫ РОДИТЕЛЕМ: " + parent.students.map { it.fullUserName }.joinToString(", "))
            .build()
    }

    private fun buildTgIdContact(
        contacts: List<Contact>,
        student: Student,
        parentTgId: Long,
    ): Contact {
        val parentTgIdContact = (contacts.find { it.contactType == ContactType.PARENT_ID.name }
            ?: Contact(student, ContactType.PARENT_ID.name, parentTgId.toString()))
        parentTgIdContact.contactValue = parentTgId.toString()
        return parentTgIdContact
    }

    private fun buildTgChatIdContact(
        contacts: List<Contact>,
        student: Student,
        update: Update,
    ): Contact {
        val parentTgChatIdContact = contacts.find { it.contactType == ContactType.PARENT_CHAT_ID.name }
            ?: Contact(student, ContactType.PARENT_CHAT_ID.name, update.message.chatId.toString())
        parentTgChatIdContact.contactValue = update.message.chatId.toString()
        return parentTgChatIdContact
    }

    private fun buildTgUserNameContact(
        contacts: List<Contact>,
        student: Student,
        parentTgUserName: String,
    ): Contact {
        val parentTgUserNameContact =
            contacts.find { it.contactType == ContactType.PARENT_TELEGRAM_USERNAME.name }
                ?: Contact(student, ContactType.PARENT_TELEGRAM_USERNAME.name, parentTgUserName)
        parentTgUserNameContact.contactValue = parentTgUserName
        return parentTgUserNameContact
    }
}