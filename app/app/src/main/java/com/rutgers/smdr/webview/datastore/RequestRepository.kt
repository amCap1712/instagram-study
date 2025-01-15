package com.rutgers.smdr.webview.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import okhttp3.Headers


private const val DATA_STORE_FILE_NAME = "request.pb"

val Context.requestDataStore: DataStore<com.rutgers.smdr.datastore.Request> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = RequestSerializer
)


class RequestRepository(private val dataStore: DataStore<com.rutgers.smdr.datastore.Request>) {

    val requestFlow: Flow<com.rutgers.smdr.datastore.Request> = dataStore
        .data
        .catch { exception ->
            Log.e("PersistenceRepository", "Error reading request details:", exception)
            emit(com.rutgers.smdr.datastore.Request.getDefaultInstance())
        }

    suspend fun saveRequestDetails(url: String, method: String, headers: Headers, cookie: String, userId: String) {
        dataStore.updateData {
            it
                .toBuilder()
                .setUrl(url)
                .setMethod(method)
                .putAllHeaders(headers.toMap())
                .setCookie(cookie)
                .setUserId(userId)
                .build()
        }
    }

}
