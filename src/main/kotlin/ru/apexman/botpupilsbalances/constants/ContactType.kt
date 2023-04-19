package ru.apexman.botpupilsbalances.constants

enum class ContactType {
    PARENT_TELEGRAM_USERNAME,   //username телеграм аккаунта родителя
    PARENT_ID,  //id телеграм аккаунта родителя
    PARENT_CHAT_ID,  //chat id родителя
    PARENT_TELEGRAM_FULL_NAME,  //full name из тг
    CHILD_TELEGRAM_USERNAME,    //username телеграм аккаунта родителя
    CHILD_ID,   //id телеграм аккаунта ребёнка
    CHILD_CHAT_ID,  //chat id ребенка
    CHILD_TELEGRAM_FULL_NAME,  //full name из тг
}