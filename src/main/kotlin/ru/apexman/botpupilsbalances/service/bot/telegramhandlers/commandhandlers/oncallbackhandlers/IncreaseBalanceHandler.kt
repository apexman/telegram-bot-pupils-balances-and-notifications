package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers

import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.repository.PendingBalancePaymentRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.PaymentService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.CallbackQueryHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

@Service
class IncreaseBalanceHandler(
    private val paymentService: PaymentService,
    private val studentRepository: StudentRepository,
    private val pendingBalancePaymentRepository: PendingBalancePaymentRepository,
) : TelegramMessageHandler, CallbackQueryHandler {
    private val logger = LoggerFactory.getLogger(IncreaseBalanceHandler::class.java)

    override fun getBotCommand(): BotCommand? {
        return null
    }

    override fun getCommandName(): String {
        return "/increase_balance"
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
        val pendingBalanceId = args[1].toLongOrNull()
            ?: return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Платеж не найден")
                    .build()
            )
        val pendingBalancePayment = pendingBalancePaymentRepository.findById(pendingBalanceId).orElse(null )
            ?: return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Платеж не найден")
                    .build()
            )
        if (pendingBalancePayment.approvedAt != null) {
            return listOf(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(update.callbackQuery.id)
                    .text("Платеж обработан: ${pendingBalancePayment.approvedBy}")
                    .build()
            )
        }
        val newBalance = student.balance + INCREASING_BALANCE_DELTA
        paymentService.movePendingBalanceWithNewBalance(student, newBalance,  pendingBalancePayment,  getCallbackCommandRequester(update))
        student.balance = newBalance
        studentRepository.save(student)
        return listOf(AnswerCallbackQuery.builder()
            .callbackQueryId(update.callbackQuery.id)
            .text("Баланс изменен")
            .build())
    }

    companion object {
        const val INCREASING_BALANCE_DELTA = 28
    }
}