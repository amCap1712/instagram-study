package com.rutgers.smdr.webview.network

import android.util.Log
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber


enum class CollectionType(val value: String) {
    ACTIVE("active"),
    PASSIVE("passive")
}

enum class CollectionSource(val value: String) {
    FEED("feed"),
    REELS("reels"),
    EXPLORE("explore")
}

class StorageService(
    private val networkClient: NetworkClient,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val mediaType = "application/json".toMediaType()

    suspend fun submitItems(
        source: CollectionSource,
        type: CollectionType,
        userId: String,
        itemsToSubmit: Collection<Map<String, Any>>,
        collectionId: String? = null,
        startRank: Int = 0,
    ): String? {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("kiran-research2.comminfo.rutgers.edu")
            .addPathSegment("data-collector-admin")
            .addPathSegment(source.value)
            .build()

        val items = mutableListOf<MutableMap<String, Any>>()
        for ((index, item) in itemsToSubmit.withIndex()) {
            val mutableItem = item.toMutableMap()
            mutableItem["rank"] = startRank + index
            items.add(mutableItem)
        }

        val surveyUserId = userPreferencesRepository
            .userPreferencesFlow
            .map { it.surveyUserId }
            .first()

        val body = mutableMapOf(
            "survey_user_id" to surveyUserId,
            "user_id" to userId,
            "source" to source.value,
            "type" to type.value,
            "items" to items
        )
        if (collectionId != null) {
            body["collection_id"] = collectionId
        }

        val requestBody = networkClient.encode(body).toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val response = networkClient.execute(request)
        val responseCollectionId = response?.get("collection_id") as String?

        Timber.tag("StorageService").d("%s Collection Id: %s", source.value, responseCollectionId)
        return responseCollectionId
    }

}
