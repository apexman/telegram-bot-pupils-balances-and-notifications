package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.service.notification.TelegramConfiguration

interface TelegramMessageHandler {
    fun getBotCommand(): BotCommand?
    fun handle(update: Update): BotApiMethodMessage
    fun canHandle(update: Update, botUsername: String, telegramConfiguration: TelegramConfiguration): Boolean {
        if (update.hasMessage()
            && update.message.hasEntities()
            && update.message.entities[0].type == "bot_command"
            && update.message.entities[0].offset == 0
            && update.message.entities[0].length > 0
        ) {
            val commandFullName = update.message.text.subSequence(0, update.message.entities[0].length)
            val commandParts = commandFullName.split("@").toMutableList()
            commandParts.add(botUsername)
            if (commandParts[1] == botUsername
                && parseCommand(update) == getBotCommand()?.command
            ) {
                return checkPermissions(update, botUsername, telegramConfiguration)
            }
        }
        return false
    }

    fun parseCommand(update: Update): String? {
        return update.message.text.subSequence(0, update.message.entities[0].length).split("@").firstOrNull()
    }

    fun parseArgs(update: Update): List<String> {
        val strings = update.message.text.split(" ").filter { it.isNotEmpty() }
        return strings.subList(1, strings.size)
    }

    fun checkPermissions(update: Update, botUsername: String, telegramConfiguration: TelegramConfiguration): Boolean {
        return checkPrivateChat(update, botUsername, telegramConfiguration)
                || checkAdminsChat(update, botUsername, telegramConfiguration)
                || checkCollectionReceiptsChat(update, botUsername, telegramConfiguration)
    }

    fun checkPrivateChat(update: Update, botUsername: String, telegramConfiguration: TelegramConfiguration): Boolean {
        return this is PrivateChatHandler && update.message?.chat?.isUserChat == true
    }

    fun checkAdminsChat(update: Update, botUsername: String, telegramConfiguration: TelegramConfiguration): Boolean {
        return this is AdminsChatHandler && update.message?.chatId?.toString() == telegramConfiguration.adminsChatId.trim()
    }

    fun checkCollectionReceiptsChat(
        update: Update,
        botUsername: String,
        telegramConfiguration: TelegramConfiguration,
    ): Boolean {
        return this is AdminsChatHandler && update.message?.chatId?.toString() == telegramConfiguration.collectingReceiptsChatId.trim()
    }

    fun getCommandRequester(update: Update): String {
        val command = getBotCommand()?.command
        val commandName = if (command != null) "Via command='$command' " else ""
        val tgId: Long = update.message.from.id
        val tgUserName: String? = update.message.from.userName
        val userName = if (tgUserName != null) " TgUserName='$tgUserName'" else ""
        return "${commandName}TgId='$tgId'$userName"
    }
}