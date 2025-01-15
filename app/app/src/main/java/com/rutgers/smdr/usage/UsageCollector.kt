package com.rutgers.smdr.usage

import android.Manifest
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.R
import com.rutgers.smdr.UserPreferences
import com.rutgers.smdr.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.ThreadLocalRandom


const val MILLIS_THRESHOLD = 60 * 60 * 1000
const val USAGE_MILLIS_THRESHOLD = 15 * 60 * 1000

class UsageCollector(private val userPreferencesRepository: UserPreferencesRepository, private val networkClient: NetworkClient) {

    private val INSTAGRAM_PACKAGE_ID = "com.instagram.android"
    private val mediaType = "application/json".toMediaType()
    private val CHANNEL_ID = "org.rutgers.social_media_data_research"

    private val SMDR_PACKAGE_ID = "com.rutgers.smdr"

    suspend fun recordInstagramUsage(context: Context): Boolean {
        try {
            if (!checkPermission(context)) {
                Timber.tag("UsageCollector").i("Missing required permissions")
                return false
            }

            val preferences = userPreferencesRepository.userPreferencesFlow.first()
            val endTime = System.currentTimeMillis()
            val lastTimeUsed = getLastTimeUsed(context, preferences, endTime)

            if ((lastTimeUsed != null)) {
                submitInstagramUsage(preferences.surveyUserId, lastTimeUsed)
                if ((endTime - lastTimeUsed) <= MILLIS_THRESHOLD) {
                    showNotification(context)
                }
            }
            userPreferencesRepository.updateUsageCollectionTime(endTime)
        } catch (e: SecurityException) {
            Timber.tag("UsageCollector").e(e)
        }
        return true
    }

    private fun getLastTimeUsed(
        context: Context,
        preferences: UserPreferences,
        endTime: Long
    ): Long? {
        val service = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val startTime = preferences.usageCollectionTime ?: System.currentTimeMillis()
        val stats = service.queryAndAggregateUsageStats(startTime, endTime)
        return stats[INSTAGRAM_PACKAGE_ID]?.lastTimeUsed
    }

    private suspend fun submitInstagramUsage(surveyUserId: Int?, lastTimeUsed: Long?) {
        val body = mutableMapOf(
            "survey_user_id" to surveyUserId,
            "usage_time" to lastTimeUsed,
        )

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("kiran-research2.comminfo.rutgers.edu")
            .addPathSegment("data-collector-admin")
            .addPathSegment("usage")
            .build()

        val requestBody = networkClient.encode(body).toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val response = networkClient.execute(request)
    }

    private fun showNotification(context: Context) {
        createNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(ThreadLocalRandom.current().nextInt(), builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            val secondaryCheck = context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS)
            secondaryCheck == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    suspend fun getCurrentAppUsage(context: Context): Long {
        Timber.tag("UsageCollector").i("Called getCurrentAppUsage")
        val surveyCompleted = userPreferencesRepository.userPreferencesFlow.map { p -> p.surveyComplete }.first()
        if (!surveyCompleted) {
            Timber.tag("UsageCollector").i("Survey not completed yet, not counting usage")
            return 0
        }
        val service = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000
        val stats = service.queryAndAggregateUsageStats(startTime, endTime)
        Timber.tag("UsageCollector").i("Stats: %s", stats[SMDR_PACKAGE_ID]?.totalTimeInForeground)
        return stats[SMDR_PACKAGE_ID]?.totalTimeInForeground ?: 0
    }
}
