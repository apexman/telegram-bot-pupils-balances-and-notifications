package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Выводит список учеников с активным ALARM
 */
@Component
class AlarmListHandler: TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/alarm_list", "Выводит список учеников с активным ALARM")
    }

    override fun handle(update: Update, botSession: Session?): Collection<PartialBotApiMethod<Message>> {
        TODO("implement")
    }
}