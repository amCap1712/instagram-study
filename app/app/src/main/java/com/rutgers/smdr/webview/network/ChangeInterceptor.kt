package com.rutgers.smdr.webview.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


internal class ChangeResponse : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse
            .newBuilder()
            .removeHeader("Content-Security-Policy")
            .build()
    }
}
