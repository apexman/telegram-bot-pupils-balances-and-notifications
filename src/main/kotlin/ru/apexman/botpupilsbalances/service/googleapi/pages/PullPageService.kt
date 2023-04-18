package ru.apexman.botpupilsbalances.service.googleapi.pages

import com.google.api.services.sheets.v4.Sheets
import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.constants.Parsers
import ru.apexman.botpupilsbalances.dto.GooglePullPageRowResponse
import ru.apexman.botpupilsbalances.dto.OperationResult
import ru.apexman.botpupilsbalances.service.googleapi.SheetsProperties

@Service
class PullPageService(
    private val sheetsProperties: SheetsProperties,
    private val sheets: Sheets,
): AbstractSheetsService() {

    @Synchronized
    fun readPullPage(): OperationResult<Collection<GooglePullPageRowResponse>> {
        val result = mutableListOf<GooglePullPageRowResponse>()
        val errors = mutableListOf<String>()
        val rowsNumber = rowsCountAtRequest
        var startRowNumber = 2
        var hasMoreData = true
        while (hasMoreData) {
            val endRowNumber = startRowNumber + rowsNumber
            val response = sheets.spreadsheets()
                .values()
                .batchGet(sheetsProperties.sheetId)
                .setRanges(listOf("${sheetsProperties.pullTableName}!A$startRowNumber:F$endRowNumber"))
                .setValueRenderOption("UNFORMATTED_VALUE")
                .setDateTimeRenderOption("SERIAL_NUMBER")
                .execute()
            val values = getTableValues(response.values)
            values.forEachIndexed { index, value ->
                val (googlePullPageRow, rowErrors) = parsePullPageRow(value)
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

    //TODO: mapping by column names
    /**
     * Ожидаемый порядок колонок:
     * - NAME
     * - BIRTHDAY
     * - DATE_ENROLLMENT
     * - CLASS
     * - HOSTEL
     * - DISCONT
     */
    private fun parsePullPageRow(values: List<Any>): Pair<GooglePullPageRowResponse?, Collection<String>> {
        val errors = mutableListOf<String>()
        val name = parse(values.getOrNull(0), this::parseString, errors,
            "Имя не должно быть пустым")
        val birthday = parse(values.getOrNull(1), this::parseDate, errors,
            "Неправильный формат даты дня рождения '${values.getOrNull(1)}', ожидался формат: ${Parsers.DATE_PATTERN}")
        val dateEnrollment = parse(values.getOrNull(2), this::parseDate, errors,
            "Неправильный формат даты вступления '${values.getOrNull(2)}', ожидался формат: ${Parsers.DATE_PATTERN}")
        val classNum = parse(values.getOrNull(3), this::parseInt, errors,
            "Неправильный формат номера класса '${values.getOrNull(3)}', ожидалось целое число")
        val hostel = parse(values.getOrNull(4), this::parseBoolean, errors,
            "Неправильный формат пансионата '${values.getOrNull(4)}', ожидалось один из: " + Parsers.STRING_TO_BOOLEAN.keys.joinToString(", ", "[", "]"))
        val discount = parse(values.getOrNull(5), this::parseBigDecimal, errors,
            "Неправильный формат дисконта '${values.getOrNull(5)}', ожидалось дробное число")
        if (name != null
            && birthday != null
            && dateEnrollment != null
            && classNum != null
            && hostel != null
            && discount != null
        ) {
            val googlePullPageRowResponse = GooglePullPageRowResponse(
                name = name,
                birthday = birthday,
                dateEnrollment = dateEnrollment,
                classNum = classNum,
                hostel = hostel,
                discount = discount,
            )
            return Pair(googlePullPageRowResponse, errors)
        }
        return Pair(null, errors)
    }

}