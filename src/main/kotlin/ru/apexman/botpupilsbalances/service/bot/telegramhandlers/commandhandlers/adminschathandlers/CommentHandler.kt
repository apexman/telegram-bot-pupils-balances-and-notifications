package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import org.apache.shiro.session.Session
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import ru.apexman.botpupilsbalances.entity.userdetails.Comment
import ru.apexman.botpupilsbalances.repository.CommentRepository
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import java.io.Serializable

/**
 * Добавления комментария к ученику
 */
@Component
class CommentHandler(
    private val commentRepository: CommentRepository,
    private val studentRepository: StudentRepository,
) : TelegramMessageHandler, AdminsChatHandler {

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/comment", "Добавления комментария к ученику")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val args = parseArgs(update)
        if (args.isEmpty() || args.size < 2) {
            return listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Использование: /comment <id> <справочная информация>")
                    .build()
            )
        }
        val googleId = args[0]
        val student = studentRepository.findByGoogleId(googleId)
            ?: return listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Ученик не найден")
                    .build()
            )
        val comment = args.subList(1, args.size).joinToString(" ")
        commentRepository.save(Comment(student, comment, getBotCommandRequester(update)))
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Комментарий добавлен")
                .build()
        )
    }
}