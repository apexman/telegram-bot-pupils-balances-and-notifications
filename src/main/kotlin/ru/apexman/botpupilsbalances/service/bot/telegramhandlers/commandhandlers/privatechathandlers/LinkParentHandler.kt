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
 * Привязывает телеграм айди пользователя как родителя к ученику
 */
@Component
class LinkParentHandler(
    private val studentRepository: StudentRepository,
    private val contactRepository: ContactRepository,
    private val contactService: ContactService,
) : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/link_parent", "Привязывает телеграм айди пользователя как родителя к указанному ученику")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: /link_parent <public_id>")
                .build())
        }
        val publicId = args[0]
        val student = (studentRepository.findByPublicId(publicId)
            ?: return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Ученик с таким public_id не найден")
                .build()))
        val parentTgId: Long = update.message.from.id
        val parentTgUserName: String? = update.message.from.userName
        val parentTgLastName: String? = update.message.from.lastName
        val parentTgFirstName: String = update.message.from.firstName
        val parentTgFullName: String = (if (parentTgLastName != null) "$parentTgLastName " else "") + parentTgFirstName
        val savingContacts = mutableListOf<Contact>()
        savingContacts.add(contactService.buildContact(student, ContactType.PARENT_ID, parentTgId.toString()))
        savingContacts.add(contactService.buildContact(student, ContactType.PARENT_CHAT_ID, update.message.chatId.toString()))
        if (parentTgUserName != null) {
            savingContacts.add(contactService.buildContact(student, ContactType.PARENT_TELEGRAM_USERNAME, parentTgUserName))
        }
        savingContacts.add(contactService.buildContact(student, ContactType.PARENT_TELEGRAM_FULL_NAME, parentTgFullName))
        contactRepository.saveAll(savingContacts)
        return listOf(SendMessage.builder()
            .chatId(update.message.chatId)
            .text("ВЫ ЗАПИСАНЫ РОДИТЕЛЕМ УЧЕНИКА(ЦЫ) ${student.fullUserName}")
            .build())
    }

}