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
}
