package ru.apexman.botpupilsbalances.dto

data class OperationResult<out E>(
    val success: Boolean,
    val result: E,
    val errors: MutableCollection<String> = mutableListOf(),
)