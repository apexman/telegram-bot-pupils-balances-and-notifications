package ru.apexman.botpupilsbalances.entity.contact

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.user.Student

@Entity
@Table(name = "contacts")
class Contact(
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    val contactType: String,
    var contactValue: String,
) : AbstractEntityWithLongKey()