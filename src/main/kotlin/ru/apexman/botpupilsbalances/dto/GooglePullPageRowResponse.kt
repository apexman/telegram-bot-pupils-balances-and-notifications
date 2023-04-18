package ru.apexman.botpupilsbalances.dto

import java.math.BigDecimal
import java.time.LocalDate

data class GooglePullPageRowResponse(
    val name: String,
    val birthday: LocalDate,
    val dateEnrollment: LocalDate,
    val classNum: Int,
    val hostel: Boolean,
    val discount: BigDecimal,
)
