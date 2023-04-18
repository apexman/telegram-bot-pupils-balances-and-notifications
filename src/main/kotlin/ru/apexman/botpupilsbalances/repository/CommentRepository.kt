package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.entity.userdetails.Comment

interface CommentRepository : JpaRepository<Comment, Long> {

    fun findFirstByStudentOrderByCreatedAtDesc(student: Student): Comment?

}