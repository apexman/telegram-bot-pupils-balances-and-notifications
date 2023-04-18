package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.PaymentService
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler

/**
 * Обновляет количество дней
 */
@Component
class EditBalanceHandler(
    private val paymentService: PaymentService,
    private val studentRepository: StudentRepository,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/edit_balance", "Обновляет количество дней")
    }

    override fun handle(update: Update, botSession: Session?): Collection<PartialBotApiMethod<Message>> {
        val args = parseArgs(update)
        if (args.isEmpty() || args.size < 2 || args[1].toIntOrNull() == null) {
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: /edit_balance <id> <balance>")
                .build())
        }
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId)
            ?: return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Ученик не найден")
                .build())
        val balance = args[1].toInt()
        paymentService.createNewBalance(student, balance, getCommandRequester(update))
        student.balance = balance
        studentRepository.save(student)
        return listOf(SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Установлен новый баланс: $balance")
            .build())
    }
}