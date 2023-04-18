package ru.apexman.botpupilsbalances.service

import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.ContactRepository

@Service
class ContactService(
    private val contactRepository: ContactRepository,
) {

    fun buildContact(student: Student, type: ContactType, value: String): Contact {
        val contacts = contactRepository.findAllByStudent(student)
        val contact = contacts.find { it.contactType == type.name }
            ?: Contact(student, type.name, value)
        contact.contactValue = value
        return contact
    }

}