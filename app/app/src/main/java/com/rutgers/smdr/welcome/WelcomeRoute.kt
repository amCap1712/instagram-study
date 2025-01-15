package com.rutgers.smdr.welcome

import WelcomeScreen
import androidx.compose.runtime.Composable

@Composable
fun WelcomeRoute(onNavigateToSurvey: () -> Unit) {
    WelcomeScreen(onNavigateToSurvey = onNavigateToSurvey)
}
