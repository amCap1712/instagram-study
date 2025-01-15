package com.rutgers.smdr.webview.client

import android.content.Context
import android.util.Log
import android.webkit.*
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.usage.UsageWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Headers.Companion.toHeaders
import com.rutgers.smdr.webview.datastore.RequestRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class LoggingWebClient(
    private val coroutineScope: CoroutineScope,
    private val context: Context,
    private val repository: RequestRepository,
    private val script: String,
    private val networkClient: NetworkClient
): WebViewClient() {

    private var processed = false
    private var cookie: String? = null
    private var userId: String? = null

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (request != null) {
            Log.i("LoggingWebClient", "Url: ${request.url}")
        }
        val apiPath = "/api/v1/feed/timeline/"
        if (apiPath == request?.url?.encodedPath) {
            if (cookie != null && !processed) {
                processed = true
                coroutineScope.launch {
                    process(request)
                }
            } else {
                Log.i("LoggingWebClient", "Cookie not found. cannot make requests")
            }
        }
        if (request?.url?.host == "www.instagram.com") {
            val path = request.url?.path ?: ""
            if (!path.contains("ajax") && !path.contains("api") && !path.contains("graphql") && !path.contains("instagram")) {
                Log.i("LoggingWebClient", "Matched host loading: ${request.url} and ${path}")
                var webResourceResponse: WebResourceResponse?
                runBlocking {
                    val response = networkClient.loadInitialWebpageWithoutCSP(request.url.toString(), request.requestHeaders.toHeaders())
                    val body = response.body?.bytes() ?: ByteArray(0)
                    webResourceResponse = WebResourceResponse("text/html", "utf-8", 200, "OK", response.headers.toMap(), ByteArrayInputStream(body))

                    Log.i("NetworkClient", "Response:")

                    val oldFile = File(context.getExternalFilesDir(null), "instagram.html")
                    val oldFos = FileOutputStream(oldFile)
                    oldFos.write(body)
                    oldFos.flush()
                    oldFos.close()
                }
                return webResourceResponse
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.run {
            evaluateJavascript(script, null)
        }
        super.onPageFinished(view, url)
        cookie = CookieManager.getInstance().getCookie(url)
        if (cookie != null) {
            Log.d("LoggingWebClient", "Cookie: $cookie")
            userId = cookie!!
                .split("; ")
                .associate {
                    val pair = it.split("=")
                    Pair(pair[0], pair[1])
                }["ds_user_id"]
        }
    }

    private suspend fun process(request: WebResourceRequest) {
        val url = request.url.toString()
        val method = request.method
        val headers = request.requestHeaders.toHeaders()

        repository.saveRequestDetails(url, method, headers, cookie!!, userId!!)

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                "snapshotFeed",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<BrowsingWorker>(30, TimeUnit.MINUTES)
                    .setConstraints(Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    )
                    .build()
            )

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                "recordUsage",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UsageWorker>(30, TimeUnit.MINUTES)
                    .setConstraints(Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    )
                    .build()
            )
    }

}
