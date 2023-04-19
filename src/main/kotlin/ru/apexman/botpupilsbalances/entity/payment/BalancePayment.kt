package ru.apexman.botpupilsbalances.entity.payment

import jakarta.persistence.*
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.Document
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.user.Student

@Entity
@Table(name = "balance_payments")
class BalancePayment(
    @ManyToOne
    @JoinColumn(name = "created_by_contact_id")
    var createdByContact: Contact?,
    var createdBy: String,
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    @ManyToOne(cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "document_id")
    var document: Document? = null,
    val delta: Int,
    val approvedBy: String,
    val comment: String? = null,
    @OneToOne(mappedBy = "balancePayment")
    val pendingBalancePayment: PendingBalancePayment? = null,
) : AbstractEntityWithLongKey()