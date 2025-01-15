package com.rutgers.smdr.webview.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class RetryInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        var tryCount = 0
        while (!response.isSuccessful && tryCount < 3) {
            Timber.tag("RetryInterceptor").d("Request unsuccessful - %s", tryCount)
            tryCount += 1
            response.close()
            response = chain.proceed(request)
        }

        return response;
    }
}