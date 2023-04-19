package ru.apexman.botpupilsbalances.dto

import java.time.LocalDate

data class AddHandlerDataDto(
    var state: String,
    var name: String? = null,
    var birthday: LocalDate? = null,
    var dateEnrollment: LocalDate? = null,
    var classNum: Int? = null,
    var isHostel: Boolean? = null,
) {
    fun canMap(): Boolean {
        return name != null
                && birthday != null
                && dateEnrollment != null
                && classNum != null
                && isHostel != null
    }
}