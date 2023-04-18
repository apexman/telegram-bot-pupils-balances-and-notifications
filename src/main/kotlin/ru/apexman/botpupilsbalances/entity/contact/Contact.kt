package ru.apexman.botpupilsbalances.entity.contact

import jakarta.persistence.*
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.payment.BalancePayment
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment
import ru.apexman.botpupilsbalances.entity.user.Student

@Entity
@Table(name = "contacts")
class Contact(
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    val contactType: String,
    var contactValue: String,
    @OneToMany(mappedBy = "createdByContact")
    val pendingBalancePayments: MutableCollection<PendingBalancePayment> = mutableListOf(),
    @OneToMany(mappedBy = "createdByContact")
    val balancePayments: MutableCollection<BalancePayment> = mutableListOf(),
) : AbstractEntityWithLongKey()