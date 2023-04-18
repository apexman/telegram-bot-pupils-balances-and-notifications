package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.StudentService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.googleapi.pages.PullPageService


/**
 * Загружаются новые данные со страницы pull
 * TODO: пока все данные со страницы воспринимаются как новые
 */
@Component
class PullHandler(
    private val pullPageService: PullPageService,
    private val studentService: StudentService,
): TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/pull", "Загрузить новых учеников")
    }
    override fun handle(update: Update): BotApiMethodMessage {
        val operationResult = pullPageService.readPullPage()
        if (!operationResult.success) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Возникли ошибки, загрузка прервана:\n\n" + operationResult.errors.joinToString("\n"))
                .build()
        }
        val students = studentService.createNewStudents(operationResult.result)
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Количество загруженных учеников: ${students.size}")
            .build()
    }
}