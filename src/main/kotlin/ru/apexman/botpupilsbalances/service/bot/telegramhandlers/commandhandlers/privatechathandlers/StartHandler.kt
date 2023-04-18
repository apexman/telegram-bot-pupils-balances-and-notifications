package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.privatechathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PrivateChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

/**
 * Бот приветствует пользователя сообщением
 * TODO: изменить сообщение
 */
@Component
class StartHandler : TelegramMessageHandler, PrivateChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/start", "Приветственное сообщение")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        return listOf(SendMessage.builder()
            .chatId(update.message.chatId)
            .text("ЗДЕСЬ ДОЛЖЕН БЫТЬ ТЕКСТ ПРИВЕТСТВЕННОГО СООБЩЕНИЯ И ИНСТРУКЦИЯ ДЛЯ ПОЛЬЗОВАТЕЛЯ О ВЗАИМОДЕЙСТВИИ С БОТОМ")
            .build())
    }
}