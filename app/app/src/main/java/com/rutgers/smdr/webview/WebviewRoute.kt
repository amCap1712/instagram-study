package com.rutgers.smdr.webview

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.UserPreferencesRepository
import org.mozilla.geckoview.GeckoRuntime

@Composable
fun WebviewRoute(
    dataStore: DataStore<com.rutgers.smdr.datastore.Request>,
    userPreferencesRepository: UserPreferencesRepository,
    geckoRuntime: GeckoRuntime
) {
    WebviewScreen(
        dataStore = dataStore,
        userPreferencesRepository = userPreferencesRepository,
        geckoRuntime = geckoRuntime
    )
}