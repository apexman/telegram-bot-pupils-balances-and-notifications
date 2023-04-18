package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.repository.*
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable
import java.time.format.DateTimeFormatter

/**
 * Присылает подробную информацию об ученике со всеми деталями
 */
@Component
class ProfileHandler(
    private val studentRepository: StudentRepository,
    private val pendingBalancePaymentRepository: PendingBalancePaymentRepository,
    private val commentRepository: CommentRepository,
    private val alarmDetailsRepository: AlarmDetailsRepository,
    private val contactRepository: ContactRepository,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/profile", "Присылает подробную информацию об ученике со всеми деталями")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        if (args.isEmpty()) {
            return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Использование: /profile <id>")
                .build())
        }
        val googleId = args[0]
        val student = (studentRepository.findByGoogleId(googleId)
            ?: return listOf(SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Ученик не найден")
                .build()))
        val hasPendingBalancePayments =
            pendingBalancePaymentRepository.findFirstByStudentAndDisabledAtIsNotNullOrderByCreatedAtDesc(student) != null
        val lastComment = commentRepository.findFirstByStudentOrderByCreatedAtDesc(student)?.comment ?: ""
        val lastAlarmDetails =
            alarmDetailsRepository.findFirstByStudentOrderByCreatedAtDesc(student)
        val contacts = contactRepository.findAllByStudent(student)
        val parentTgIds = contacts
            .filter { it.contactType == ContactType.PARENT_ID.name }
            .joinToString(", ") { it.contactValue }
        val childTgIds = contacts
            .filter { it.contactType == ContactType.CHILD_ID.name }
            .joinToString(", ") { it.contactValue }
        val text = """
            Id: ${student.googleId}
            Public ID: ${student.publicId}
            Имя: ${student.fullUserName}
            Дата рождения: ${student.birthday.format(DateTimeFormatter.ofPattern(Parsers.DATE_PATTERN))}
            Дата поступления: ${student.dateEnrollment.format(DateTimeFormatter.ofPattern(Parsers.DATE_PATTERN))}
            Класс: ${student.classNum}
            Пансионат: ${Parsers.BOOLEAN_TO_STRING(student.isHostel)}
            Скидка: ${student.discount.stripTrailingZeros().toPlainString()}
            Цена: ${student.price.stripTrailingZeros().toPlainString()}
            Валюта: ${student.currencyName}
            Баланс: ${student.balance}
            Пауза: ${Parsers.BOOLEAN_TO_STRING(student.isPause)}
            Alarm: ${Parsers.BOOLEAN_TO_STRING(student.isAlarm)}
            Пени: ${student.penalty.stripTrailingZeros().toPlainString()}
            Есть необработанные платежи: ${Parsers.BOOLEAN_TO_STRING(hasPendingBalancePayments)}
            Последний комментарий: $lastComment
            Последний alarm_details: ${lastAlarmDetails?.details ?: ""}
            Контакты:
              Tg id родителя: $parentTgIds
              Tg id ученика: $childTgIds
        """.trimIndent()

        return listOf(SendMessage.builder()
            .chatId(update.message.chatId)
            .text(text)
            .build())
    }
}