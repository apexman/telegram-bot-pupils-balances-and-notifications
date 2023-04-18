package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.StudentService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.googleapi.pages.RefreshingFromMainPageService

/**
 * Обновляет состояние базы данных в соответствии с текущим состоянием таблицы Google, вкладки “main”
 */
@Component
class RefreshHandler(
    private val refreshingFromMainPageService: RefreshingFromMainPageService,
    private val studentService: StudentService,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/refresh", "Обновляет состояние базы данных в соответствии с текущим состоянием таблицы Google, вкладки 'main'")
    }

    override fun handle(update: Update, botSession: Session?): PartialBotApiMethod<Message> {
        val operationResult = refreshingFromMainPageService.refreshFromMainPage()
        if (!operationResult.success) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Возникли ошибки, обновление данных прервано:\n\n" + operationResult.errors.joinToString("\n\n"))
                .build()
        }
        val students = studentService.updateStudents(operationResult.result, getCommandRequester(update))
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Количество обновленных учеников: ${students.size}")
            .build()
    }
}