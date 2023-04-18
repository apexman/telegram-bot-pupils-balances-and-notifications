package ru.apexman.botpupilsbalances.entity

import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment

@Entity
@Table(name = "documents")
class Document(
    val documentName: String,
    val documentValue: ByteArray,
    val documentType: String,
    @OneToMany(mappedBy = "document")
    val pendingBalancePayments: MutableCollection<PendingBalancePayment> = mutableListOf(),
) : AbstractEntityWithLongKey()