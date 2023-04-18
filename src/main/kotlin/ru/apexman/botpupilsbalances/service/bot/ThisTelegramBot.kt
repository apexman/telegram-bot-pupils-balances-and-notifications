package ru.apexman.botpupilsbalances.service.bot

import jakarta.annotation.PostConstruct
import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.session.DefaultChatIdConverter
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PendingBalancePaymentHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramConfiguration
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService
import java.util.*

@Component
class ThisTelegramBot(
    private val telegramConfiguration: TelegramConfiguration,
    private val handlers: Collection<TelegramMessageHandler>,
    private val telegramNotificationService: TelegramNotificationService,
) : TelegramLongPollingSessionBot(DefaultChatIdConverter(), withBotOptions(), telegramConfiguration.token) {
    private val logger = LoggerFactory.getLogger(ThisTelegramBot::class.java)
    private var botUser: User? = null

    @PostConstruct
    fun postConstruct() {
        botUser = me
        val botCommands = handlers.mapNotNull { it.getBotCommand() }
        setBot()
        validateBotCommands(botCommands)
        execute(SetMyCommands(botCommands, null, null))
    }

    override fun getBotUsername(): String {
        return botUser?.userName!!
    }

    override fun onUpdateReceived(update: Update?, botSession: Optional<Session>?) {
        onUpdateReceived(update, botSession?.orElse(null))
    }


    fun onUpdateReceived(update: Update?, botSession: Session?) {
        try {
            logger.debug("Got new update: $update")
            if (update == null) {
                logger.debug("Got null update, skip")
                return
            }
            for (handler in handlers) {
                if (handler.canHandle(update, botUsername, telegramConfiguration)) {
                    when (val apiMethod = handler.handle(update)) {
                        is BotApiMethodMessage -> execute(apiMethod)
                        is SendPhoto -> execute(apiMethod)
                        is SendDocument -> execute(apiMethod)
                        else -> throw RuntimeException("Unhandled response type: ${apiMethod.javaClass.name}")
                    }
                    logger.debug("Handled by ${handler.javaClass.name}")
                    return
                }
            }
            //TODO: can talk in not private chat?
            if (update.message?.chat?.isUserChat == true) {
                val message = SendMessage.builder()
                    .chatId(update.message.chatId)
                    //todo: fix message
                    .text("Простите, я вас не понял")
                    .build()
                execute(message)
            }
        } catch (e: Throwable) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e, update)
            )
            tryAnswer(update)
        }
    }

    private fun tryAnswer(update: Update?) {
        try {
            execute(
                SendMessage.builder()
                    .chatId(update?.message?.chatId!!)
                    //todo: fix message
                    .text("Простите, при обработке сообщения произошли ошибки, администраторы сообщены о проблеме")
                    .allowSendingWithoutReply(true)
                    .build()
            )
        } catch (e: Exception) {
            logger.error("Cannot send notification about error to user", e)
        }
    }

    private fun setBot() {
        for (handler in handlers) {
            if (handler is PendingBalancePaymentHandler) {
                handler.setBot(this)
            }
        }
    }

    private fun validateBotCommands(botCommands: List<BotCommand>) {
        val commands = mutableListOf<String>()
        for (botCommand in botCommands) {
            if (commands.contains(botCommand.command)) {
                throw RuntimeException("Command duplicates: '${botCommand.command}'")
            }
            commands.add(botCommand.command)
        }
    }

    companion object {
        private fun withBotOptions() =
            DefaultBotOptions()
                .also { it.allowedUpdates = listOf("message", "edited_channel_post", "callback_query", "chat_member") }
    }
}