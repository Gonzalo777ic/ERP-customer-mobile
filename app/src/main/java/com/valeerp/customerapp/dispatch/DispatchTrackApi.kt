package com.valeerp.customerapp.dispatch

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DispatchTrackApi(
    private var baseUrl: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build(),
) {
    fun updateBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun getTrip(publicCode: String): TripSnapshot {
        val req = Request.Builder()
            .url("$baseUrl/dispatch/trips/${publicCode.trim()}")
            .get()
            .build()
        client.newCall(req).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: $text")
            }
            val o = JSONObject(text)
            return TripSnapshot(
                publicCode = o.getString("publicCode"),
                label = o.optString("label"),
                status = o.getString("status"),
                lastLatitude = if (o.isNull("lastLatitude")) null else o.getDouble("lastLatitude"),
                lastLongitude = if (o.isNull("lastLongitude")) null else o.getDouble("lastLongitude"),
                lastLocatedAt = if (o.isNull("lastLocatedAt")) null else o.getString("lastLocatedAt"),
                pollIntervalSeconds = o.optInt("pollIntervalSeconds", 12),
            )
        }
    }
}

data class TripSnapshot(
    val publicCode: String,
    val label: String,
    val status: String,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastLocatedAt: String?,
    val pollIntervalSeconds: Int,
)
