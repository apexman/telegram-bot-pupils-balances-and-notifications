package ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.adminschathandlers

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.shiro.session.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.dto.AddHandlerDataDto
import ru.apexman.botpupilsbalances.dto.SessionDataDto
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.AdminsChatHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.TelegramMessageHandler
import ru.apexman.botpupilsbalances.service.bot.telegramhandlers.commandhandlers.oncallbackhandlers.AddingUserHandler
import java.io.Serializable
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Процедура внесения нового ученика
 */
@Component
class AddHandler(
    private val mapper: ObjectMapper,
    private val addingUserHandler: AddingUserHandler,
) : TelegramMessageHandler, AdminsChatHandler {
    private val logger = LoggerFactory.getLogger(AddHandler::class.java)
    val routing = mapOf(
        this::handleStart.name to this::handleStart,
        this::processUserName.name to this::processUserName,
        this::handleBirthday.name to this::handleBirthday,
        this::processBirthday.name to this::processBirthday,
        this::handleDateEnrollment.name to this::handleDateEnrollment,
        this::processDateEnrollment.name to this::processDateEnrollment,
        this::handleClassNum.name to this::handleClassNum,
        this::processClassNum.name to this::processClassNum,
        this::handleIsHostel.name to this::handleIsHostel,
        this::processIsHostel.name to this::processIsHostel,
        this::handleConfirmation.name to this::handleConfirmation,
    )

    override fun getBotCommand(): BotCommand? {
        return BotCommand("/add", "Процедура внесения нового ученика")
    }

    override fun handle(update: Update, botSession: Session?): List<PartialBotApiMethod<out Serializable>> {
        val sessionData = botSession?.getAttribute(update.message.from.id) as SessionDataDto?
        val hadSessionData = sessionData != null
        val userSessionData = sessionData ?: SessionDataDto(getBotCommand()?.command)
        userSessionData.currentCommandName = getBotCommand()?.command

        val answers: List<PartialBotApiMethod<out Serializable>> = handleState(update, userSessionData)

        if (!hadSessionData) {
            botSession?.setAttribute(update.message.from.id, userSessionData)
        }
        return answers
    }

    //region state handlers
    private fun handleState(
        update: Update,
        userSessionData: SessionDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        val dataDto = deserialize(userSessionData.data ?: getDefaultData())
        val answers = routing.getOrDefault(dataDto.state, this::handleStart)
            .invoke(update, dataDto)
        userSessionData.data = serialize(dataDto)
        return answers
    }

    private fun handleStart(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.state = this::processUserName.name
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Вы хотите внести нового ученика в базу данных. Для отмены введите /cancel. Введите имя ученика")
                .build()
        )
    }

    private fun processUserName(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.name = update.message.text
        return handleBirthday(update, dataDto)
    }

    private fun handleBirthday(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.state = this::processBirthday.name
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Введите дату рождения ученика в формате «11.11.2012» без кавычек")
                .build()
        )
    }

    private fun processBirthday(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        val dateString = update.message.text.trim()
        if (parseDate(dateString) == null) {
            val answer = mutableListOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Неправильный формат даты дня рождения '${dateString}', ожидался формат: ${Parsers.DATE_PATTERN}")
                    .build()
            )
            return answer + handleBirthday(update, dataDto)
        }
        dataDto.birthday = parseDate(dateString)
        return handleDateEnrollment(update, dataDto)
    }

    private fun handleDateEnrollment(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.state = this::processDateEnrollment.name
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Введите дату поступления ученика в формате «10.10.2020» без кавычек")
                .build()
        )
    }

    private fun processDateEnrollment(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        val dateString = update.message.text.trim()
        if (parseDate(dateString) == null) {
            val answer = mutableListOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Неправильный формат даты поступления '${dateString}', ожидался формат: ${Parsers.DATE_PATTERN}")
                    .build()
            )
            return answer + handleDateEnrollment(update, dataDto)
        }
        dataDto.dateEnrollment = parseDate(dateString)
        return handleClassNum(update, dataDto)
    }

    private fun handleClassNum(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.state = this::processClassNum.name
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Введите класс")
                .build()
        )
    }

    private fun processClassNum(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        val any = update.message.text.trim()
        val classNum = any.toIntOrNull()
        if (classNum == null) {
            val answer = mutableListOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Неправильный формат номера класса '$any', ожидалось целое число")
                    .build()
            )
            return answer + handleClassNum(update, dataDto)
        }
        dataDto.classNum = classNum
        return handleIsHostel(update, dataDto)
    }

    private fun handleIsHostel(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.state = this::processIsHostel.name
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Проживает ли в пансионате школы?")
                .build()
        )
    }

    private fun processIsHostel(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        val any = update.message.text.trim()
        val isHostel = Parsers.STRING_TO_BOOLEAN(any)
        if (isHostel == null) {
            val answer = mutableListOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Неправильный формат пансионата '$any', ожидалось один из: " + Parsers.STRING_TO_BOOLEAN_KEYS.keys.joinToString(", ", "[", "]"))
                    .build()
            )
            return answer + handleIsHostel(update, dataDto)
        }
        dataDto.isHostel = isHostel
        return handleConfirmation(update, dataDto)
    }

    private fun handleConfirmation(
        update: Update,
        dataDto: AddHandlerDataDto,
    ): List<PartialBotApiMethod<out Serializable>> {
        dataDto.state = this::handleConfirmation.name
        if (!dataDto.canMap()) {
            logger.error("User session context is empty, must be values though")
            val answer = listOf(
                SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("Текущий контекст очищен")
                    .build()
            )
            dataDto.state = this::handleStart.name
            return answer + handleStart(update, dataDto)
        }
        return listOf(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("""
                    Подтвердите введенные данные:
                    Имя: ${dataDto.name}
                    День рождения: ${Parsers.LOCAL_DATE_TO_STRING(dataDto.birthday!!)}
                    Дата поступления: ${Parsers.LOCAL_DATE_TO_STRING(dataDto.dateEnrollment!!)}
                    Класс: ${dataDto.classNum}
                    Пансионат: ${Parsers.BOOLEAN_TO_STRING(dataDto.isHostel!!)}
                """.trimIndent())
                .replyMarkup(buildInlineKeyboardMarkup())
                .build()
        )
    }
    //endregion state handlers

    private fun getDefaultData(): String {
        return serialize(AddHandlerDataDto("handleStart"))
    }

    private fun serialize(dataDto: AddHandlerDataDto): String {
        return mapper.writeValueAsString(dataDto)
    }

    private fun deserialize(dataString: String): AddHandlerDataDto {
        return try {
            mapper.readValue(dataString, AddHandlerDataDto::class.java)
        } catch (_: JacksonException) {
            AddHandlerDataDto("handleStart")
        }
    }

    private fun parseDate(date: String): LocalDate? {
        return try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern(Parsers.DATE_PATTERN))
        } catch (e: DateTimeException) {
            null
        }
    }

    private fun buildInlineKeyboardMarkup(): InlineKeyboardMarkup {
        val confirmationButton = InlineKeyboardButton.builder()
            .text("Подтверждаю")
            .callbackData("${addingUserHandler.getCommandName()} true")
            .build()
        val cancelButton = InlineKeyboardButton.builder()
            .text("Отмена")
            .callbackData("${addingUserHandler.getCommandName()} false")
            .build()
        return InlineKeyboardMarkup.builder()
            .keyboard(listOf(listOf(confirmationButton, cancelButton)))
            .build()
    }

}