package ru.apexman.botpupilsbalances.entity.payment

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.Document
import ru.apexman.botpupilsbalances.entity.user.Parent
import ru.apexman.botpupilsbalances.entity.user.Student
import java.math.BigDecimal

@Entity
@Table(name = "penalties")
class Penalty(
    @ManyToOne
    @JoinColumn(name = "pupil_id")
    val student: Student,
    val amount: BigDecimal,
    val currencyName: String,
    val createdBy: String? = null,
) : AbstractEntityWithLongKey()