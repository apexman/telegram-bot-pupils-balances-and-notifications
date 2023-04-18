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
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.AlarmDisablingHandler
import java.io.Serializable


/**
 * Выводит список учеников с активным ALARM
 */
@Component
class AlarmListHandler(
    private val studentRepository: StudentRepository,
    private val alarmDisablingHandler: AlarmDisablingHandler,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/alarm_list", "Выводит список учеников с активным ALARM")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val withAlarm = studentRepository.findAllByIsAlarmIsTrueWithActiveAlarmDetails()
        if (withAlarm.isEmpty()) {
            return listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Нет учеников с активным ALARM")
                    .build()
            )
        }
        return withAlarm
            .sortedBy { it.fullUserName }
            .map { st ->
                Pair(st,
                    "${st.fullUserName}\n" +
                            st.alarmDetails
                                .filter { it.disabledAt == null }
                                .sortedByDescending { it.createdAt }
                                .joinToString("\n----------------------------\n") {
                                    (if (it.alarmedBy != null) "ALARMED BY ${it.alarmedBy}\n" else "") + "DETAILS: ${it.details}"
                                }
                )
            }.map { (st, text) ->
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text(text)
                    .replyMarkup(buildInlineKeyboardMarkup(st))
                    .build()
            }
    }

    private fun buildInlineKeyboardMarkup(student: Student): InlineKeyboardMarkup {
        val exceptButton = InlineKeyboardButton.builder()
            .text("ПРОБЛЕМА РЕШЕНА")
            .callbackData("${alarmDisablingHandler.getCommandName()} ${student.googleId}")
            .build()
        return InlineKeyboardMarkup.builder()
            .keyboard(listOf(listOf(exceptButton)))
            .build()
    }
}