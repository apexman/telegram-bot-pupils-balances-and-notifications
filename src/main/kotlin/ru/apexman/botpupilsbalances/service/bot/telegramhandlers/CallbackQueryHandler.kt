package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Хендлер доступн из приватного чата
 */
interface CallbackQueryHandler {

    fun getCommandName(): String

    fun getCallbackCommandRequester(update: Update): String {
        val tgId: Long = update.callbackQuery.message.from.id
        val tgUserName: String? = update.callbackQuery.message.from.userName
        val userName = if (tgUserName != null) " TgUserName='$tgUserName'" else ""
        return "TgId='$tgId'$userName"
    }

}