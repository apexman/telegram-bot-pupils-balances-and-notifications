package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

/**
 * Удаляет ученика из базы и оповещает, ученику и родителю отправляется прощальная открытка: Картинка + текст
 * Перед удалением запрашивается подтверждение
 */
@Service
class DeleteCallbackHandler(
    private val studentRepository: StudentRepository,
    private val deleteForceCallbackHandler: DeleteForceCallbackHandler,
) : TelegramMessageHandler, CallbackQueryHandler {
    private val logger = LoggerFactory.getLogger(DeleteCallbackHandler::class.java)

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/delete_student"
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId)
            ?: return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Ученик не найден")
                    .build()
            )
        val message = update.callbackQuery.message
        return listOf(
            SendMessage.builder()
                .chatId(message.chatId)
                .text("""
                    Подтвердите удаление ученика
                    ID: ${student.googleId}
                    ${student.fullUserName}
                """.trimIndent())
                .replyMarkup(buildInlineKeyboardMarkup(student))
                .replyToMessageId(message.messageId)
                .build()
        )
    }

    private fun buildInlineKeyboardMarkup(student: Student): ReplyKeyboard {
        val confirmButton = InlineKeyboardButton.builder()
            .text("Подтвердить")
            .callbackData("${deleteForceCallbackHandler.getCommandName()} ${student.googleId} true")
            .build()
        val cancelButton = InlineKeyboardButton.builder()
            .text("Отмена")
            .callbackData("${deleteForceCallbackHandler.getCommandName()} ${student.googleId} false")
            .build()
        return InlineKeyboardMarkup.builder()
            .keyboard(listOf(listOf(confirmButton, cancelButton)))
            .build()
    }
}