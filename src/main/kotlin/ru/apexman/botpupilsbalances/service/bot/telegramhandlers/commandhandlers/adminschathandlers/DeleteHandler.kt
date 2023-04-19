package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.DeleteForceCallbackHandler
import java.io.Serializable

/**
 * Удаляет ученика из базы и оповещает, ученику и родителю отправляется прощальная открытка: Картинка + текст
 * Перед удалением запрашивается подтверждение
 */
@Component
class DeleteHandler(
    private val studentRepository: StudentRepository,
    private val deleteForceCallbackHandler: DeleteForceCallbackHandler,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/delete", "Удаляет ученика из базы")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Использование: /delete <id>")
                    .build()
            )
        }
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId)
            ?: return listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Ученик не найден")
                    .build()
            )
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    """
                    Подтвердите удаление ученика
                    ID: ${student.googleId}
                    ${student.fullUserName}
                """.trimIndent()
                )
                .replyMarkup(buildInlineKeyboardMarkup(student))
                .replyToMessageId(update.message.messageId)
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