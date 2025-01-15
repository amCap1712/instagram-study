package com.rutgers.smdr.survey.question

import androidx.annotation.StringRes
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.rutgers.smdr.R

@Composable
fun YearDateQuestion(
    @StringRes titleResourceId: Int,
    @StringRes directionsResourceId: Int,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val yearState by rememberSaveable(stateSaver = YearStateSaver) {
        mutableStateOf(YearState())
    }
    QuestionWrapper(
        titleResourceId = titleResourceId,
        directionsResourceId = directionsResourceId,
        modifier = modifier.selectableGroup(),
    ) {
        CustomTextField(
            state = yearState,
            onStateChange = onTextChange,
            labelStringResourceId = R.string.year,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        )
    }
}
