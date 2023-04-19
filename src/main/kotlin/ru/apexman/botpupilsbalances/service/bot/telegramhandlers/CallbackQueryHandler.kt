package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Хендлер доступн из приватного чата
 * Главное отличие - сообщение находится в update.callbackQuery.message
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE + 100)
interface CallbackQueryHandler {

    fun getCommandName(): String

    fun getCallbackCommandRequester(update: Update): String {
        val tgId: Long = update.callbackQuery.message.from.id
        val tgUserName: String? = update.callbackQuery.message.from.userName
        val userName = if (tgUserName != null) " TgUserName='$tgUserName'" else ""
        return "TgId='$tgId'$userName"
    }

}