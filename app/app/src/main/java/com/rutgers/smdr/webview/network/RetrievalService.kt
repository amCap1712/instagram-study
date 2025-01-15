package com.rutgers.smdr.webview.network

import android.util.Log
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.internal.EMPTY_REQUEST
import com.rutgers.smdr.NetworkClient

class RetrievalService(private val networkClient: NetworkClient) {

    private val MAX_RESULTS = 100

    suspend fun retrieveData(url: String, method: String, headers: Headers, cookie: String): List<Map<String, Any>> {
        val feedItems = mutableListOf<Map<String, Any>>()
        var maxId: String? = null
        while (feedItems.size < MAX_RESULTS) {
            val formBody = if (maxId != null) {
                FormBody.Builder().add("max_id", maxId).build()
            } else EMPTY_REQUEST

            val request = Request.Builder()
                .url(url)
                .headers(headers)
                .addHeader("Cookie", cookie)
                .method(method, formBody)
                .build()

            val response = networkClient.execute(request)
            feedItems.addAll(response?.get("feed_items") as Collection<Map<String, Any>>)
            debugFeed(response)

            if (response["next_max_id"] == null) {
                break
            }
            maxId = response["next_max_id"].toString()
        }
        return feedItems
    }

    private fun debugFeed(json: Map<String, Any?>) {
        Log.i("LoggingWebClient", "Next Max ID: ${json["next_max_id"]}")
        for (item in json["feed_items"] as Collection<Map<String, Map<String, Any>>>) {
            if (item["end_of_feed_demarcator"] != null) {
                val feedDemarcator = item["end_of_feed_demarcator"] as Map<String, Any>
                val groupSet = feedDemarcator["group_set"] as Map<String, Any>
                val groups = groupSet["groups"] as Collection<Map<String, String>>
                for (group in groups) {
                    Log.i("LoggingWebClient", "Next Max ID: ${group["title"]}: ${group["next_max_id"]}")
                }
            }
        }
    }

}
