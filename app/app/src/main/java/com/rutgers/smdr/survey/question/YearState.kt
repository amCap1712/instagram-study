package com.rutgers.smdr.survey.question

import java.util.Calendar

private val CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR)
private const val MIN_YEAR = 1900

class YearState(year: String? = null):
    TextFieldState(validator = ::isYearValid, errorFor = ::yearDateValidationError) {
    init {
        year?.let {
            text = it
        }
    }
}

/**
 * Returns an error to be displayed or null if no error was found
 */
private fun yearDateValidationError(year: String): String {
    return "Invalid year: $year"
}

fun isYearValid(year: String): Boolean {
    val number = year.toIntOrNull()
    return number != null && MIN_YEAR <= number && number <= CURRENT_YEAR
}

val YearStateSaver = textFieldStateSaver(YearState())
