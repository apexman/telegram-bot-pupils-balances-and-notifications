package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.apache.shiro.session.InvalidSessionException
import org.apache.shiro.session.Session
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.dto.SessionDataDto
import ru.apexman.botpupilsbalances.service.notification.TelegramProperties
import java.io.Serializable

/**
 * Интерфейс классов, которые могут обработать сообщения от телеги
 * Важен порядок - @Order
 */
interface TelegramMessageHandler {
    fun getBotCommand(): BotCommand?
    fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>>
    fun canHandle(
        update: Update,
        botSession: Session?,
        botUsername: String,
        telegramProperties: TelegramProperties,
    ): Boolean {
        return hasBotCommand(update, botSession, botUsername, telegramProperties)
                || hasCallbackCommand(update, botSession, botUsername, telegramProperties)
                || hasActiveSessionCommand(update, botSession, botUsername, telegramProperties)
    }

    fun hasBotCommand(
        update: Update,
        botSession: Session?,
        botUsername: String,
        telegramProperties: TelegramProperties
    ): Boolean {
        if (update.hasMessage()
            && update.message.hasEntities()
            && update.message.entities[0].type == "bot_command"
            && update.message.entities[0].offset == 0
            && update.message.entities[0].length > 0
        ) {
            val commandFullName = update.message.text.subSequence(0, update.message.entities[0].length)
            val commandParts = commandFullName.split("@").toMutableList()
            commandParts.add(botUsername)
            if (commandParts.getOrNull(1) == botUsername
                && getBotCommand()?.command != null
                && parseCommand(update) == getBotCommand()?.command
            ) {
                return checkPermissions(update, botUsername, telegramProperties)
            }
        }
        return false
    }

    fun hasCallbackCommand(
        update: Update,
        botSession: Session?,
        botUsername: String,
        telegramProperties: TelegramProperties
    ): Boolean {
        return this is CallbackQueryHandler
                && update.hasCallbackQuery()
                && checkCallbackQuery(update, botUsername, telegramProperties)
                && parseCallbackCommand(update) == this.getCommandName()
    }

    fun hasActiveSessionCommand(
        update: Update,
        botSession: Session?,
        botUsername: String,
        telegramProperties: TelegramProperties
    ): Boolean {
        return try {
            val tgId = update.message?.from?.id ?: return false
            val userSessionData = botSession?.getAttribute(tgId) as SessionDataDto?
                ?: SessionDataDto()
            userSessionData.currentCommandName != null
                    && userSessionData.currentCommandName == getBotCommand()?.command
        } catch (_: InvalidSessionException) {
            false
        }
    }

    fun parseCommand(update: Update): String? {
        return update.message.text.subSequence(0, update.message.entities[0].length).split("@").firstOrNull()
    }

    fun parseCallbackCommand(update: Update): String? {
        return update.callbackQuery?.data?.split(" ")?.firstOrNull()
    }

    fun parseArgs(update: Update): List<String> {
        val strings = update.message?.text?.split(" ")?.filter { it.isNotEmpty() }
        val callbackList = update.callbackQuery?.data?.split(" ")?.filter { it.isNotEmpty() }
        return strings?.subList(1, strings.size)
            ?: update.message?.caption?.split(" ")?.filter { it.isNotEmpty() }
            ?: callbackList?.subList(1, callbackList.size)
            ?: listOf()
    }

    fun checkPermissions(update: Update, botUsername: String, telegramProperties: TelegramProperties): Boolean {
        return checkPrivateChat(update, botUsername, telegramProperties)
                || checkAdminsChat(update, botUsername, telegramProperties)
                || checkCollectionReceiptsChat(update, botUsername, telegramProperties)
                || checkCallbackQuery(update, botUsername, telegramProperties)
    }

    fun checkPrivateChat(update: Update, botUsername: String, telegramProperties: TelegramProperties): Boolean {
        return this is PrivateChatHandler && update.message?.chat?.isUserChat == true
    }

    fun checkAdminsChat(update: Update, botUsername: String, telegramProperties: TelegramProperties): Boolean {
        return this is AdminsChatHandler && update.message?.chatId?.toString() == telegramProperties.adminsChatId.trim()
    }

    fun checkCollectionReceiptsChat(
        update: Update,
        botUsername: String,
        telegramProperties: TelegramProperties,
    ): Boolean {
        return this is AdminsChatHandler && update.message?.chatId?.toString() == telegramProperties.collectingReceiptsChatId.trim()
    }

    fun checkCallbackQuery(update: Update, botUsername: String, telegramProperties: TelegramProperties): Boolean {
        return this is CallbackQueryHandler && update.hasCallbackQuery()
    }

    fun getBotCommandRequester(update: Update): String {
        val command = getBotCommand()?.command
        val commandName = if (command != null) "Via command='$command' " else ""
        val tgId: Long = update.message.from.id
        val tgUserName: String? = update.message.from.userName
        val userName = if (tgUserName != null) " TgUserName='$tgUserName'" else ""
        return "${commandName}TgId='$tgId'$userName"
    }

}