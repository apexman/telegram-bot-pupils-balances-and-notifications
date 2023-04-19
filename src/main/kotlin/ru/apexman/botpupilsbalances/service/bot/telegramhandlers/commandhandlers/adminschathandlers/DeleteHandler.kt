package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.DeleteCallbackHandler
import java.io.Serializable

/**
 * Удаляет ученика из базы и оповещает, ученику и родителю отправляется прощальная открытка: Картинка + текст
 * Перед удалением запрашивается подтверждение
 */
@Component
class DeleteHandler(
    private val deleteCallbackHandler: DeleteCallbackHandler,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/delete", "Удаляет ученика из базы")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        return deleteCallbackHandler.handle(update, botSession)
    }
}