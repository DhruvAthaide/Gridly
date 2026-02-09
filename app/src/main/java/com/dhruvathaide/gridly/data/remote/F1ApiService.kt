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
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

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
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 5000
        }
    }

    suspend fun getSessions(year: Int, sessionType: String? = null): List<SessionDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/sessions") {
                parameter("year", year)
                if (sessionType != null) {
                    parameter("session_type", sessionType)
                }
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getSessions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRaceControl(sessionKey: Int, dateStart: String? = null): List<com.dhruvathaide.gridly.data.remote.model.RaceControlDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/race_control") {
                parameter("session_key", sessionKey)
                if (dateStart != null) parameter("date>", dateStart)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getRaceControl: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getTeamRadio(sessionKey: Int, driverNumber: Int? = null): List<com.dhruvathaide.gridly.data.remote.model.TeamRadioDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/team_radio") {
                parameter("session_key", sessionKey)
                if (driverNumber != null) parameter("driver_number", driverNumber)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getTeamRadio: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTelemetry(
        sessionKey: Int,
        driverNumber: Int? = null,
        lapNumber: Int? = null,
        dateStart: String? = null,
        dateEnd: String? = null
    ): List<TelemetryDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/car_data") {
                parameter("session_key", sessionKey)
                if (driverNumber != null) parameter("driver_number", driverNumber)
                if (lapNumber != null) parameter("lap_number", lapNumber)
                if (dateStart != null) parameter("date>", dateStart)
                if (dateEnd != null) parameter("date<", dateEnd)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getTelemetry: ${e.message}")
            emptyList()
        }
    }

    suspend fun getWeather(
        sessionKey: Int,
        dateStart: String? = null
    ): List<WeatherDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/weather") {
                parameter("session_key", sessionKey)
                if (dateStart != null) parameter("date>", dateStart)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getWeather: ${e.message}")
            emptyList()
        }
    }

    suspend fun getIntervals(
        sessionKey: Int,
        driverNumber: Int? = null,
        dateStart: String? = null
    ): List<IntervalDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/intervals") {
                parameter("session_key", sessionKey)
                if (driverNumber != null) parameter("driver_number", driverNumber)
                if (dateStart != null) parameter("date>", dateStart)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getIntervals: ${e.message}")
            emptyList()
        }
    }
    
    

    suspend fun getDrivers(sessionKey: Int): List<com.dhruvathaide.gridly.data.remote.model.DriverDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/drivers") {
                parameter("session_key", sessionKey)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getDrivers: ${e.message}")
            emptyList()
        }
    }

    suspend fun getStints(sessionKey: Int, driverNumber: Int? = null): List<com.dhruvathaide.gridly.data.remote.model.StintDto> {
        return try {
            RateLimiter.acquire()
            val response = client.get("$BASE_URL/stints") {
                parameter("session_key", sessionKey)
                if (driverNumber != null) parameter("driver_number", driverNumber)
            }
            if (response.status.value == 200) response.body() else emptyList()
        } catch (e: Exception) {
            println("F1ApiService Error getStints: ${e.message}")
            emptyList()
        }
    }


    
    // --- RSS NEWS ---
    
    data class NewsItemDto(
        val title: String,
        val link: String,
        val pubDate: String,
        val description: String
    )

    suspend fun fetchRssNews(rssUrls: List<String>): List<NewsItemDto> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val deferreds = rssUrls.map { url ->
                async {
                    try {
                        val response = client.get(url)
                        if (response.status.value == 200) {
                            val xmlContent = response.body<String>()
                            // Sanitize: Remove DOCTYPE to prevent generic XmlPullParser errors
                            val cleanXml = xmlContent.replace(Regex("<!DOCTYPE[^>]*>"), "")
                            RssParser().parse(cleanXml)
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        // Log but continue
                        println("F1ApiService Error fetching News from $url: ${e.message}")
                        emptyList<NewsItemDto>()
                    }
                }
            }
            deferreds.awaitAll().flatten().distinctBy { it.title }.take(20)
        }
    }
}
