package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.apexman.botpupilsbalances.constants.ContactType
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.CommentRepository
import ru.apexman.botpupilsbalances.repository.PendingBalancePaymentRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.DeleteCallbackHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.IncreaseBalanceForceHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.PauseChangingHandler
import ru.apexman.botpupilsbalances.service.notification.TelegramProperties
import java.io.Serializable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class GetHandler(
    private val telegramProperties: TelegramProperties,
    private val studentRepository: StudentRepository,
    private val increaseBalanceForceHandler: IncreaseBalanceForceHandler,
    private val deleteCallbackHandler: DeleteCallbackHandler,
    private val pauseChangingHandler: PauseChangingHandler,
    private val commentRepository: CommentRepository,
    private val pendingBalancePaymentRepository: PendingBalancePaymentRepository,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/get", "Get some get")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        return buildTextWithOverdueBalances()
            .map {
                SendMessage.builder()
                    .chatId(telegramProperties.adminsChatId)
                    .text(it.second)
                    .replyMarkup(buildInlineKeyboardMarkup(it.first))
                    .build()
            }
    }

    fun buildTextWithOverdueBalances(): Collection<Pair<Student, String>> {
        return studentRepository.findAllByIsPauseIsFalseAndBalanceLessThanEqual()
            .sortedBy { it.fullUserName }
            .map {
                val balanceText =
                    if (it.balance < 0) "Просроченный период: ${it.balance} дней" else "Оплаченный период: ${it.balance} дней"
                Pair(it, balanceText)
            }.map { (student, balanceText) ->
                val lastComment = commentRepository.findFirstByStudentOrderByCreatedAtDesc(student)?.comment ?: ""
                val hasPendingBalancePayments =
                    pendingBalancePaymentRepository.findFirstByStudentAndApprovedAtIsNullOrderByCreatedAtDesc(student) != null
                val text = """
                    ${student.fullUserName}, ID: ${student.googleId}, ${
                    ChronoUnit.YEARS.between(
                        student.birthday,
                        LocalDate.now()
                    )
                } лет, 
                    ${student.classNum} класс, проживание: ${Parsers.BOOLEAN_TO_STRING(student.isHostel).uppercase()}, 
                    $balanceText,
                    Есть необработанные платежи: ${Parsers.BOOLEAN_TO_STRING(hasPendingBalancePayments)}
                    Справочная информация: '$lastComment',
                    Контакты:
                    Tg id родителя: ${
                    student.contacts.filter { it.contactType == ContactType.PARENT_ID.name }
                        .joinToString(", ") { it.contactValue }
                }
                    Tg id ученика: ${
                    student.contacts.filter { it.contactType == ContactType.CHILD_ID.name }
                        .joinToString(", ") { it.contactValue }
                }
                """.trimIndent()
                Pair(student, text)
            }
    }

    private fun buildInlineKeyboardMarkup(student: Student): ReplyKeyboard {
        val balanceIncreaseButton = InlineKeyboardButton.builder()
            .text("Оплата произведена (+28)")
            .callbackData("${increaseBalanceForceHandler.getCommandName()} ${student.googleId}")
            .build()
        val deleteButton = InlineKeyboardButton.builder()
            .text("Ученик выбыл")
            .callbackData("${deleteCallbackHandler.getCommandName()} ${student.googleId}")
            .build()
        val pauseButton = InlineKeyboardButton.builder()
            .text("Приостановить счетчик (PAUSE)")
            .callbackData("${pauseChangingHandler.getCommandName()} ${student.googleId} true")
            .build()
        return InlineKeyboardMarkup.builder()
            .keyboard(listOf(listOf(balanceIncreaseButton, deleteButton), listOf(pauseButton)))
            .build()
    }


}