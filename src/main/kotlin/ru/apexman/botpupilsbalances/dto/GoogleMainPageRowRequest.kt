package ru.apexman.botpupilsbalances.dto

data class GoogleMainPageRowRequest(
    val googleId: String,
    val publicId: String,
    val name: String,
    val birthday: String,
    val age: String,
    val dateEnrollment: String,
    val classNum: String,
    val hostel: String,
    val discount: String,
    val price: String,
    val currency: String,
    val balance: String,
    val parentId: String,
    val childId: String,
    val pause: String,
    val comment: String,
    val alarm: String,
    val alarmDetails: String,
    val penalty: String,
)
