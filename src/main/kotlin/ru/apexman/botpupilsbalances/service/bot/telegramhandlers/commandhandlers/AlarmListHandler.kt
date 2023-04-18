package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

@Component
class AlarmListHandler: TelegramMessageHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/alarm_list", "Get some alarm_list")
    }

    override fun handle(update: Update): BotApiMethodMessage {
        TODO("implement")
    }
}