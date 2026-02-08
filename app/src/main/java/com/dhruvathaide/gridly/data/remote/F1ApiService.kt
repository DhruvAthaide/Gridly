package com.dhruvathaide.gridly.data.remote

import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto
import com.dhruvathaide.gridly.data.remote.model.WeatherDto
import com.dhruvathaide.gridly.data.remote.model.IntervalDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object F1ApiService {
    private const val BASE_URL = "https://api.openf1.org/v1"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun getSessions(year: Int, sessionType: String? = null): List<SessionDto> {
        return client.get("$BASE_URL/sessions") {
            parameter("year", year)
            if (sessionType != null) {
                parameter("session_type", sessionType)
            }
        }.body()
    }

    suspend fun getTelemetry(
        sessionKey: Int,
        driverNumber: Int,
        dateStart: String? = null,
        dateEnd: String? = null
    ): List<TelemetryDto> {
        return client.get("$BASE_URL/car_data") {
            parameter("session_key", sessionKey)
            parameter("driver_number", driverNumber)
            if (dateStart != null) parameter("date>", dateStart)
            if (dateEnd != null) parameter("date<", dateEnd)
        }.body()
    }

    suspend fun getWeather(
        sessionKey: Int,
        dateStart: String? = null
    ): List<WeatherDto> {
        return client.get("$BASE_URL/weather") {
            parameter("session_key", sessionKey)
            if (dateStart != null) parameter("date>", dateStart)
        }.body()
    }

    suspend fun getIntervals(
        sessionKey: Int,
        driverNumber: Int? = null,
        dateStart: String? = null
    ): List<IntervalDto> {
        return client.get("$BASE_URL/intervals") {
            parameter("session_key", sessionKey)
            if (driverNumber != null) parameter("driver_number", driverNumber)
            if (dateStart != null) parameter("date>", dateStart)
        }.body()
    }
    
    

    suspend fun getDrivers(sessionKey: Int): List<com.dhruvathaide.gridly.data.remote.model.DriverDto> {
        return client.get("$BASE_URL/drivers") {
            parameter("session_key", sessionKey)
        }.body()
    }

    suspend fun getStints(sessionKey: Int, driverNumber: Int? = null): List<com.dhruvathaide.gridly.data.remote.model.StintDto> {
        return client.get("$BASE_URL/stints") {
            parameter("session_key", sessionKey)
            if (driverNumber != null) parameter("driver_number", driverNumber)
        }.body()
    }

    suspend fun getRaceControl(sessionKey: Int, dateStart: String? = null): List<com.dhruvathaide.gridly.data.remote.model.RaceControlDto> {
        return client.get("$BASE_URL/race_control") {
            parameter("session_key", sessionKey)
            if (dateStart != null) parameter("date>", dateStart)
        }.body()
    }
    
    // --- RSS NEWS ---
    
    data class NewsItemDto(
        val title: String,
        val link: String,
        val pubDate: String,
        val description: String
    )

    suspend fun fetchRssNews(): List<NewsItemDto> {
        // Using Motorsport.com F1 RSS (Public)
        val rssUrl = "https://www.motorsport.com/rss/f1/news/" 
        // Alternative: "https://www.autosport.com/rss/feed/f1"
        
        try {
            val xmlContent = client.get(rssUrl).body<String>()
            return parseRss(xmlContent)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun parseRss(xml: String): List<NewsItemDto> {
        val items = mutableListOf<NewsItemDto>()
        // Simple Regex Parser for Demo (Avoids full XML parser dependency)
        // Matches <item>...</item> blocks
        val itemRegex = "<item>(.*?)</item>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val linkRegex = "<link>(.*?)</link>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val dateRegex = "<pubDate>(.*?)</pubDate>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val descRegex = "<description>(.*?)</description>".toRegex(RegexOption.DOT_MATCHES_ALL)

        itemRegex.findAll(xml).forEach { match ->
            val block = match.groupValues[1]
            val title = titleRegex.find(block)?.groupValues?.get(1)?.replace("<![CDATA[", "")?.replace("]]>", "")?.trim() ?: ""
            val link = linkRegex.find(block)?.groupValues?.get(1)?.trim() ?: ""
            val pubDate = dateRegex.find(block)?.groupValues?.get(1)?.trim() ?: ""
            val description = descRegex.find(block)?.groupValues?.get(1)?.replace("<![CDATA[", "")?.replace("]]>", "")?.trim() ?: ""
            
            if (title.isNotEmpty()) {
                items.add(NewsItemDto(title, link, pubDate, description))
            }
        }
        return items.take(10)
    }
}
