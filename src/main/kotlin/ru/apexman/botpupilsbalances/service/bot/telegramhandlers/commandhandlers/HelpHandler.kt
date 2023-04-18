package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Инструкция по взаимодействию с ботом
 */
@Component
class HelpHandler(
    private val adminsChatHandlers: Collection<AdminsChatHandler>,
) : TelegramMessageHandler, AdminsChatHandler {

    private lateinit var commands: List<BotCommand>

    @PostConstruct
    fun postConstruct() {
        commands = adminsChatHandlers
            .filterIsInstance(TelegramMessageHandler::class.java)
            .mapNotNull { it.getBotCommand() }
            .sortedBy { it.command }
    }

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/help", "Инструкция по взаимодействию с ботом")
    }

    override fun handle(update: Update): PartialBotApiMethod<Message> {
        val joinToString = commands.joinToString("\n") { "${it.command} - ${it.description}" }
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Список команд, доступные в чате администратора\n\n$joinToString")
            .build()
    }
}