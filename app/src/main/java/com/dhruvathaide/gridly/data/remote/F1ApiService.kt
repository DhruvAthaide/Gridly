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

    suspend fun getRaceControl(sessionKey: Int, dateStart: String? = null): List<com.dhruvathaide.gridly.data.remote.model.RaceControlDto> {
        return client.get("$BASE_URL/race_control") {
            parameter("session_key", sessionKey)
            if (dateStart != null) parameter("date>", dateStart)
        }.body()
    }
    
    suspend fun getTeamRadio(sessionKey: Int, driverNumber: Int? = null): List<com.dhruvathaide.gridly.data.remote.model.TeamRadioDto> {
        return client.get("$BASE_URL/team_radio") {
            parameter("session_key", sessionKey)
            if (driverNumber != null) parameter("driver_number", driverNumber)
        }.body()
    }

    suspend fun getTelemetry(
        sessionKey: Int,
        driverNumber: Int,
        lapNumber: Int? = null,
        dateStart: String? = null,
        dateEnd: String? = null
    ): List<TelemetryDto> {
        return client.get("$BASE_URL/car_data") {
            parameter("session_key", sessionKey)
            parameter("driver_number", driverNumber)
            if (lapNumber != null) parameter("lap_number", lapNumber)
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


    
    // --- RSS NEWS ---
    
    data class NewsItemDto(
        val title: String,
        val link: String,
        val pubDate: String,
        val description: String
    )

    suspend fun fetchRssNews(rssUrls: List<String>): List<NewsItemDto> {
        val allNews = mutableListOf<NewsItemDto>()
        
        // Fetch concurrently? For simplicity, sequential or simple logic
        rssUrls.forEach { url ->
            try {
                val xmlContent = client.get(url).body<String>()
                allNews.addAll(parseRss(xmlContent))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Deduplicate by title and sort by date (if possible, but date parsing is weak here)
        // Just distinct by title for now
        return allNews.distinctBy { it.title }.take(20)
    }

    private fun parseRss(xml: String): List<NewsItemDto> {
        val items = mutableListOf<NewsItemDto>()
        try {
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(java.io.StringReader(xml))

            var eventType = xpp.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentPubDate = ""
            var currentDescription = ""
            var insideItem = false

            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG) {
                    if (xpp.name.equals("item", ignoreCase = true)) {
                        insideItem = true
                    } else if (insideItem) {
                        when (xpp.name.lowercase()) {
                            "title" -> currentTitle = safeNextText(xpp)
                            "link" -> currentLink = safeNextText(xpp)
                            "pubdate" -> currentPubDate = safeNextText(xpp)
                            "description" -> currentDescription = safeNextText(xpp)
                        }
                    }
                } else if (eventType == org.xmlpull.v1.XmlPullParser.END_TAG) {
                    if (xpp.name.equals("item", ignoreCase = true)) {
                        insideItem = false
                        // Clean HTML from description
                        val cleanDesc = currentDescription.replace(Regex("<[^>]*>"), "").trim()
                        if (currentTitle.isNotEmpty()) {
                            items.add(NewsItemDto(
                                title = currentTitle.trim(),
                                link = currentLink.trim(),
                                pubDate = currentPubDate.trim(),
                                description = cleanDesc
                            ))
                        }
                        // Reset
                        currentTitle = ""
                        currentLink = ""
                        currentPubDate = ""
                        currentDescription = ""
                    }
                }
                eventType = xpp.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    private fun safeNextText(xpp: org.xmlpull.v1.XmlPullParser): String {
        return try {
            if (xpp.next() == org.xmlpull.v1.XmlPullParser.TEXT) {
                val text = xpp.text
                xpp.nextTag() // Advance to end tag
                text
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
