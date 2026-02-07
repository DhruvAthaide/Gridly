package com.dhruvathaide.gridly.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("date") val date: String,
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("air_temperature") val airTemperature: Double,
    @SerialName("humidity") val humidity: Double,
    @SerialName("pressure") val pressure: Double,
    @SerialName("rainfall") val rainfall: Int, // 0 = No, 1 = Yes
    @SerialName("track_temperature") val trackTemperature: Double,
    @SerialName("wind_direction") val windDirection: Int,
    @SerialName("wind_speed") val windSpeed: Double
)

@Serializable
data class IntervalDto(
    @SerialName("date") val date: String,
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("gap_to_leader") val gapToLeader: String?, // e.g. "12.345" or "LAP 1"
    @SerialName("interval") val interval: String? // Gap to car ahead
)
