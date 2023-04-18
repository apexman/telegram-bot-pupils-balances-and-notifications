package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

@Component
class DeleteHandler : TelegramMessageHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/delete", "Get some delete")
    }

    override fun handle(update: Update): BotApiMethodMessage {
        TODO("implement")
    }
}