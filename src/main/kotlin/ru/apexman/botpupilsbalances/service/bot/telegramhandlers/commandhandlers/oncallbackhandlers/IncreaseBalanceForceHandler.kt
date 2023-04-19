package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.PaymentService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.IncreaseBalanceHandler.Companion.INCREASING_BALANCE_DELTA
import java.io.Serializable

@Service
class IncreaseBalanceForceHandler(
    private val paymentService: PaymentService,
    private val studentRepository: StudentRepository,
) : TelegramMessageHandler, CallbackQueryHandler {
    private val logger = LoggerFactory.getLogger(IncreaseBalanceForceHandler::class.java)

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/increase_balance_force"
    }

    @Transactional
    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId)
            ?: return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Ученик не найден")
                    .build()
            )
        val newBalance = student.balance + INCREASING_BALANCE_DELTA
        paymentService.createNewBalance(student, newBalance,  getCallbackCommandRequester(update))
        student.balance = newBalance
        studentRepository.save(student)
        return listOf(AnswerCallbackQuery.builder()
            .callbackQueryId(update.callbackQuery.id)
            .text("Баланс изменен")
            .build())
    }
}