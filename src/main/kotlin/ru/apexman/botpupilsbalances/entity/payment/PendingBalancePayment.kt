package ru.apexman.botpupilsbalances.entity.payment

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.Document
import ru.apexman.botpupilsbalances.entity.user.Parent
import ru.apexman.botpupilsbalances.entity.user.Student

@Entity
@Table(name = "pending_balance_payments")
class PendingBalancePayment(
    @ManyToOne
    @JoinColumn(name = "parent_id")
    val parent: Parent,
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    @ManyToOne
    @JoinColumn(name = "document_id")
    val document: Document? = null,
) : AbstractEntityWithLongKey()