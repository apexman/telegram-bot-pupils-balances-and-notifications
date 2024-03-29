package ru.apexman.botpupilsbalances.entity.payment

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.Document
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.user.Student
import java.time.LocalDateTime

@Entity
@Table(name = "pending_balance_payments")
class PendingBalancePayment(
    @ManyToOne
    @JoinColumn(name = "created_by_contact_id")
    val createdByContact: Contact?,
    val createdBy: String,
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    @ManyToOne
    @JoinColumn(name = "document_id")
    val document: Document? = null,
    var approvedAt: LocalDateTime? = null,
    var approvedBy: String? = null,
    @OneToOne
    @JoinColumn(name = "balance_payment_id")
    var balancePayment: BalancePayment? = null,
) : AbstractEntityWithLongKey()