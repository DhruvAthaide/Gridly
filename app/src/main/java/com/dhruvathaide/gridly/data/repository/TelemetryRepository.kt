package com.dhruvathaide.gridly.data.repository

import com.dhruvathaide.gridly.data.remote.F1ApiService
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class TelemetryState(
    val driver1Data: TelemetryDto?,
    val driver2Data: TelemetryDto?,
    val timestamp: String
)

class TelemetryRepository(
    private val apiService: F1ApiService
) {
    // Helper to poll telemetry for a specific driver
    private fun pollDriverTelemetry(sessionKey: Int, driverNumber: Int): Flow<TelemetryDto?> = flow {
        var lastDate: String? = null
        while (true) {
            try {
                // Fetch latest data. In a real app, we might optimize this to only fetch new data since lastDate.
                // OpenF1 allows filtering by date.
                val data = apiService.getTelemetry(
                    sessionKey = sessionKey,
                    driverNumber = driverNumber,
                    dateStart = lastDate
                )
                
                if (data.isNotEmpty()) {
                    val latest = data.maxByOrNull { it.date }
                    latest?.let {
                        lastDate = it.date // Update marker
                        emit(it)
                    }
                }
            } catch (e: Exception) {
                // Handle error or emit null/previous state
                e.printStackTrace()
            }
            delay(1000) // Poll every second for "live" feel
        }
    }

    fun getSyncedTelemetry(sessionKey: Int, driver1: Int, driver2: Int): Flow<TelemetryState> {
        val driver1Flow = pollDriverTelemetry(sessionKey, driver1)
        val driver2Flow = pollDriverTelemetry(sessionKey, driver2)

        return combine(driver1Flow, driver2Flow) { d1, d2 ->
             // logic to align or just return the latest of both
             // For true alignment, we might check timestamps.
             // Here we return the latest available for each as the polling aligns them roughly by wall-clock time.
             // A more complex impl would buffer and match.
             val ts = d1?.date ?: d2?.date ?: ""
             TelemetryState(d1, d2, ts)
        }
    }
}
