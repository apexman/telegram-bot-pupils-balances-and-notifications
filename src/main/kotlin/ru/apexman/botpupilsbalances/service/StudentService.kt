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
import ru.apexman.botpupilsbalances.entity.payment.BalancePayment
import ru.apexman.botpupilsbalances.entity.payment.Penalty
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.entity.userdetails.AlarmDetails
import ru.apexman.botpupilsbalances.entity.userdetails.Comment
import ru.apexman.botpupilsbalances.repository.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val commentRepository: CommentRepository,
    private val alarmDetailsRepository: AlarmDetailsRepository,
    private val contactRepository: ContactRepository,
    private val balancePaymentRepository: BalancePaymentRepository,
    private val penaltyRepository: PenaltyRepository,
    private val contactService: ContactService,
) {
    private val logger = LoggerFactory.getLogger(StudentService::class.java)

    @PostConstruct
    fun test() {
        val students = studentRepository.findAll()
        println(students)
        val googleMainPageRows = students.map { toGoogleMainPageRowRequest(it) }
        println(googleMainPageRows)
    }

    fun toGoogleMainPageRowRequest(student: Student): GoogleMainPageRowRequest {
        val alarmDetails = alarmDetailsRepository.findFirstByStudentOrderByCreatedAtDesc(student)
        val alarmDetailsComment = if (alarmDetails?.disabledAt != null) null else alarmDetails?.details
        val contacts = contactRepository.findAllByStudent(student)
        return GoogleMainPageRowRequest(
            googleId = student.googleId,
            publicId = student.publicId,
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

    @Transactional
    fun createNewStudents(googlePullPageRowResponses: Collection<GooglePullPageRowResponse>): Collection<Student> {
        //TODO: create student's price
        val students = googlePullPageRowResponses.map { Student.from(it) }
        studentRepository.saveAll(students)
        for (student in students) {
            student.googleId = createGoogleId(student)
            student.publicId = createPublicId(student)
        }
        studentRepository.saveAll(students)
        return students
    }

    @Transactional
    fun updateStudents(pageRows: Collection<GoogleMainPageRowResponse>, modifiedBy: String): Collection<Student> {
        val students = mutableListOf<Student>()
        val comments = mutableListOf<Comment>()
        val alarmDetailsList = mutableListOf<AlarmDetails>()
        val balances = mutableListOf<BalancePayment>()
        val penalties = mutableListOf<Penalty>()
        val contacts = mutableListOf<Contact>()
        for (pageRow in pageRows) {
            val student = studentRepository.findByGoogleId(pageRow.googleId) ?: continue
            updateSimple(pageRow, student)
            updateBalance(pageRow, student, modifiedBy, balances)
            updatePenalty(pageRow, student, modifiedBy, penalties)
            updateTgIds(pageRow, student, contacts)
            updateComments(pageRow, student, modifiedBy, comments)
            updateAlarm(pageRow, student, modifiedBy, alarmDetailsList)
            students.add(student)
        }
        studentRepository.saveAll(students)
        commentRepository.saveAll(comments)
        alarmDetailsRepository.saveAll(alarmDetailsList)
        balancePaymentRepository.saveAll(balances)
        penaltyRepository.saveAll(penalties)
        contactRepository.saveAll(contacts)
        return students
    }

    private fun updateSimple(
        pageRow: GoogleMainPageRowResponse,
        student: Student,
    ) {
        student.publicId = pageRow.publicId
        student.fullUserName = pageRow.name
        student.birthday = pageRow.birthday
        student.dateEnrollment = pageRow.dateEnrollment
        student.classNum = pageRow.classNum
        student.isHostel = pageRow.hostel
        student.discount = pageRow.discount
        //TODO: handle price changing
        student.price = pageRow.price
        student.currencyName = pageRow.currency
        student.isPause = pageRow.pause
    }

    private fun updateBalance(
        pageRow: GoogleMainPageRowResponse,
        student: Student,
        modifiedBy: String,
        balances: MutableList<BalancePayment>,
    ) {
        val balanceDelta = pageRow.balance - student.balance
        student.balance = pageRow.balance
        if (balanceDelta != 0) {
            val balancePayment = BalancePayment(
                createdByContact = null,
                createdBy = modifiedBy,
                student = student,
                document = null,
                delta = balanceDelta,
                comment = null,
                approvedBy = modifiedBy
            )
            student.balances.add(balancePayment)
            balances.add(balancePayment)
        }
    }

    private fun updatePenalty(
        pageRow: GoogleMainPageRowResponse,
        student: Student,
        modifiedBy: String,
        penalties: MutableList<Penalty>,
    ) {
        val penaltyDelta = pageRow.penalty - student.penalty
        student.penalty = pageRow.penalty
        if (penaltyDelta.compareTo(BigDecimal.ZERO) != 0) {
            val penalty = Penalty(
                createdBy = modifiedBy,
                student = student,
                delta = penaltyDelta,
                currencyName = student.currencyName,
            )
            student.penalties.add(penalty)
            penalties.add(penalty)
        }
    }

    private fun updateTgIds(
        pageRow: GoogleMainPageRowResponse,
        student: Student,
        allContacts: MutableList<Contact>,
    ) {
        //TODO: must change parent and child chat id on tg ids changing
        pageRow.parentId?.let { parentId ->
            val contact = contactService.buildContact(student, ContactType.PARENT_ID, parentId)
            student.contacts.add(contact)
            allContacts.add(contact)
            contact
        }
        pageRow.childId?.let { childId ->
            val contact = contactService.buildContact(student, ContactType.CHILD_ID, childId)
            student.contacts.add(contact)
            allContacts.add(contact)
            contact
        }
    }

    private fun updateComments(
        pageRow: GoogleMainPageRowResponse,
        student: Student,
        modifiedBy: String,
        comments: MutableList<Comment>,
    ) {
        val lastComment = commentRepository.findFirstByStudentOrderByCreatedAtDesc(student)
        pageRow.comment?.let { comment ->
            if (lastComment?.comment != comment) {
                val newComment = Comment(student, comment, modifiedBy)
                student.comments.add(newComment)
                comments.add(newComment)
            }
        }
    }

    private fun updateAlarm(
        pageRow: GoogleMainPageRowResponse,
        student: Student,
        modifiedBy: String,
        alarmDetailsList: MutableList<AlarmDetails>,
    ) {
        val alarmBefore = student.isAlarm
        pageRow.alarm.let { newAlarm ->
            if (!newAlarm && student.isAlarm) {
                alarmDetailsRepository.disableActiveStudentAlarmDetails(student, modifiedBy, LocalDateTime.now())
            }
        }
        student.isAlarm = pageRow.alarm
        val lastAlarmDetails =
            alarmDetailsRepository.findFirstByStudentOrderByCreatedAtDesc(student)
        pageRow.alarmDetails?.let { alarmDetails ->
            if (lastAlarmDetails?.details != alarmDetails
                || lastAlarmDetails.disabledAt != null && pageRow.alarm
            ) {
                val disabledAt =
                    if (!pageRow.alarm && alarmBefore || !pageRow.alarm && !alarmBefore) LocalDateTime.now() else null
                val disabledBy = if (disabledAt != null) modifiedBy else null
                val alarmedBy = if (pageRow.alarm && !alarmBefore) modifiedBy else null
                val newAlarmDetails = AlarmDetails(student, alarmDetails, alarmedBy, disabledAt, disabledBy)
                student.alarmDetails.add(newAlarmDetails)
                alarmDetailsList.add(newAlarmDetails)
            }
        }
    }

    private fun createGoogleId(student: Student): String {
        return student.id.toString().padStart(4, '0')
    }

    private fun createPublicId(student: Student): String {
        return "${student.googleId}${getIdSalt()}"
    }

    private fun getIdSalt(): Int {
        val minLength = 3
        val from = 10.0.pow(minLength).roundToInt()
        val until = from * 10 - 1
        return Random.nextInt(from, until)
    }

}