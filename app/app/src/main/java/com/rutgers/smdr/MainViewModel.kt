package com.rutgers.smdr

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.rutgers.smdr.survey.question.Option
import com.rutgers.smdr.survey.question.getOptionValue
import com.rutgers.smdr.survey.question.isEmailValid


enum class OnboardingStatus {
    LOADING,
    SHOW,
    HIDE
}

class MainViewModel(
    private val application: Application,
    private val networkClient: NetworkClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    ) : AndroidViewModel(application) {

    private val mediaType = "application/json".toMediaType()

    fun onboardingFlow(): StateFlow<OnboardingStatus> =
        userPreferencesRepository
            .userPreferencesFlow
            .map { userPreferences ->
                if (userPreferences.surveyComplete) {
                    OnboardingStatus.HIDE
                } else {
                    OnboardingStatus.SHOW
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = OnboardingStatus.LOADING,
            )

    suspend fun completeOnboarding() {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("kiran-research2.comminfo.rutgers.edu")
            .addPathSegment("data-collector-admin")
            .addPathSegment("survey")
            .build()

        val body = mapOf(
            "prolific_id" to prolificIdResponse,
            "politics" to application.getOptionValue(politicsResponse!!),
            "ethnicity" to application.getOptionValue(ethnicityResponse!!),
            "gender_identity" to application.getOptionValue(genderIdentityResponse!!),
            "birth_year" to birthYearResponse?.toInt(),
            "can_contact" to application.getOptionValue(canContactResponse!!),
            "email" to emailResponse
        )
        val requestBody = networkClient.encode(body).toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val response = networkClient.execute(request)
        val surveyUserId = (response?.get("id") as Double).toInt()
        userPreferencesRepository.updateShowCompleted(true, surveyUserId)
    }

    private val questionOrder: List<SurveyQuestion> = listOf(
        SurveyQuestion.PROLIFIC_ID,
        SurveyQuestion.POLITICS,
        SurveyQuestion.ETHNICITY,
        SurveyQuestion.GENDER_IDENTITY,
        SurveyQuestion.BIRTHYEAR,
        SurveyQuestion.CONTACT,
        SurveyQuestion.EMAIL,
    )

    private var questionIndex = 0

    private val _politicsResponse = mutableStateOf<Option?>(null)
    val politicsResponse: Option?
        get() = _politicsResponse.value

    private val _ethnicityResponse = mutableStateOf<Option?>(null)
    val ethnicityResponse: Option?
        get() = _ethnicityResponse.value

    private val _genderIdentityResponse = mutableStateOf<Option?>(null)
    val genderIdentityResponse: Option?
        get() = _genderIdentityResponse.value

    private val _birthYearResponse = mutableStateOf<String?>(null)
    private val birthYearResponse: String?
        get() = _birthYearResponse.value

    private val _canContactResponse = mutableStateOf<Option?>(null)
    val canContactResponse: Option?
        get() = _canContactResponse.value

    private val _emailResponse = mutableStateOf<String?>(null)
    private val emailResponse: String?
        get() = _emailResponse.value

    private val _prolificIdResponse = mutableStateOf<String?>(null)
    private val prolificIdResponse: String?
        get() = _prolificIdResponse.value

    private val _surveyScreenData = mutableStateOf(createSurveyScreenData())
    val surveyScreenData: SurveyScreenData
        get() = _surveyScreenData.value

    private val _isNextEnabled = mutableStateOf(false)
    val isNextEnabled: Boolean
        get() = _isNextEnabled.value

    /**
     * Returns true if the ViewModel handled the back press (i.e., it went back one question)
     */
    fun onBackPressed(): Boolean {
        if (questionIndex == 0) {
            return false
        }
        changeQuestion(questionIndex - 1)
        return true
    }

    fun onPreviousPressed() {
        if (questionIndex == 0) {
            throw IllegalStateException("onPreviousPressed when on question 0")
        }
        changeQuestion(questionIndex - 1)
    }

    fun onNextPressed(onSurveyComplete: () -> Unit) {
        if ((questionIndex == questionOrder.size - 2 && _canContactResponse.value?.stringResourceId == R.string.no) || questionIndex == questionOrder.size - 1) {
            onSurveyComplete()
        } else {
            changeQuestion(questionIndex + 1)
        }
    }

    private fun changeQuestion(newQuestionIndex: Int) {
        questionIndex = newQuestionIndex
        _isNextEnabled.value = getIsNextEnabled()
        _surveyScreenData.value = createSurveyScreenData()
    }

    fun onProlificIdResponse(value: String) {
        _prolificIdResponse.value = value
        _isNextEnabled.value = getIsNextEnabled()
    }

    fun onPoliticsResponse(option: Option) {
        _politicsResponse.value = option
        _isNextEnabled.value = getIsNextEnabled()
    }

    fun onEthnicityResponse(option: Option) {
        _ethnicityResponse.value = option
        _isNextEnabled.value = getIsNextEnabled()
    }

    fun onGenderIdentityResponse(option: Option) {
        _genderIdentityResponse.value = option
        _isNextEnabled.value = getIsNextEnabled()
    }


    fun onBirthYearResponse(value: String) {
        _birthYearResponse.value = value
        _isNextEnabled.value = getIsNextEnabled()
    }

    fun onCanContactResponse(option: Option) {
        _canContactResponse.value = option
        _isNextEnabled.value = getIsNextEnabled()
    }

    fun onEmailResponse(value: String) {
        _emailResponse.value = value
        _isNextEnabled.value = getIsNextEnabled()
    }

    private fun getIsNextEnabled(): Boolean {
        return when (questionOrder[questionIndex]) {
            SurveyQuestion.PROLIFIC_ID -> _prolificIdResponse.value != null
            SurveyQuestion.POLITICS -> _politicsResponse.value != null
            SurveyQuestion.ETHNICITY -> _ethnicityResponse.value != null
            SurveyQuestion.GENDER_IDENTITY -> _genderIdentityResponse.value != null
            SurveyQuestion.BIRTHYEAR -> _birthYearResponse.value != null
            SurveyQuestion.CONTACT -> _canContactResponse.value != null
            SurveyQuestion.EMAIL -> _emailResponse.value != null && isEmailValid(_emailResponse.value!!)
        }
    }

    private fun createSurveyScreenData(): SurveyScreenData {
        return SurveyScreenData(
            questionIndex = questionIndex,
            questionCount = questionOrder.size,
            shouldShowPreviousButton = questionIndex > 0,
            surveyQuestion = questionOrder[questionIndex],
        )
    }

}

class MainViewModelFactory(
    private val application: Application,
    private val networkClient: NetworkClient,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return MainViewModel(
            application,
            networkClient,
            userPreferencesRepository
        ) as T
    }
}

enum class SurveyQuestion {
    PROLIFIC_ID,
    POLITICS,
    ETHNICITY,
    GENDER_IDENTITY,
    BIRTHYEAR,
    CONTACT,
    EMAIL
}

data class SurveyScreenData(
    val questionIndex: Int,
    val questionCount: Int,
    val shouldShowPreviousButton: Boolean,
    val surveyQuestion: SurveyQuestion,
)
