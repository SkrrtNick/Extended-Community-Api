package org.tribot.api.ge

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object PriceLookup {
    private val httpClient = HttpClient.newHttpClient()
    private val gson = Gson()
    private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs"

    fun getPrice(itemId: Int): Pair<Int, Int>? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$BASE_URL/latest?id=$itemId"))
                .header("User-Agent", "tribot-community-api")
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = gson.fromJson(response.body(), JsonObject::class.java)
            val data = json.getAsJsonObject("data")?.getAsJsonObject(itemId.toString()) ?: return null
            val high = data.get("high")?.asInt ?: return null
            val low = data.get("low")?.asInt ?: return null
            Pair(high, low)
        } catch (e: Exception) {
            null
        }
    }
}
