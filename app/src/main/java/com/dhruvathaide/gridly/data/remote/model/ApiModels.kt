package com.dhruvathaide.gridly.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelemetryDto(
    @SerialName("date") val date: String,
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("speed") val speed: Int,
    @SerialName("throttle") val throttle: Int,
    @SerialName("brake") val brake: Int,
    @SerialName("rpm") val rpm: Int,
    @SerialName("gear") val gear: Int,
    @SerialName("drs") val drs: Int
)

@Serializable
data class SessionDto(
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("session_name") val sessionName: String,
    @SerialName("date_start") val dateStart: String,
    @SerialName("date_end") val dateEnd: String,
    @SerialName("gmt_offset") val gmtOffset: String,
    @SerialName("session_type") val sessionType: String,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("location") val location: String,
    @SerialName("country_key") val countryKey: Int,
    @SerialName("country_code") val countryCode: String,
    @SerialName("country_name") val countryName: String,
    @SerialName("circuit_key") val circuitKey: Int,
    @SerialName("circuit_short_name") val circuitShortName: String,
    @SerialName("year") val year: Int
)

@Serializable
data class DriverDto(
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("broadcast_name") val broadcastName: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("name_acronym") val nameAcronym: String,
    @SerialName("team_name") val teamName: String,
    @SerialName("team_colour") val teamColour: String,
    @SerialName("headshot_url") val headshotUrl: String?,
    @SerialName("country_code") val countryCode: String
)

@Serializable
data class LapDto(
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("lap_number") val lapNumber: Int,
    @SerialName("date_start") val dateStart: String?,
    @SerialName("lap_duration") val lapDuration: Double?,
    @SerialName("is_pit_out_lap") val isPitOutLap: Boolean,
    @SerialName("duration_sector_1") val durationSector1: Double?,
    @SerialName("duration_sector_2") val durationSector2: Double?,
    @SerialName("duration_sector_3") val durationSector3: Double?
)
