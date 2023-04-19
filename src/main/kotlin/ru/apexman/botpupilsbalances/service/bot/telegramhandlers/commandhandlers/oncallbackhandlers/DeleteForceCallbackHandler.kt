package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

@Service
class DeleteForceCallbackHandler(
    private val studentRepository: StudentRepository,
) : TelegramMessageHandler, CallbackQueryHandler {
    private val logger = LoggerFactory.getLogger(DeleteForceCallbackHandler::class.java)

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/delete_student_force"
    }

    @Transactional
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
        val reallyDelete = args.getOrNull(1)?.toBoolean() == true
        if (reallyDelete) {
            val fullUserName = student.fullUserName
            studentRepository.delete(student)
            logger.info("Deleted student $fullUserName by ${getCallbackCommandRequester(update)}")
            return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Ученик удален")
                    .build()
            )
        }
        return listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text("Отмено")
                .build()
        )
    }
}