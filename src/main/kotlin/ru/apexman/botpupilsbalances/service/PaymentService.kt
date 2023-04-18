package ru.apexman.botpupilsbalances.service

import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.entity.payment.BalancePayment
import ru.apexman.botpupilsbalances.entity.payment.Penalty
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.BalancePaymentRepository
import java.math.BigDecimal

@Service
class PaymentService(
    private val balancePaymentRepository: BalancePaymentRepository
) {

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