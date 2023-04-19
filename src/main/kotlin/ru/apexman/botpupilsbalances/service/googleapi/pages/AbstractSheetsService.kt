package ru.apexman.botpupilsbalances.service.googleapi.pages

import com.google.api.services.sheets.v4.model.ValueRange
import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.constants.Parsers
import java.math.BigDecimal
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
abstract class AbstractSheetsService {

    val rowsCountAtRequest = 1000

    protected fun getTableValues(values: MutableCollection<Any>): List<List<Any>> {
        //спускаемся до таблицы со значениями
        for (rawValue in values) {
            if (rawValue is ArrayList<*>) {
                val valueRange = rawValue.getOrNull(0)
                if (valueRange is ValueRange) {
                    @Suppress("UNCHECKED_CAST")
                    return valueRange.getValue("values") as List<List<Any>>
                }
            }
        }
        return listOf(listOf())
    }

    protected fun <T> parse(
        someString: Any?,
        kFunction: (Any?) -> T?,
        errors: MutableList<String>? = null,
        errorMessage: String? = null,
    ): T? {
        val parsed = kFunction.invoke(someString)
        if (parsed != null) {
            return parsed
        }
        if (errors != null && errorMessage != null) {
            errors.add(errorMessage)
        }
        return null
    }

    protected fun parseString(someString: Any?): String? {
        return when (someString) {
                is String  -> someString.ifBlank { null }
                is BigDecimal -> someString.toPlainString()
                else -> someString?.toString()?.ifBlank { null }
            }
    }

    protected fun parseDate(date: Any?): LocalDate? {
        return try {
            when (date) {
                is String -> LocalDate.parse(date, DateTimeFormatter.ofPattern(Parsers.DATE_PATTERN))
                is BigDecimal -> LocalDate.of(1899, 12, 30).plusDays(date.toLong())
                else -> null
            }
        } catch (e: DateTimeException) {
            null
        }
    }

    protected fun parseInt(someString: Any?): Int? {
        return try {
            when (someString) {
                is String -> someString.toIntOrNull()
                is BigDecimal -> someString.toBigInteger().intValueExact()
                else -> null
            }
        } catch (e: ArithmeticException) {
            //omit
            null
        }
    }

    protected fun parseBoolean(someString: Any?): Boolean? {
        return when (someString) {
            is String -> Parsers.STRING_TO_BOOLEAN(someString)
            is BigDecimal -> Parsers.STRING_TO_BOOLEAN(someString.stripTrailingZeros().toPlainString())
            else -> null
        }
    }

    protected fun parseBigDecimal(someString: Any?): BigDecimal? {
        return when (someString) {
            is String -> someString.toBigDecimalOrNull()
            is BigDecimal -> someString
            else -> null
        }
    }

}