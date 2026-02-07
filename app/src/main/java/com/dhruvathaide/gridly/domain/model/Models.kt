package com.dhruvathaide.gridly.domain.model

data class DriverPosition(
    val driverNumber: Int,
    val currentLapTime: Double?, // in seconds
    val gapToLeader: Double,
    val tireAge: Int
)

data class Lap(
    val lapNumber: Int,
    val lapTime: Double, // in seconds
    val tireCompound: String
)
