package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.user.Student

interface StudentRepository : JpaRepository<Student, Long> {

    fun existsByGoogleId(googleId: String): Boolean

    fun existsByPublicId(publicId: String): Boolean

    fun findByGoogleId(googleId: String): Student?

    fun findByPublicId(publicId: String): Student?

}