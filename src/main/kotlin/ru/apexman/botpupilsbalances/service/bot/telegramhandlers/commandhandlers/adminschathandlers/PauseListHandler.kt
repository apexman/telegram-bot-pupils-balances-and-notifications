package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.AlarmEnablingHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.PauseChangingHandler
import java.io.Serializable

/**
 * Выводит список учеников с активным PAUSE
 */
@Component
class PauseListHandler(
    private val studentRepository: StudentRepository,
    private val pauseChangingHandler: PauseChangingHandler,
    private val alarmEnablingHandler: AlarmEnablingHandler,
): TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/pause_list", "Выводит список учеников с активным PAUSE")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val pausedStudents = studentRepository.findAllByIsPauseIsTrue()
        if (pausedStudents.isEmpty()) {
            return listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Нет учеников с активным PAUSE")
                    .build()
            )
        }
        return pausedStudents
            .sortedBy { it.fullUserName }
            .map { st -> Pair(st, "ID: ${st.googleId}\n${st.fullUserName}") }
            .map { (st, text) ->
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text(text)
                    .replyMarkup(buildInlineKeyboardMarkup(st))
                    .build()
            }
    }

    private fun buildInlineKeyboardMarkup(student: Student): InlineKeyboardMarkup {
        val continueButton = InlineKeyboardButton.builder()
            .text("Continue")
            .callbackData("${pauseChangingHandler.getCommandName()} ${student.googleId} false")
            .build()
        val alarmSetButton = InlineKeyboardButton.builder()
            .text("Alarm")
            .callbackData("${alarmEnablingHandler.getCommandName()} ${student.googleId}")
            .build()
        return InlineKeyboardMarkup.builder()
            .keyboard(listOf(listOf(continueButton, alarmSetButton)))
            .build()
    }
}