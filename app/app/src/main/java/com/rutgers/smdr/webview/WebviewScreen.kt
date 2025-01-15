package com.rutgers.smdr.webview

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rutgers.smdr.UserPreferencesRepository
import com.rutgers.smdr.usage.UsageWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtension.MessageDelegate
import org.mozilla.geckoview.WebExtension.PortDelegate
import timber.log.Timber
import java.util.concurrent.TimeUnit


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebviewScreen(
    dataStore: DataStore<com.rutgers.smdr.datastore.Request>,
    userPreferencesRepository: UserPreferencesRepository,
    geckoRuntime: GeckoRuntime
) {
    val url = "https://www.instagram.com/"
    val context = LocalContext.current
    val geckoView = GeckoView(context)
    val geckoSession = GeckoSession()

    var mPort: WebExtension.Port? = null
    val portDelegate: PortDelegate = object : PortDelegate {

        override fun onPortMessage(message: Any, port: WebExtension.Port) {
            Timber.tag("PortDelegate").i("Received message from WebExtension: %s", message)
        }

        override fun onDisconnect(port: WebExtension.Port) {
            if (port === mPort) {
                mPort = null
            }
        }
    }

    var surveyUserId: Int? = null
    runBlocking {
        surveyUserId = userPreferencesRepository
            .userPreferencesFlow
            .map { it.surveyUserId }
            .first()
    }

    val messageDelegate = object : MessageDelegate {
        override fun onConnect(port: WebExtension.Port) {
            mPort = port
            mPort!!.setDelegate(portDelegate)

            val message = JSONObject()
            try {
                message.put("survey_user_id", surveyUserId)
            } catch (ex: JSONException) {
                throw RuntimeException(ex)
            }
            mPort!!.postMessage(message)
            Timber.tag("GeckoNative").i("Sent: %s", message)
        }

        override fun onMessage(
            nativeApp: String,
            message: Any,
            sender: WebExtension.MessageSender
        ): GeckoResult<Any>? {
            if (message is JSONObject) {
                try {
                    if (message.has("type") && "WPAManifest" == message.getString("type")) {
                        val manifest: JSONObject = message.getJSONObject("manifest")
                        Timber.tag("MessageDelegate").d("Found WPA manifest: %s", manifest)
                    }
                } catch (ex: JSONException) {
                    Timber.tag("MessageDelegate").e(ex, "Invalid message:")
                }
            }
            return null
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(factory = {
            geckoRuntime.webExtensionController
                .ensureBuiltIn("resource://android/assets/interceptor/", "kartikohri13@gmail.com")
                .accept(
                    { extension ->  run {
                        extension!!.setMessageDelegate(
                            messageDelegate,
                            "browser"
                        )
                        Timber.tag("MessageDelegate").i("Extension installed: %s", extension)
                    }},
                    { e -> Timber.tag("MessageDelegate").e(e, "Error registering WebExtension") }
                )
            geckoSession.open(geckoRuntime)
            geckoView.setSession(geckoSession)
            geckoSession.loadUri(url)
            geckoView
        }, update = {
            geckoSession.loadUri(url)
        })
    }
}
