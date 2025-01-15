package com.rutgers.smdr.webview.client

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import okhttp3.Headers.Companion.toHeaders
import com.rutgers.smdr.webview.datastore.RequestRepository
import com.rutgers.smdr.webview.datastore.requestDataStore
import com.rutgers.smdr.webview.network.CollectionType
import com.rutgers.smdr.webview.network.CollectionSource
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.UserPreferencesRepository
import com.rutgers.smdr.preferencesDataStore
import com.rutgers.smdr.webview.network.RetrievalService
import com.rutgers.smdr.webview.network.StorageService

class BrowsingWorker(private val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repository = RequestRepository(context.requestDataStore)
        val networkClient = NetworkClient()
        val retrieval = RetrievalService(networkClient)
        val userPreferencesRepository = UserPreferencesRepository(context.preferencesDataStore)
        val storage = StorageService(networkClient, userPreferencesRepository)

        val request = repository.requestFlow.first()
        val feed = retrieval.retrieveData(
            request.url,
            request.method,
            request.headersMap.toHeaders(),
            request.cookie
        )
        storage.submitItems(CollectionSource.FEED, CollectionType.PASSIVE, request.userId, feed)

        return Result.success()
    }

}

