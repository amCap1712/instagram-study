package com.rutgers.smdr

import android.util.Log
import android.webkit.CookieManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import com.rutgers.smdr.webview.network.RetryInterceptor
import okhttp3.Headers
import okhttp3.Response
import java.util.concurrent.TimeUnit

class NetworkClient {

    private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor()
    private val client: OkHttpClient

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val type = Types.newParameterizedType(
        MutableMap::class.java,
        String::class.java,
        Any::class.java
    )
    private val adapter: JsonAdapter<Map<String, Any?>> = moshi.adapter<Map<String, Any?>?>(type).serializeNulls()

    init {
        logging.level = HttpLoggingInterceptor.Level.HEADERS
        client = OkHttpClient()
            .newBuilder()
            .addInterceptor(RetryInterceptor())
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    suspend fun execute(request: Request): Map<String, Any?>? {
        return withContext(Dispatchers.IO) {
            client
                .newCall(request)
                .execute()
                .use {
                    if (!it.isSuccessful) {
                        val message = "Error received during HTTP Requests. Response Code: ${it.code}, Body: ${it.body?.string()}"
                        Log.e("RetrievalService", message)
                        FirebaseCrashlytics.getInstance().log(message)
                        return@withContext null
                    }
                    val body = it.body?.source()
                    return@withContext adapter.fromJson(body!!)!!
                }
        }
    }

    suspend fun loadInitialWebpageWithoutCSP(url: String, headers: Headers): Response {
        return withContext(Dispatchers.IO) {
            val cookieManager = CookieManager.getInstance()
            val cookie = cookieManager.getCookie(url)
            var request = Request
                .Builder()
                .url(url)
                .headers(headers)
            if (cookie != null) {
                request = request.header("Cookie", cookie)
            }
            val response = client.newCall(request.build()).execute()
            response
                .headers("Set-Cookie")
                .forEach {
                    Log.i("NetworkClient", "Setting cookie: $it")
                    cookieManager.setCookie(url, it)
                }

            val newResponse = response.newBuilder()
                .removeHeader("content-security-policy")
                .removeHeader("content-security-policy-report-only")
                .removeHeader("cross-origin-embedder-policy-report-only")
                .removeHeader("cross-origin-opener-policy")
                .removeHeader("cross-origin-resource-policy")
                .removeHeader("document-policy")
                .removeHeader("permissions-policy")
                .removeHeader("x-frame-options")
                .removeHeader("x-xss-protection")
                .build()
            Log.i("NetworkClient", "New Headers")
            for (header in newResponse.headers) {
                Log.i("NetworkClient", header.first + ": " + header.second)
            }
            return@withContext newResponse
        }
    }

    fun encode(response: Map<String, Any?>): String = adapter.toJson(response)

    fun decode(response: String): Map<String, Any?>? = adapter.fromJson(response)

}