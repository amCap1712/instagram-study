package com.rutgers.smdr.survey.question

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType


@Composable
fun CustomTextField(
    state: TextFieldState,
    onStateChange: (String) -> Unit,
    @StringRes labelStringResourceId: Int,
    keyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = state.text,
        onValueChange = {
            state.text = it
            onStateChange(it)
            state.enableShowErrors()
        },
        label = {
            Text(
                text = stringResource(id = labelStringResourceId),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                state.onFocusChange(focusState.isFocused)
                if (!focusState.isFocused) {
                    state.enableShowErrors()
                }
            },
        textStyle = MaterialTheme.typography.bodyMedium,
        isError = state.showErrors(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = imeAction,
            keyboardType = keyboardType
        )
    )

    state.getError()?.let { error -> TextFieldError(textError = error) }
}
