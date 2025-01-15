package com.rutgers.smdr.survey

import com.rutgers.smdr.survey.question.SingleChoiceQuestion
import com.rutgers.smdr.survey.question.Option
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rutgers.smdr.R
import com.rutgers.smdr.survey.question.EmailQuestion
import com.rutgers.smdr.survey.question.YearDateQuestion


@Composable
fun PoliticsQuestion(
    selectedAnswer: Option?,
    onOptionSelected: (Option) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceQuestion(
        titleResourceId = R.string.choose_politics,
        directionsResourceId = R.string.select_one,
        possibleAnswers = listOf(
            Option(R.string.option_very_liberal),
            Option(R.string.option_liberal),
            Option(R.string.option_moderate),
            Option(R.string.option_conservative),
            Option(R.string.option_very_conservative),
            Option(R.string.option_libertarian),
            Option(R.string.option_progressive),
            Option(R.string.option_socialist),
            Option(R.string.option_centrist),
            Option(R.string.option_apolitical)
        ),
        selectedAnswer = selectedAnswer,
        onOptionSelected = onOptionSelected,
        modifier = modifier,
    )
}

@Composable
fun EthnicityQuestion(
    selectedAnswer: Option?,
    onOptionSelected: (Option) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceQuestion(
        titleResourceId = R.string.choose_ethnicity,
        directionsResourceId = R.string.select_one,
        possibleAnswers = listOf(
            Option(R.string.black),
            Option(R.string.white),
            Option(R.string.hispanic),
            Option(R.string.asian),
            Option(R.string.american_indian),
            Option(R.string.native_hawaii),
            Option(R.string.north_african),
            Option(R.string.not_disclose),
        ),
        selectedAnswer = selectedAnswer,
        onOptionSelected = onOptionSelected,
        modifier = modifier,
    )
}

@Composable
fun GenderIdentityQuestion(
    selectedAnswer: Option?,
    onOptionSelected: (Option) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceQuestion(
        titleResourceId = R.string.choose_gender,
        directionsResourceId = R.string.select_one,
        possibleAnswers = listOf(
            Option(R.string.man),
            Option(R.string.woman),
            Option(R.string.non_binary),
            Option(R.string.not_disclose),
        ),
        selectedAnswer = selectedAnswer,
        onOptionSelected = onOptionSelected,
        modifier = modifier,
    )
}

@Composable
fun BirthYearQuestion(
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    YearDateQuestion(
        titleResourceId = R.string.choose_birthyear,
        directionsResourceId = R.string.select_date,
        onTextChange = onTextChange,
        modifier = modifier,
    )
}

@Composable
fun CanContactQuestion(
    selectedAnswer: Option?,
    onOptionSelected: (Option) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceQuestion(
        titleResourceId = R.string.choose_whether_to_contact,
        directionsResourceId = R.string.select_one,
        possibleAnswers = listOf(
            Option(R.string.yes),
            Option(R.string.no),
        ),
        selectedAnswer = selectedAnswer,
        onOptionSelected = onOptionSelected,
        modifier = modifier,
    )
}

@Composable
fun EmailContactQuestion(
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    EmailQuestion(
        titleResourceId = R.string.choose_email_to_contact,
        directionsResourceId = R.string.select_email,
        onTextChange = onTextChange,
        modifier = modifier,
    )
}

