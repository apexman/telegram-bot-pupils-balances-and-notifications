package ru.apexman.botpupilsbalances.service.bot.telegramhandlers

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Хендлер доступн из приватного чата
 * Отличия от других типв хендлеров:
 * - сообщение находится в update.callbackQuery.message
 * - отправить оригинальног сообщения в update.callbackQuery.from
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE + 100)
interface CallbackQueryHandler {

    fun getCommandName(): String

    fun getCallbackCommandRequester(update: Update): String {
        val tgId: Long = update.callbackQuery.from.id
        val tgUserName: String? = update.callbackQuery.from.userName
        val userName = if (tgUserName != null) " TgUserName='$tgUserName'" else ""
        return "TgId='$tgId'$userName"
    }

}