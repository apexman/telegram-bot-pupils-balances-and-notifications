package ru.apexman.botpupilsbalances.service

import com.google.api.client.util.Data
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.dto.GoogleMainPageRowRequest
import ru.apexman.botpupilsbalances.dto.GoogleMainPageRowResponse
import ru.apexman.botpupilsbalances.dto.GooglePullPageRowResponse
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.user.Parent
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.entity.userdetails.AlarmDetails
import ru.apexman.botpupilsbalances.entity.userdetails.Comment
import ru.apexman.botpupilsbalances.repository.*
import ru.apexman.botpupilsbalances.service.notification.TelegramNotificationService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val telegramNotificationService: TelegramNotificationService,
    private val commentRepository: CommentRepository,
    private val alarmDetailsRepository: AlarmDetailsRepository,
    private val parentRepository: ParentRepository,
    private val contactRepository: ContactRepository,
) {
    private val logger = LoggerFactory.getLogger(StudentService::class.java)

    @PostConstruct
    fun test() {
        val students = studentRepository.findAll()
        println(students)
        val googleMainPageRows = students.map { toGoogleMainPageRowRequest(it) }
        println(googleMainPageRows)
    }

    @Transactional
    fun createNewStudents(googlePullPageRowResponses: Collection<GooglePullPageRowResponse>): Collection<Student> {
        val students = googlePullPageRowResponses.map { Student.from(it) }
        studentRepository.saveAll(students)
        val newParents = mutableListOf<Parent>()
        for (student in students) {
            student.googleId = createGoogleId(student)
            val parent = Parent(createPublicId(student), mutableListOf(student))
            student.parents.add(parent)
            newParents.add(parent)
        }
        studentRepository.saveAll(students)
        parentRepository.saveAll(newParents)
        return students
    }

    @Transactional
    fun updateStudents(pageRows: Collection<GoogleMainPageRowResponse>, modifiedBy: String): Collection<Student> {
        val students = mutableListOf<Student>()
        val comments = mutableListOf<Comment>()
        val alarmDetailsList = mutableListOf<AlarmDetails>()
        for (pageRow in pageRows) {
            val student = studentRepository.findByGoogleId(pageRow.googleId) ?: continue
            val contacts = contactRepository.findAllByStudent(student)
            val lastComment = commentRepository.findFirstByStudentOrderByCreatedAtDesc(student)
            //TODO: how save new public_id
//        student.publicId = pageRow.publicId
            student.fullUserName = pageRow.name
            student.birthday = pageRow.birthday
            student.dateEnrollment = pageRow.dateEnrollment
            student.classNum = pageRow.classNum
            student.isHostel = pageRow.hostel
            student.discount = pageRow.discount
            student.price = pageRow.price
            student.currencyName = pageRow.currency
            student.balance = pageRow.balance
            pageRow.parentId?.let { parentId ->
                if (contacts.find { it.contactType == ContactType.PARENT_ID.name } == null) {
                    val contact = Contact(student, ContactType.PARENT_ID.name, parentId)
                    student.contacts.add(contact)
                }
            }
            pageRow.childId?.let { childId ->
                if (contacts.find { it.contactType == ContactType.CHILD_ID.name } == null) {
                    val contact = Contact(student, ContactType.CHILD_ID.name, childId)
                    student.contacts.add(contact)
                }
            }
            student.isPause = pageRow.pause
            pageRow.comment?.let { comment ->
                if (lastComment?.comment != comment) {
                    val newComment = Comment(student, comment, modifiedBy)
                    student.comments.add(newComment)
                    comments.add(newComment)
                }
            }
            val alarmBefore = student.isAlarm
            pageRow.alarm.let { newAlarm ->
                if (!newAlarm && student.isAlarm) {
                    alarmDetailsRepository.disableActiveStudentAlarmDetails(student, modifiedBy, LocalDateTime.now())
                }
            }
            val lastAlarmDetails =
                alarmDetailsRepository.findFirstByStudentOrderByCreatedAtDesc(student)
            student.isAlarm = pageRow.alarm
            pageRow.alarmDetails?.let { alarmDetails ->
                if (lastAlarmDetails?.details != alarmDetails
                    || lastAlarmDetails.disabledAt != null && pageRow.alarm
                ) {
                    val disabledAt = if (!pageRow.alarm && alarmBefore || !pageRow.alarm && !alarmBefore) LocalDateTime.now() else null
                    val disabledBy = if (disabledAt != null) modifiedBy else null
                    val alarmedBy = if (pageRow.alarm && !alarmBefore) modifiedBy else null
                    val newAlarmDetails = AlarmDetails(student, alarmDetails, alarmedBy, disabledAt, disabledBy)
                    student.alarmDetails.add(newAlarmDetails)
                    alarmDetailsList.add(newAlarmDetails)
                }
            }
            student.penalty = pageRow.penalty
            students.add(student)
        }
        studentRepository.saveAll(students)
        commentRepository.saveAll(comments)
        alarmDetailsRepository.saveAll(alarmDetailsList)
        return students
    }

    fun toGoogleMainPageRowRequest(student: Student): GoogleMainPageRowRequest {
        val firstParent = parentRepository.findFirstByStudentsInOrderByIdDesc(listOf(student))
        val alarmDetails = alarmDetailsRepository.findFirstByStudentOrderByCreatedAtDesc(student)
        val alarmDetailsComment = if (alarmDetails?.disabledAt != null) null else alarmDetails?.details
        val contacts = contactRepository.findAllByStudent(student)
        return GoogleMainPageRowRequest(
            googleId = student.googleId,
            publicId = firstParent?.publicId ?: Data.NULL_STRING,
            name = student.fullUserName,
            birthday = student.birthday.format(DateTimeFormatter.ofPattern(Parsers.DATE_PATTERN)),
            age = ChronoUnit.YEARS.between(student.birthday, LocalDate.now()).toString(),
            dateEnrollment = student.dateEnrollment.format(DateTimeFormatter.ofPattern(Parsers.DATE_PATTERN)),
            classNum = student.classNum.toString(),
            hostel = Parsers.BOOLEAN_TO_STRING(student.isHostel),
            discount = student.discount.stripTrailingZeros().toPlainString(),
            price = student.price.stripTrailingZeros().toPlainString(),
            currency = student.currencyName,
            balance = student.balance.toString(),
            parentId = contacts.find { it.contactType == ContactType.PARENT_ID.name }?.contactValue ?: Data.NULL_STRING,
            childId = contacts.find { it.contactType == ContactType.CHILD_ID.name }?.contactValue ?: Data.NULL_STRING,
            pause = Parsers.BOOLEAN_TO_STRING(student.isPause),
            comment = commentRepository.findFirstByStudentOrderByCreatedAtDesc(student)?.comment ?: Data.NULL_STRING,
            alarm = Parsers.BOOLEAN_TO_STRING(student.isAlarm),
            alarmDetails = alarmDetailsComment ?: Data.NULL_STRING,
            penalty = student.penalty.stripTrailingZeros().toPlainString(),
        )
    }

    private fun createGoogleId(student: Student): String {
        return student.id.toString().padStart(4, '0')
    }

    private fun createPublicId(student: Student): String {
        val paddedId = student.googleId
        var newPublicId = "$paddedId${getIdSalt()}"
        for (i in 1..3) {
            if (!parentRepository.existsByPublicId(newPublicId)) {
                break
            }
            newPublicId = "${paddedId}${getIdSalt()}"
        }
        if (parentRepository.existsByPublicId(newPublicId)) {
            newPublicId = UUID.randomUUID().toString()
            val error = "Could not create unique public id for student id: ${student.id}, got public id = $newPublicId"
            logger.error(error)
            telegramNotificationService.sendMonitoring(error)
        }
        return newPublicId
    }

    private fun getIdSalt(): Int {
        val minLength = 3
        val from = 10.0.pow(minLength).roundToInt()
        val until = from * 10 - 1
        return Random.nextInt(from, until)
    }

}