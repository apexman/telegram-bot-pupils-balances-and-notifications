package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.payment.BalancePayment

interface BalancePaymentRepository : JpaRepository<BalancePayment, Long>