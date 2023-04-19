package ru.apexman.botpupilsbalances.service.jobs

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.apexman.botpupilsbalances.repository.BalancePaymentRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.PaymentService
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService

/**
 * Уменьшает баланс на 1 за каждый проход, если студент не приостановлен
 */
@Service
class BalanceDecreaserJobService(
    private val telegramNotificationService: TelegramNotificationService,
    private val studentRepository: StudentRepository,
    private val balancePaymentRepository: BalancePaymentRepository,
    private val paymentService: PaymentService,
    private val informOverdueJobService: InformOverdueJobService,
) : Job {
    private val logger = LoggerFactory.getLogger(BalanceDecreaserJobService::class.java)

    @Transactional
    override fun execute(context: JobExecutionContext) {
        try {
            logger.trace("Triggered!")
            val students =
                studentRepository.findAllByIsPauseIsFalseAndBalanceLessThanEqual()
            val balancePayments = students.mapNotNull { paymentService.buildBalance(it, it.balance - 1, "scheduler") }
            students.forEach { it.balance -= 1 }
            studentRepository.saveAll(students)
            balancePaymentRepository.saveAll(balancePayments)
            //TODO: too many notifications
            //            informOverdueJobService.execute(context)
        } catch (e: Exception) {
            logger.error(e.toString(), e)
            telegramNotificationService.sendMonitoring(
                e.toString(),
                TelegramNotificationService.buildTelegramDocumentDto(e)
            )
            throw e //rollback transaction
        }
    }

}