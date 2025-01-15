package com.rutgers.smdr.webview.client

import android.content.Context
import android.os.Environment
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.rutgers.smdr.NetworkClient
import com.rutgers.smdr.webview.datastore.RequestRepository
import com.rutgers.smdr.webview.network.CollectionSource
import com.rutgers.smdr.webview.network.CollectionType
import com.rutgers.smdr.webview.network.StorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min


class RequestBridge(
    private val storage: StorageService,
    private val repository: RequestRepository,
    private val networkClient: NetworkClient,
    private val webView: WebView,
    private val coroutineScope: CoroutineScope,
    private val context: Context,
    private val injectJson: String
) {
    private val mutex = Mutex()

    private var itemsToInject: List<Map<String, Any>>? = null
    private var injectedIndex = 0

    private var feedCollectionId: String? = null
    private var reelsCollectionId: String? = null
    private var exploreCollectionId: String? = null
    private var feedItemsCount = 0
    private var reelsItemsCount = 0
    private var exploreItemsCount = 0

    private val seenFeedIds = mutableSetOf<String>()
    private val seenReelIds = mutableSetOf<String>()

    @JavascriptInterface
    public fun record(type: String, response: String): String {
        val json = networkClient.decode(response) ?: return response
        if (type.equals("feed", ignoreCase = true)) {
            val newFeed = recordFeed(json)
            webView.post {
                // TODO: Get the list of usernames from API and make highlighting more precise
                webView.evaluateJavascript(
                    """
                setInterval(function () {
                    const users = ["theonion", "tokyo_uyon", "_poetry_in_pictures_"];
                    for (const user of users) {
                        const elements = document.querySelectorAll(`a[href^='/${'$'}{user}']`);
                        for (const element of elements) {
                            const item = element.closest("article");
                            item.style.borderStyle = "solid";
                            item.style.borderWidth = "12px";
                            item.style.borderColor = "red";
                        }
                    }
                }, 5000);
            """.trimIndent(), null
                )
            }
            if (newFeed != null) {
                val newResponse = networkClient.encode(newFeed)

                val oldFile = File(context.getExternalFilesDir(null), "originalResponse.json")
                val oldFos = FileOutputStream(oldFile)
                oldFos.write(response.toByteArray())
                oldFos.flush()
                oldFos.close()

                val newFile = File(context.getExternalFilesDir(null), "modifiedResponse.json")
                val newFos = FileOutputStream(newFile)
                newFos.write(newResponse.toByteArray())
                newFos.flush()
                newFos.close()

                return newResponse
            }
        }
        if (type.equals("reels", ignoreCase = true)) {
            recordReels(json)
        }
        if (type.equals("explore", ignoreCase = true)) {
            recordExplore(json)
        }
        return response
    }

    private fun recordExplore(json: Map<String, Any?>) {
        val itemsToSubmit = mutableListOf<Map<String, Any>>()
        val data = json["sectional_items"] as Collection<Map<String, Any>>? ?: return
        for (item in data) {
            itemsToSubmit.add(item)
        }

        if (itemsToSubmit.isEmpty()) {
            return
        }

        coroutineScope.launch {
            mutex.withLock {
                val request = repository.requestFlow.first()
                exploreCollectionId = storage.submitItems(
                    CollectionSource.EXPLORE,
                    CollectionType.ACTIVE,
                    request.userId,
                    itemsToSubmit,
                    exploreCollectionId,
                    exploreItemsCount
                )
                exploreItemsCount += itemsToSubmit.size
            }
        }
    }

    private fun recordReels(json: Map<String, Any?>) {
        val itemsToSubmit = mutableListOf<Map<String, Any>>()

        val data = json["data"] as Map<String, Any>? ?: return
        val clipsHome = data["xdt_api__v1__clips__home__connection"] as Map<String, Any>?
            ?: data["xdt_api__v1__clips__home__connection_v2"] as Map<String, Any>?
            ?: return
        val edges = clipsHome["edges"] as MutableList<Map<String, Any>>? ?: return
        for (edge in edges) {
            val node = edge["node"] as Map<String, Any>
            val mediaOrAd = node["media"] as Map<String, Any>
            if (mediaOrAd["id"] != null) {
                if (seenReelIds.contains(mediaOrAd["id"])) {
                    continue
                } else {
                    seenReelIds.add(mediaOrAd["id"] as String)
                }
            } else {
                Log.e("Request Bridge", "Unknown reel item encountered: $node")
            }
            itemsToSubmit.add(node)
        }
//        (((edges[2]["node"] as Map<String, Any>)["media"] as Map<String, Any>)["caption"] as MutableMap<String, Any>)["text"] = "Interceptor from Android says hello!"
//        edges.removeAt(1)
//        edges.add(0, edges[0])

        if (itemsToSubmit.isEmpty()) {
            return
        }

        coroutineScope.launch {
            mutex.withLock {
                val request = repository.requestFlow.first()
                reelsCollectionId = storage.submitItems(
                    CollectionSource.REELS,
                    CollectionType.ACTIVE,
                    request.userId,
                    itemsToSubmit,
                    reelsCollectionId,
                    reelsItemsCount
                )
                reelsItemsCount += itemsToSubmit.size
            }
        }
    }

    private fun recordFeed(json: Map<String, Any?>): Map<String, Any?>? {
        val data = json["data"] as Map<String, Any>? ?: return null
        val clipsHome = data["xdt_api__v1__feed__home__connection"] as Map<String, Any>?
            ?: data["xdt_api__v1__feed__timeline__connection"] as Map<String, Any>?
            ?: return null
        val edges = clipsHome["edges"] as MutableList<Map<String, Any>>? ?: return null
        val itemsToSubmit = deduplicateFeedItems(edges)

        val newFeed = injectFeed(json)
        if (itemsToSubmit.isEmpty()) {
            return newFeed
        }

        coroutineScope.launch {
            mutex.withLock {
                val request = repository.requestFlow.first()
                feedCollectionId = storage.submitItems(
                    CollectionSource.FEED,
                    CollectionType.ACTIVE,
                    request.userId,
                    itemsToSubmit,
                    feedCollectionId,
                    feedItemsCount
                )
                feedItemsCount += itemsToSubmit.size
            }
        }

        return newFeed
    }

    private fun injectFeed(json: Map<String, Any?>): Map<String, Any?> {
        if (itemsToInject == null) {
            runBlocking {
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("kiran-research2.comminfo.rutgers.edu")
                    .addPathSegment("data-collector-admin")
                    .addPathSegment("inject")
                    .build()
                val request = Request.Builder().url(url).get().build()
                val response = networkClient.execute(request)
                itemsToInject = (response?.get("items") as Collection<Map<String, Any>>).toList().reversed()
            }
        }
        val itemToInjectSize = itemsToInject?.size ?: 0

        val data = json["data"] as Map<String, Any>
        val items = data["xdt_api__v1__feed__home__connection"] as MutableMap<String, Any>?
            ?: data["xdt_api__v1__feed__timeline__connection"] as MutableMap<String, Any>
        val feedItems = items["edges"] as MutableList<Map<String, Any>>

        if (injectedIndex >= itemToInjectSize) {
            return json
        }

        val itemsToInjectCount = min(feedItems.size, itemToInjectSize - injectedIndex)
        val actualItemsToInject =
            itemsToInject?.subList(injectedIndex, injectedIndex + itemsToInjectCount) ?: listOf()
        val finalItems = feedItems
            .zip(actualItemsToInject) { first, second -> listOf(first, second) }
            .flatten()
            .toMutableList()
        finalItems.add(0, networkClient.decode(injectJson)!! as Map<String, Any>)
        items["edges"] = finalItems
        val newJson = json.toMutableMap()
        injectedIndex += itemsToInjectCount
        return newJson
    }

    private fun deduplicateFeedItems(items: Collection<Map<String, Any>>): Collection<Map<String, Any>> {
        val itemsToSubmit = mutableListOf<Map<String, Any>>()
        for (edge in items) {
            val item = edge["node"] as Map<String, Any>
            var itemId: String? = null
            if (item["explore_story"] != null) {
                itemId = (item["explore_story"] as Map<String, Any>)["id"] as String
            } else if (item["media_or_ad"] != null) {
                val mediaOrAd = item["media_or_ad"] as Map<String, Any>
                if (mediaOrAd["id"] != null) {
                    itemId = mediaOrAd["id"] as String
                } else if (mediaOrAd["media_id"] != null) {
                    itemId = mediaOrAd["media_id"] as String
                }
            } else if (item["end_of_feed_demarcator"] != null) {
                itemId = (item["end_of_feed_demarcator"] as Map<String, Any>)["id"] as String
            } else if (item["ad"] != null) {
                itemId = (item["ad"] as Map<String, Any>)["ad_id"] as String
            }
            if (itemId == null) {
                Log.e("RequestBridge", "Item Id not found: $item")
                continue
            }
            if (!seenFeedIds.contains(itemId)) {
                itemsToSubmit.add(item)
                seenFeedIds.add(itemId)
            } else {
                Log.e("RequestBridge", "Duplicate item seen, type: ${item.keys}")
            }
        }
        return itemsToSubmit
    }

}
