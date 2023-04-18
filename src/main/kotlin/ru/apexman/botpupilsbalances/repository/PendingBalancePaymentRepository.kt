package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment

interface PendingBalancePaymentRepository : JpaRepository<PendingBalancePayment, Long>