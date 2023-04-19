package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

@Service
class PauseChangingHandler(
    private val studentRepository: StudentRepository,
) : TelegramMessageHandler, CallbackQueryHandler {

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/pause_changing"
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
        val newIsPause = args.getOrNull(1)?.toBoolean() == true
        student.isPause = newIsPause
        studentRepository.save(student)
        return listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text(if (!newIsPause) "Снят с паузы" else "Приостановлен")
                .build()
        )
    }
}