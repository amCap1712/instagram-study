package com.rutgers.smdr.survey

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.rutgers.smdr.MainViewModel
import com.rutgers.smdr.R
import com.rutgers.smdr.SurveyQuestion
import com.rutgers.smdr.survey.question.ProlificIdQuestion

private const val CONTENT_ANIMATION_DURATION = 300


@Composable
fun SurveyRoute(
    viewModel: MainViewModel,
    onSurveyComplete: () -> Unit,
    onNavUp: () -> Unit,
) {
    val surveyScreenData = viewModel.surveyScreenData

    BackHandler {
        if (!viewModel.onBackPressed()) {
            onNavUp()
        }
    }

    SurveyQuestionsScreen(
        surveyScreenData = surveyScreenData,
        isNextEnabled = viewModel.isNextEnabled,
        onClosePressed = { onNavUp() },
        onPreviousPressed = { viewModel.onPreviousPressed() },
        onNextPressed = { viewModel.onNextPressed(onSurveyComplete) },
    ) { paddingValues ->

        val modifier = Modifier.padding(paddingValues)

        AnimatedContent(
            targetState = surveyScreenData,
            transitionSpec = {
                val animationSpec: TweenSpec<IntOffset> = tween(CONTENT_ANIMATION_DURATION)

                val direction = getTransitionDirection(
                    initialIndex = initialState.questionIndex,
                    targetIndex = targetState.questionIndex,
                )

                slideIntoContainer(
                    towards = direction,
                    animationSpec = animationSpec,
                ) togetherWith slideOutOfContainer(
                    towards = direction,
                    animationSpec = animationSpec
                )
            },
            label = "surveyScreenDataAnimation"
        ) { targetState ->

            when (targetState.surveyQuestion) {
                SurveyQuestion.PROLIFIC_ID ->
                    ProlificIdQuestion(
                        titleResourceId = R.string.enter_prolific_id,
                        directionsResourceId = R.string.description_prolific_id,
                        onTextChange = viewModel::onProlificIdResponse,
                        modifier = modifier,
                    )

                SurveyQuestion.POLITICS ->
                    PoliticsQuestion(
                        selectedAnswer = viewModel.politicsResponse,
                        onOptionSelected = viewModel::onPoliticsResponse,
                        modifier = modifier,
                    )

                SurveyQuestion.ETHNICITY ->
                    EthnicityQuestion(
                        selectedAnswer = viewModel.ethnicityResponse,
                        onOptionSelected = viewModel::onEthnicityResponse,
                        modifier = modifier,
                    )

                SurveyQuestion.GENDER_IDENTITY -> {
                    GenderIdentityQuestion(
                        selectedAnswer = viewModel.genderIdentityResponse,
                        onOptionSelected = viewModel::onGenderIdentityResponse,
                        modifier = modifier,
                    )
                }

                SurveyQuestion.BIRTHYEAR ->
                    BirthYearQuestion(
                        onTextChange = viewModel::onBirthYearResponse,
                        modifier = modifier
                    )

                SurveyQuestion.CONTACT -> {
                    CanContactQuestion(
                        selectedAnswer = viewModel.canContactResponse,
                        onOptionSelected = viewModel::onCanContactResponse,
                        modifier = modifier,
                    )
                }

                SurveyQuestion.EMAIL -> {
                    EmailContactQuestion(
                        onTextChange = viewModel::onEmailResponse,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

private fun getTransitionDirection(
    initialIndex: Int,
    targetIndex: Int
): AnimatedContentTransitionScope.SlideDirection {
    return if (targetIndex > initialIndex) {
        // Going forwards in the survey: Set the initial offset to start
        // at the size of the content so it slides in from right to left, and
        // slides out from the left of the screen to -fullWidth
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        // Going back to the previous question in the set, we do the same
        // transition as above, but with different offsets - the inverse of
        // above, negative fullWidth to enter, and fullWidth to exit.
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}
