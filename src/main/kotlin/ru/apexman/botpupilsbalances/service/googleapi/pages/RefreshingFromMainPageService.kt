package ru.apexman.botpupilsbalances.service.googleapi.pages

import com.google.api.services.sheets.v4.Sheets
import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.constants.CurrencyName
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.dto.GoogleMainPageRowResponse
import ru.apexman.botpupilsbalances.dto.OperationResult
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.StudentService
import ru.apexman.botpupilsbalances.service.googleapi.SheetsProperties


@Service
class RefreshingFromMainPageService(
    private val sheetsProperties: SheetsProperties,
    private val sheets: Sheets,
    private val studentRepository: StudentRepository,
    private val studentService: StudentService,
) : AbstractSheetsService() {

    @Synchronized
    fun refreshFromMainPage(): OperationResult<Collection<GoogleMainPageRowResponse>> {
        val result = mutableListOf<GoogleMainPageRowResponse>()
        val errors = mutableListOf<String>()
        val rowsNumber = rowsCountAtRequest
        var startRowNumber = 2
        var hasMoreData = true
        while (hasMoreData) {
            val endRowNumber = startRowNumber + rowsNumber
            val response = sheets.spreadsheets()
                .values()
                .batchGet(sheetsProperties.sheetId)
                .setRanges(listOf("${sheetsProperties.mainTableName}!A$startRowNumber:S$endRowNumber"))
                .setValueRenderOption("UNFORMATTED_VALUE")
                .setDateTimeRenderOption("SERIAL_NUMBER")
                .execute()
            val values = getTableValues(response.values)
            values.forEachIndexed { index, value ->
                val (googlePullPageRow, rowErrors) = parseMainPageRow(value)
                if (googlePullPageRow != null) {
                    result.add(googlePullPageRow)
                }
                if (rowErrors.isNotEmpty()) {
                    errors.add(
                        "Ошибки в строке ${index + startRowNumber}: " +
                                rowErrors.joinToString(", ", "[", "]")
                    )
                }
            }

            hasMoreData = values.size >= rowsNumber
            startRowNumber = endRowNumber + 1
        }
        return OperationResult(errors.isEmpty(), result, errors)
    }

    /**
     * Ожидаемый порядок колонок:
    "ID",
    "PUBLIC_ID",
    "NAME",
    "BIRTHDAY",
    "AGE",
    "DATE_ENROLLMENT",
    "CLASS",
    "HOSTEL",
    "DISCONT",
    "PRICE",
    "CURRENCY",
    "BALANCE",
    "PARENT_ID",
    "CHILD_ID",
    "PAUSE",
    "COMMENT",
    "ALARM",
    "ALARM_DETALS",
    "PENALTY",
     */
    private fun parseMainPageRow(values: List<Any>): Pair<GoogleMainPageRowResponse?, MutableCollection<String>> {
        //TODO: what if some fields can be empty?
        val errors = mutableListOf<String>()
        var googleId = parse(values.getOrNull(0), this::parseString, errors,
            "Идентификатор ID не заполнен")
        val publicId = parse(values.getOrNull(1), this::parseString, errors,
            "Идентификатор PUBLIC_D не заполнен")
        val name = parse(values.getOrNull(2), this::parseString, errors,
            "Имя не заполнено")
        val birthday = parse(values.getOrNull(3), this::parseDate, errors,
            "Неправильный формат даты дня рождения '${values.getOrNull(3)}', ожидался формат: ${Parsers.DATE_PATTERN}")
        val age = values[4]//ignore
        val dateEnrollment = parse(values.getOrNull(5), this::parseDate, errors,
            "Неправильный формат даты вступления '${values.getOrNull(5)}', ожидался формат: ${Parsers.DATE_PATTERN}")
        val classNum = parse(values.getOrNull(6), this::parseInt, errors,
            "Неправильный формат номера класса '${values.getOrNull(6)}', ожидалось целое число")
        val hostel = parse(values.getOrNull(7), this::parseBoolean, errors,
            "Неправильный формат пансионата '${values.getOrNull(7)}', ожидалось один из: " + Parsers.STRING_TO_BOOLEAN.keys.joinToString(", ", "[", "]"))
        val discount = parse(values.getOrNull(8), this::parseBigDecimal, errors,
            "Неправильный формат дисконта '${values.getOrNull(8)}', ожидалось дробное число")
        val price = parse(values.getOrNull(9), this::parseBigDecimal, errors,
            "Неправильный формат цены '${values.getOrNull(9)}', ожидалось дробное число")
        var currency = parse(values.getOrNull(10), this::parseString, errors,
            "Валюта не заполнена")
        val balance = parse(values.getOrNull(11), this::parseInt, errors,
            "Неправильный формат баланса '${values.getOrNull(10)}', ожидалось целое число")
        val parentId = parse(values.getOrNull(12), this::parseString, errors,
            "Телеграм аккаунт родителя не заполнен")
        val childId= parse(values.getOrNull(13), this::parseString, errors,
            "Телеграм аккаунт ученика не заполнен")
        val pause = parse(values.getOrNull(14), this::parseBoolean, errors,
            "Неправильный формат паузы '${values.getOrNull(14)}', ожидалось один из: " + Parsers.STRING_TO_BOOLEAN.keys.joinToString(", ", "[", "]"))
        val comment = parse(values.getOrNull(15), this::parseString)
        var alarm = parse(values.getOrNull(16), this::parseBoolean, errors,
        "Неправильный формат Alarm '${values.getOrNull(16)}', ожидалось один из: " + Parsers.STRING_TO_BOOLEAN.keys.joinToString(", ", "[", "]"))
        val alarmDetails = parse(values.getOrNull(17), this::parseString)
        val penalty = parse(values.getOrNull(18), this::parseBigDecimal, errors,
        "Неправильный формат пени '${values.getOrNull(18)}', ожидалось дробное число")

        if (currency != null
            && !CurrencyName.values().map { it.name }.contains(currency.uppercase())
        ) {
            errors.add("Неправильный формат валюты $currency, ожидается одно из значений: " + CurrencyName.values().joinToString(", ", "[", "]") { it.name })
            currency = null
        }
        if (currency != null && classNum != null
            && Parsers.CLASS_NUM_TO_CURRENCY_NAME(classNum).name != currency.uppercase()
        ) {
            errors.add("Неправильный формат валюты $currency согласно номеру класса, ожидается " + Parsers.CLASS_NUM_TO_CURRENCY_NAME(classNum).name)
            currency = null
        }
        currency = currency?.uppercase()

        if (googleId != null && !studentRepository.existsByGoogleId(googleId)) {
            //TODO: what if skip?
            errors.add("Нет студента с идентификатором ID = '$googleId'")
            googleId = null
        }

        if (alarm == true && alarmDetails == null) {
            errors.add("Необходимо указать ALARM_DETAILS, если выставляется флаг ALARM")
            alarm = null
        }

        if (googleId != null
            && publicId != null
            && name != null
            && birthday != null
            && dateEnrollment != null
            && classNum != null
            && hostel != null
            && discount != null
            && price != null
            && currency != null
            && balance != null
            && pause != null
            && alarm != null
            && penalty != null
        ) {
            val googleMainPageRowResponse = GoogleMainPageRowResponse(
                googleId = googleId,
                publicId = publicId,
                name = name,
                birthday = birthday,
//                age = ChronoUnit.YEARS.between(birthday, LocalDate.now()),
                dateEnrollment = dateEnrollment,
                classNum = classNum,
                hostel = hostel,
                discount = discount,
                price = price,
                currency = currency,
                balance = balance,
                parentId = parentId,
                childId = childId,
                pause = pause,
                comment = comment,
                alarm = alarm,
                alarmDetails = alarmDetails,
                penalty = penalty,
            )
            return Pair(googleMainPageRowResponse, errors)
        }
        return Pair(null, errors)
    }

}