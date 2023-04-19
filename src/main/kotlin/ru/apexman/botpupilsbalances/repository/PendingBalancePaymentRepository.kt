package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment
import ru.apexman.botpupilsbalances.entity.user.Student

interface PendingBalancePaymentRepository : JpaRepository<PendingBalancePayment, Long> {

    fun findFirstByStudentAndApprovedAtIsNotNullOrderByCreatedAtDesc(student: Student): PendingBalancePayment?

}