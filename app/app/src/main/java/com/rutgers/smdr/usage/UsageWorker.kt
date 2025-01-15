package com.rutgers.smdr.usage;

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.UserPreferencesRepository
import com.rutgers.smdr.preferencesDataStore


class UsageWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val networkClient = NetworkClient()
        val userPreferencesRepository = UserPreferencesRepository(context.preferencesDataStore)

        val collector = UsageCollector(userPreferencesRepository, networkClient)
        return try {
            return if(collector.recordInstagramUsage(context)) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

}
