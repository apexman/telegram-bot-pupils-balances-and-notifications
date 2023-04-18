package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.user.Parent
import ru.apexman.botpupilsbalances.entity.user.Student

interface ParentRepository : JpaRepository<Parent, Long> {

    fun findFirstByStudentsInOrderByIdDesc(students: Collection<Student>): Parent?

    fun findByPublicId(publicId: String): Parent?

    fun existsByPublicId(newPublicId: String): Boolean

}