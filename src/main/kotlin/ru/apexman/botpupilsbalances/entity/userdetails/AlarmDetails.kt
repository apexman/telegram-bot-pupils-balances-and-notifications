package ru.apexman.botpupilsbalances.entity.userdetails

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.user.Student
import java.time.LocalDateTime

@Entity
@Table(name = "alarm_details")
class AlarmDetails(
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    val details: String,
    val alarmedBy: String? = null,
    val disabledAt: LocalDateTime? = null,
    val disabledBy: String? = null,
) : AbstractEntityWithLongKey()