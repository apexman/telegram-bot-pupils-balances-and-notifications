package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.shiro.session.InvalidSessionException
import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.dto.AddHandlerDataDto
import ru.apexman.botpupilsbalances.dto.SessionDataDto
import ru.apexman.botpupilsbalances.service.StudentService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService
import java.io.Serializable

@Service
class AddingUserHandler(
    private val studentService: StudentService,
    private val mapper: ObjectMapper,
    private val telegramNotificationService: TelegramNotificationService,
) : TelegramMessageHandler, CallbackQueryHandler {
    private val logger = LoggerFactory.getLogger(AddingUserHandler::class.java)

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/adding_user"
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val message = update.callbackQuery.message
        val from = update.callbackQuery.from
        val userErrorMessage = "Контекст очищен, ученик не создан"
        val answerErrorCallbackQueries = listOf(
            AnswerCallbackQuery.builder()
                .callbackQueryId(update.callbackQuery.id)
                .text(userErrorMessage)
                .build()
        )
        try {
            val sessionDataDto = botSession?.getAttribute(from.id) as SessionDataDto?
            if (sessionDataDto?.data == null) {
                logger.debug("User session context is empty")
                return answerErrorCallbackQueries
            }
            val args = parseArgs(update)
            if (args.getOrNull(0)?.toBoolean() != true) {
                return answerErrorCallbackQueries
            }
            val dto = mapper.readValue(sessionDataDto.data, AddHandlerDataDto::class.java)
            if (!dto.canMap()) {
                val error = "User session context is empty, must be values though"
                logger.error(error)
                telegramNotificationService.sendMonitoring(
                    error,
                    TelegramNotificationService.buildTelegramDocumentDto(RuntimeException(error), update, botSession)
                )
                return answerErrorCallbackQueries
            }
            val student = studentService.createStudent(dto)
            return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Ученик создан")
                    .build(),
                SendMessage.builder()
                    .chatId(update.callbackQuery.message.chatId)
                    .text("""
                        ID: ${student.googleId}
                    """.trimIndent())
                    .build()
            )
        } catch (e: JacksonException) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e, update, botSession)
            )
            return answerErrorCallbackQueries
        } catch (e: InvalidSessionException) {
            logger.warn("Chat session invalidated", e)
            return answerErrorCallbackQueries
        } finally {
            clearUserSession(from, botSession)
        }
    }

    private fun clearUserSession(from: User, botSession: Session?) {
        try {
            botSession?.removeAttribute(from.id)
        } catch (_: InvalidSessionException) {
            //omit
        }
    }
}