package ru.apexman.botpupilsbalances.constants

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Parsers {
    companion object {
        const val DATE_PATTERN = "dd.MM.yyyy"

        val STRING_TO_BOOLEAN_KEYS = mapOf(
            Pair("да", true),
            Pair("1", true),
            Pair("нет", false),
            Pair("0", false),
            Pair("yes", true),
            Pair("no", false),
        )

        val STRING_TO_BOOLEAN = {x: String? ->
            STRING_TO_BOOLEAN_KEYS[x?.lowercase()?.trim()]
        }

        val CLASS_NUM_TO_CURRENCY_NAME = { x: Int ->
            when (x) {
                0 -> CurrencyName.IDR
                else -> CurrencyName.USD
            }
        }

        val BOOLEAN_TO_STRING = { x: Boolean ->
            when (x) {
                true -> "Да"
                else -> "Нет"
            }
        }

        val LOCAL_DATE_TO_STRING = { x: LocalDate ->
            x.format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        }
    }
}