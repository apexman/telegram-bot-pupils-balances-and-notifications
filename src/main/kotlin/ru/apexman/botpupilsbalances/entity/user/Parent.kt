package ru.apexman.botpupilsbalances.entity.user

import jakarta.persistence.*
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment

@Entity
@Table(name = "parents")
class Parent(
    val publicId: String,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "parents_students_relations",
        joinColumns = [JoinColumn(name = "parent_id")],
        inverseJoinColumns = [JoinColumn(name = "student_id")]
    )
    val students: MutableCollection<Student> = mutableListOf(),
    @OneToMany(mappedBy = "parent")
    val pendingBalancePayments: MutableCollection<PendingBalancePayment> = mutableListOf(),
) : AbstractEntityWithLongKey()