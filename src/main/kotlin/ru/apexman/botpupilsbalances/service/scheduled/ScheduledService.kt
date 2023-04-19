package ru.apexman.botpupilsbalances.service.scheduled

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.repository.ContactRepository
import ru.apexman.botpupilsbalances.service.bot.ThisTelegramBot

@Service
class ScheduledService(
    private val contactRepository: ContactRepository,
    private val thisTelegramBot: ThisTelegramBot,
) {

    //todo
    @Scheduled(cron = "0 * * ? * * ")
    fun schedule() {
        println(1)
    }

    fun sendAboutOverdue() {
        TODO("Not yet implemented")
    }

}