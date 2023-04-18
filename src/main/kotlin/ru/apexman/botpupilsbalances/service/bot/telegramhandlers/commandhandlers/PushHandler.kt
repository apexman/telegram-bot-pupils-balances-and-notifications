package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.googleapi.pages.PushingToMainPageService

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

    override fun handle(update: Update): PartialBotApiMethod<Message> {
        val operationResult = pushingToMainPageService.pushToMainPage()
        if (!operationResult.success) {
            return SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Возникли ошибки, выгрузка прервана:\n\n" + operationResult.errors.joinToString("\n\n"))
                .build()
        }
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Количество выгруженных учеников: ${operationResult.result}")
            .build()
    }
}