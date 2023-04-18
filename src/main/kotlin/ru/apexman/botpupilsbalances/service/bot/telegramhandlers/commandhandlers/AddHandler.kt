package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

@Component
class AddHandler : TelegramMessageHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/add", "Get some add")
    }

    override fun handle(update: Update): PartialBotApiMethod<Message> {
        TODO("implement")
    }
}