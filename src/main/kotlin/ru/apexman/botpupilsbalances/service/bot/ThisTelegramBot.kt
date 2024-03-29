package ru.apexman.botpupilsbalances.service.bot

import jakarta.annotation.PostConstruct
import org.apache.shiro.session.InvalidSessionException
import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.session.DefaultChatIdConverter
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.PendingBalancePaymentHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramProperties
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService
import java.util.*

@Component
class ThisTelegramBot(
    private val telegramProperties: TelegramProperties,
    private val handlers: List<TelegramMessageHandler>,
    private val telegramNotificationService: TelegramNotificationService,
) : TelegramLongPollingSessionBot(DefaultChatIdConverter(), withBotOptions(), telegramProperties.token) {
    private val logger = LoggerFactory.getLogger(ThisTelegramBot::class.java)
    private var botUser: User? = null

    @PostConstruct
    fun postConstruct() {
        botUser = me
        validateBotCommands()
        validateOnCallbackQueryCommands()
        setBot()
        val botCommands = handlers.mapNotNull { it.getBotCommand() }
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
                if (handler.canHandle(update, botSession, botUsername, telegramProperties)) {
                    //todo: implement async
                    val apiMethods = handler.handle(update, botSession)
                    for (apiMethod in apiMethods) {
                        when (apiMethod) {
                            is BotApiMethodMessage -> execute(apiMethod)
                            is SendPhoto -> execute(apiMethod)
                            is SendDocument -> execute(apiMethod)
                            is AnswerCallbackQuery -> execute(apiMethod)
                            else -> throw RuntimeException("Unhandled response type: ${apiMethod.javaClass.name}")
                        }
                    }
                    logger.debug("Handled by ${handler.javaClass.name}")
                    return
                }
            }
            //TODO: can talk in non private chat?
            if (update.message?.chat?.isUserChat == true) {
                val message = SendMessage.builder()
                    .chatId(update.message.chatId)
                    //todo: fix message
                    .text("Простите, я вас не понял")
                    .build()
                execute(message)
            }
        } catch (e: InvalidSessionException) {
            try {
                logger.error(e.toString(), e)
                execute(
                    SendMessage.builder()
                        .chatId(update?.message?.chatId ?: update?.callbackQuery?.message?.chatId!!)
                        //todo: fix message
                        .text("Вышло время ожидания ответа от пользователя, текущий контекст очищен")
                        .allowSendingWithoutReply(true)
                        .build()
                )
            } catch (e: Exception) {
                logger.error("Cannot send notification about error to user", e)
            }
        } catch (e: Throwable) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e, update, botSession)
            )
            tryAnswer(update)
        }
    }

    private fun tryAnswer(update: Update?) {
        try {
            execute(
                SendMessage.builder()
                    .chatId(update?.message?.chatId ?: update?.callbackQuery?.message?.chatId!!)
                    //todo: fix message
                    .text("Простите, при обработке вашего запроса произошла ошибка")
                    .allowSendingWithoutReply(true)
                    .build()
            )
        } catch (e: Exception) {
            logger.error("Cannot send notification about error to user", e)
        }
    }

    //todo: remove injection
    private fun setBot() {
        for (handler in handlers) {
            if (handler is PendingBalancePaymentHandler) {
                handler.setBot(this)
            }
        }
    }

    private fun validateBotCommands() {
        val botCommands = handlers.mapNotNull { it.getBotCommand() }
        val commands = mutableListOf<String>()
        for (botCommand in botCommands) {
            if (commands.contains(botCommand.command)) {
                throw RuntimeException("Command duplicates: '${botCommand.command}'")
            }
            commands.add(botCommand.command)
        }
    }

    private fun validateOnCallbackQueryCommands() {
        val botCommands = handlers.filterIsInstance(CallbackQueryHandler::class.java).map { it.getCommandName() }
        val commands = mutableListOf<String>()
        for (botCommand in botCommands) {
            if (commands.contains(botCommand)) {
                throw RuntimeException("Command duplicates: '${botCommand}'")
            }
            commands.add(botCommand)
        }
    }

    companion object {
        private fun withBotOptions() =
            DefaultBotOptions()
                .also { it.allowedUpdates = listOf("message", "edited_channel_post", "callback_query", "chat_member") }
    }
}