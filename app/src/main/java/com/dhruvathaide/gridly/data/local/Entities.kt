package com.dhruvathaide.gridly.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "historical_race_sessions")
data class HistoricalRaceSession(
    @PrimaryKey val sessionKey: Int,
    val sessionName: String,
    val dateStart: String,
    val countryName: String,
    val circuitShortName: String,
    val sessionType: String
)

@Entity(tableName = "lap_data")
data class LapEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionKey: Int,
    val driverNumber: Int,
    val lapNumber: Int,
    val lapDuration: Double?,
    val sector1: Double?,
    val sector2: Double?,
    val sector3: Double?
)
