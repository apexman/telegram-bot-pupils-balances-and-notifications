package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.googleapi.pages.PushingToMainPageService
import java.io.Serializable

/**
 * Выгружаются ученики на страницу 'main'
 */
@Component
class PushHandler(
    private val pushingToMainPageService: PushingToMainPageService,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/push", "Отправка актуального состояния бд на страницу 'main'")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val operationResult = pushingToMainPageService.pushToMainPage()
        if (!operationResult.success) {
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Возникли ошибки, выгрузка прервана:\n\n" + operationResult.errors.joinToString("\n\n"))
                .build())
        }
        return listOf(SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Количество выгруженных учеников: ${operationResult.result}")
            .build())
    }
}