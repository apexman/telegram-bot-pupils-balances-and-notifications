package ru.apexman.botpupilsbalances.dto

import java.math.BigDecimal
import java.time.LocalDate

data class GoogleMainPageRowResponse(
    val googleId: String,
    val publicId: String,
    val name: String,
    val birthday: LocalDate,
//    val age: Long,
    val dateEnrollment: LocalDate,
    val classNum: Int,
    val hostel: Boolean,
    val discount: BigDecimal,
    val price: BigDecimal,
    val currency: String,
    val balance: Int,
    val parentId: String?,
    val childId: String?,
    val pause: Boolean,
    val comment: String?,
    val alarm: Boolean,
    val alarmDetails: String?,
    val penalty: BigDecimal,
)
