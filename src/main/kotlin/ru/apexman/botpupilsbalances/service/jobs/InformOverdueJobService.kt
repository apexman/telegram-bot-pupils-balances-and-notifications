package ru.apexman.botpupilsbalances.service.jobs

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.service.bot.ThisTelegramBot
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers.GetHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService
import ru.apexman.botpupilsbalances.service.notification.TelegramProperties

/**
 * Отправляет список учеников с информацией о предстоящем платеже
 */
@Service
class InformOverdueJobService(
    private val thisTelegramBot: ThisTelegramBot,
    private val telegramProperties: TelegramProperties,
    private val telegramNotificationService: TelegramNotificationService,
    private val getHandler: GetHandler,
) : Job {
    private val logger = LoggerFactory.getLogger(InformOverdueJobService::class.java)

    override fun execute(context: JobExecutionContext) {
        try {
            logger.trace("Triggered!")
            getHandler.buildTextWithOverdueBalances()
                .map { (student, text) ->
                    val messageToManagersChat = SendMessage.builder()
                        .chatId(telegramProperties.collectingReceiptsChatId)
                        .text(text)
                        .build()
                    val messageToAdminsChat = SendMessage.builder()
                        .chatId(telegramProperties.adminsChatId)
                        .text(text)
                        .replyMarkup(getHandler.buildInlineKeyboardMarkup(student))
                        .build()
                    val otherNotifications = student.contacts
                        .filter { it.contactType == ContactType.PARENT_CHAT_ID.name }
                        .map { it.contactValue }
                        .map {
                            SendMessage.builder()
                                .chatId(it)
                                .text(text)
                                .build()
                        }.ifEmpty {
                            listOf(
                                SendMessage.builder()
                                    .chatId(telegramProperties.adminsChatId)
                                    .text(
                                        """
                                    Уведомление не отправлено родителю, так как не было привязки аккаунта
                                    ID ученика: ${student.googleId}
                                    """.trimIndent()
                                    )
                                    .build()
                            )
                        }
                    listOf(messageToAdminsChat, messageToManagersChat) + otherNotifications
                }.flatten().forEach { thisTelegramBot.execute(it) }
        } catch (e: Throwable) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e)
            )
        }
    }

}