package ru.apexman.botpupilsbalances.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.apexman.botpupilsbalances.entity.payment.BalancePayment
import ru.apexman.botpupilsbalances.entity.payment.Penalty
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.BalancePaymentRepository
import ru.apexman.botpupilsbalances.repository.PendingBalancePaymentRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class PaymentService(
    private val balancePaymentRepository: BalancePaymentRepository,
    private val pendingBalancePaymentRepository: PendingBalancePaymentRepository,
) {

    @Transactional
    fun movePendingBalanceWithNewBalance(
        student: Student,
        newBalance: Int,
        pendingBalancePayment: PendingBalancePayment,
        modifiedBy: String,
    ) {
        val balancePayment = createNewBalance(student, newBalance, modifiedBy)!!
        balancePayment.createdByContact = pendingBalancePayment.createdByContact
        balancePayment.createdBy = pendingBalancePayment.createdBy
        balancePayment.document = pendingBalancePayment.document
        balancePaymentRepository.save(balancePayment)

        pendingBalancePayment.approvedBy = modifiedBy
        pendingBalancePayment.approvedAt = LocalDateTime.now()
        pendingBalancePayment.balancePayment = balancePayment
        pendingBalancePaymentRepository.save(pendingBalancePayment)
    }

    @Transactional
    fun createNewBalance(student: Student, newBalance: Int, modifiedBy: String): BalancePayment? {
        val builtBalance = buildBalance(student, newBalance, modifiedBy)
        if (builtBalance != null) {
            return balancePaymentRepository.save(builtBalance)
        }
        return null
    }

    fun buildBalance(student: Student, newBalance: Int, modifiedBy: String): BalancePayment? {
        val balanceDelta = newBalance - student.balance
        if (balanceDelta != 0) {
            return BalancePayment(
                createdByContact = null,
                createdBy = modifiedBy,
                student = student,
                document = null,
                delta = balanceDelta,
                comment = null,
                approvedBy = modifiedBy
            )
        }
        return null
    }

    fun buildPenalty(student: Student, newPenalty: BigDecimal, modifiedBy: String): Penalty? {
        val penaltyDelta = newPenalty - student.penalty
        if (penaltyDelta.compareTo(BigDecimal.ZERO) != 0) {
            return Penalty(
                createdBy = modifiedBy,
                student = student,
                delta = penaltyDelta,
                currencyName = student.currencyName,
            )
        }
        return null
    }

}