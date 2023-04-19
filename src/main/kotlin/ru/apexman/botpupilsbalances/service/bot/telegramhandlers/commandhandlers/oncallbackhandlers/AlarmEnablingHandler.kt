package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.entity.userdetails.AlarmDetails
import ru.apexman.botpupilsbalances.repository.AlarmDetailsRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

@Service
class AlarmEnablingHandler(
    private val studentRepository: StudentRepository,
    private val alarmDetailsRepository: AlarmDetailsRepository,
) : TelegramMessageHandler, CallbackQueryHandler {

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/alarm_enabling"
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val answerErrorCallbackQueryList = listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text("Ученик не найден")
                .build()
        )
        val args = parseArgs(update)
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId) ?: return answerErrorCallbackQueryList
        if (student.isAlarm) {
            return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Выставлен ALARM")
                    .build()
            )
        }
        student.isAlarm = true
        studentRepository.save(student)
        val alarmDetails = AlarmDetails(
            student, getCallbackCommandRequester(update), getCallbackCommandRequester(update), null, null
        )
        alarmDetailsRepository.save(alarmDetails)
        return listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text("Выставлен ALARM")
                .build()
        )
    }
}