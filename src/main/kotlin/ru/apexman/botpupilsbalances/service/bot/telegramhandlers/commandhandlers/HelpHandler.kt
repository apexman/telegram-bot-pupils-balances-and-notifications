package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

@Component
class HelpHandler: TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/help", "Get some help")
    }

    override fun handle(update: Update): PartialBotApiMethod<Message> {
        TODO("implement")
    }
}