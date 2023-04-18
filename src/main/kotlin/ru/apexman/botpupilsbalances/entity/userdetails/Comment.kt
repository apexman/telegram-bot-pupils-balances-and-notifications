package ru.apexman.botpupilsbalances.entity.userdetails

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.user.Student

@Entity
@Table(name = "comments")
class Comment(
    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,
    val comment: String,
    val commentedBy: String? = null,
) : AbstractEntityWithLongKey()