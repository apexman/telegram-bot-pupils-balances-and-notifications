package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import jakarta.annotation.PostConstruct
import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramProperties
import java.io.Serializable

/**
 * Удаляет ученика из базы БЕЗ ПОДОТВЕРЖДЕНИЯ и оповещает, ученику и родителю отправляется прощальная открытка: Картинка + текст
 * Перед удалением запрашивается подтверждение
 */
@Service
class DeleteForceCallbackHandler(
    private val studentRepository: StudentRepository,
    private val contactRepository: ContactRepository,
    private val telegramProperties: TelegramProperties,
) : TelegramMessageHandler, CallbackQueryHandler {
    private lateinit var goodbyePng: ByteArray
    private val logger = LoggerFactory.getLogger(DeleteForceCallbackHandler::class.java)

    @PostConstruct
    fun postConstruct() {
        goodbyePng = DeleteForceCallbackHandler::class.java.getResourceAsStream("/goodbye.png")!!.readAllBytes()
    }

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/delete_student_force"
    }

    @Transactional
    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val message = update.callbackQuery.message
        val answerErrorCallbackQueryList = listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text("Ученик не найден")
                .build()
        )
        val args = parseArgs(update)
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId) ?: return answerErrorCallbackQueryList
        val reallyDelete = args.getOrNull(1)?.toBoolean() == true
        if (reallyDelete) {
            val contacts = contactRepository.findAllByStudent(student)
            val childTgChatIds =
                contacts.filter { it.contactType == ContactType.CHILD_CHAT_ID.name }.map { it.contactValue }
            val parentTgChatIds =
                contacts.filter { it.contactType == ContactType.PARENT_CHAT_ID.name }.map { it.contactValue }
            val fullUserName = student.fullUserName
            studentRepository.delete(student)
            logger.info("Deleted student $fullUserName by ${getCallbackCommandRequester(update)}")
            val callbackAnswer = listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Ученик удален")
                    .build()
            )
            val studentGoodbyeMessage = buildStudentGoodbyeMessage(childTgChatIds, message)
            val parentsGoodbyeMessages = buildParentsGoodbyeMessages(parentTgChatIds, message)
            return callbackAnswer + studentGoodbyeMessage + parentsGoodbyeMessages
        }
        return listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text("Отмено")
                .build()
        )
    }

    //TODO: change last text message to parent and child
    private fun buildStudentGoodbyeMessage(
        childTgChatIds: List<String>,
        message: Message,
    ): List<PartialBotApiMethod<out Serializable>> {
        val studentErrorMessage = "Уведомление не отправлено ученику, так как не было привязки аккаунта"
        if (childTgChatIds.isEmpty()) {
            return listOf(
                SendMessage.builder()
                    .chatId(telegramProperties.adminsChatId)
                    .replyToMessageId(message.messageId)
                    .text(studentErrorMessage)
                    .build()
            )
        }
        return childTgChatIds.map {
            SendPhoto.builder()
                .chatId(it)
                .caption("Пока-пока")
                .photo(InputFile(goodbyePng.inputStream(), "goodbye.png"))
                .build()
        }
    }

    //TODO: change last text message to parent and child
    private fun buildParentsGoodbyeMessages(
        parentTgChatIds: List<String>,
        message: Message,
    ): List<PartialBotApiMethod<out Serializable>> {
        val parentErrorMessage = "Уведомление не отправлено родителю, так как не было привязки аккаунта"
        if (parentTgChatIds.isEmpty()) {
            return listOf(
                SendMessage.builder()
                    .chatId(telegramProperties.adminsChatId)
                    .replyToMessageId(message.messageId)
                    .text(parentErrorMessage)
                    .build()
            )
        }
        return parentTgChatIds.map {
            SendPhoto.builder()
                .chatId(it)
                .caption("Пока-пока")
                .photo(InputFile(goodbyePng.inputStream(), "goodbye.png"))
                .build()
        }
    }
}