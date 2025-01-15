package com.rutgers.smdr

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rutgers.smdr.Destinations.MAIN_ROUTE
import com.rutgers.smdr.Destinations.SURVEY_RESULTS_ROUTE
import com.rutgers.smdr.Destinations.SURVEY_ROUTE
import com.rutgers.smdr.Destinations.WELCOME_ROUTE
import com.rutgers.smdr.datastore.Request
import com.rutgers.smdr.survey.SurveyResultScreen
import com.rutgers.smdr.survey.SurveyRoute
import com.rutgers.smdr.webview.WebviewRoute
import com.rutgers.smdr.welcome.WelcomeRoute
import org.mozilla.geckoview.GeckoRuntime

object Destinations {
    const val WELCOME_ROUTE = "welcome"
    const val MAIN_ROUTE = "main"
    const val SURVEY_ROUTE = "survey"
    const val SURVEY_RESULTS_ROUTE = "surveyresults"
}

@Composable
fun DataCollectorNavHost(
    navController: NavHostController = rememberNavController(),
    requestDataStore: DataStore<Request>,
    userPreferencesRepository: UserPreferencesRepository,
    onboardingStatus: OnboardingStatus,
    viewModel: MainViewModel,
    geckoRuntime: GeckoRuntime
) {
    val startDestination = if (onboardingStatus == OnboardingStatus.SHOW) {
        WELCOME_ROUTE
    } else {
        MAIN_ROUTE
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(WELCOME_ROUTE) {
            WelcomeRoute(
                onNavigateToSurvey = {
                    navController.navigate(SURVEY_ROUTE)
                }
            )
        }

        composable(SURVEY_ROUTE) {
            SurveyRoute(
                viewModel = viewModel,
                onSurveyComplete = {
                    navController.navigate(SURVEY_RESULTS_ROUTE)
                },
                onNavUp = navController::navigateUp,
            )
        }

        composable(SURVEY_RESULTS_ROUTE) {
            SurveyResultScreen(viewModel) {
                navController.popBackStack(WELCOME_ROUTE, true)
                navController.navigate(MAIN_ROUTE)
            }
        }

        composable(MAIN_ROUTE) {
            WebviewRoute(
                dataStore = requestDataStore,
                userPreferencesRepository = userPreferencesRepository,
                geckoRuntime = geckoRuntime
            )
        }
    }
}
