package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CollectingReceiptsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Бот приветствует пользователя сообщением
 * TODO: изменить сообщение
 */
@Component
class StartHandler : TelegramMessageHandler, AdminsChatHandler, PrivateChatHandler, CollectingReceiptsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/start", "Приветственное сообщение")
    }

    override fun handle(update: Update): BotApiMethodMessage {
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("ЗДЕСЬ ДОЛЖЕН БЫТЬ ТЕКСТ ПРИВЕТСТВЕННОГО СООБЩЕНИЯ И ИНСТРУКЦИЯ ДЛЯ ПОЛЬЗОВАТЕЛЯ О ВЗАИМОДЕЙСТВИИ С БОТОМ")
            .build()
    }
}