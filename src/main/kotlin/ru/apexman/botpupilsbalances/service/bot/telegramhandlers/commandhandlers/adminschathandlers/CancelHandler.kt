package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.InvalidSessionException
import org.apache.shiro.session.Session
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

/**
 * Сбрасывает текущий контекст с пользователем в чате
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 200)
class CancelHandler : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/cancel", "Сбрасывает текущий контекст с пользователем в чате")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        try {
            botSession?.removeAttribute(update.message.from.id)
        } catch (_: InvalidSessionException) {
            //omit
        }
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Контекст очищен")
                .build()
        )
    }
}