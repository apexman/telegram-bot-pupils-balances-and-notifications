package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.user.Student

interface ContactRepository : JpaRepository<Contact, Long> {

    fun findAllByStudent(student: Student): List<Contact>

    fun findByContactTypeAndContactValue(type: String, value: String): Collection<Contact>

}