package ru.apexman.botpupilsbalances.entity.user

import jakarta.persistence.*
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.dto.GooglePullPageRowResponse
import ru.apexman.botpupilsbalances.entity.AbstractEntityWithLongKey
import ru.apexman.botpupilsbalances.entity.contact.Contact
import ru.apexman.botpupilsbalances.entity.payment.Penalty
import ru.apexman.botpupilsbalances.entity.payment.PendingBalancePayment
import ru.apexman.botpupilsbalances.entity.userdetails.AlarmDetails
import ru.apexman.botpupilsbalances.entity.userdetails.Comment
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "students")
class Student(
    var googleId: String,
    var publicId: String,
    var fullUserName: String,
    var birthday: LocalDate,
    var dateEnrollment: LocalDate,
    var classNum: Int,
    @Column(name = "hostel")
    var isHostel: Boolean,
    var discount: BigDecimal,
    var price: BigDecimal,
    var currencyName: String,
    var balance: Int,
    @Column(name = "pause")
    var isPause: Boolean,
    @Column(name = "alarm")
    var isAlarm: Boolean,
    var penalty: BigDecimal,
    @OneToMany(mappedBy = "student")
    val contacts: MutableCollection<Contact> = mutableListOf(),
    @OneToMany(mappedBy = "student")
    val comments: MutableCollection<Comment> = mutableListOf(),
    @OneToMany(mappedBy = "student")
    val alarmDetails: MutableCollection<AlarmDetails> = mutableListOf(),
    @OneToMany(mappedBy = "student")
    val pendingBalancePayments: MutableCollection<PendingBalancePayment> = mutableListOf(),
    @OneToMany(mappedBy = "student")
    val penalties: MutableCollection<Penalty> = mutableListOf(),
) : AbstractEntityWithLongKey() {

    companion object {
        fun from(pageRow: GooglePullPageRowResponse): Student {
            return Student(
                googleId = UUID.randomUUID().toString(),
                publicId = UUID.randomUUID().toString(),
                fullUserName = pageRow.name,
                birthday = pageRow.birthday,
                dateEnrollment = pageRow.dateEnrollment,
                classNum = pageRow.classNum,
                isHostel = pageRow.hostel,
                discount = pageRow.discount,
                price = BigDecimal.ZERO,
                currencyName = Parsers.CLASS_NUM_TO_CURRENCY_NAME(pageRow.classNum).name,
                balance = 0,
                isPause = false,
                isAlarm = false,
                penalty = BigDecimal.ZERO,
                contacts = mutableListOf(),
                comments = mutableListOf(),
                alarmDetails = mutableListOf(),
                pendingBalancePayments = mutableListOf(),
                penalties = mutableListOf(),
            )
        }
    }

}