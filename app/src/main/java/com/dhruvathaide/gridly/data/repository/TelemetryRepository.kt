package com.dhruvathaide.gridly.data.repository

import com.dhruvathaide.gridly.data.remote.F1ApiService
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class TelemetryState(
    val driver1Data: TelemetryDto?,
    val driver2Data: TelemetryDto?,
    val timestamp: String
)

class TelemetryRepository(
    private val apiService: F1ApiService
) {
    /**
     * Polls global telemetry for a session and filters for d1/d2 locally.
     * This reduces API calls from 2 per interval to 1 per interval.
     */
    fun getSyncedTelemetry(sessionKey: Int, driver1: Int, driver2: Int): Flow<TelemetryState> = flow {
        var lastDate: String? = null
        var errorCount = 0
        
        while (true) {
            try {
                // Fetch GLOBAL telemetry for the session (driverNumber = null)
                // OpenF1 supports fetching all cars if driver_number is omitted.
                val allData = apiService.getTelemetry(
                    sessionKey = sessionKey,
                    driverNumber = null, // Fetch ALL
                    dateStart = lastDate
                )

                errorCount = 0

                if (allData.isNotEmpty()) {
                    // Find latest available timestamp across all data to update cursor
                    val maxDate = allData.maxByOrNull { it.date }?.date
                    if (maxDate != null) lastDate = maxDate

                    // Filter for our target drivers
                    val d1Data = allData.filter { it.driverNumber == driver1 }.maxByOrNull { it.date }
                    val d2Data = allData.filter { it.driverNumber == driver2 }.maxByOrNull { it.date }
                    
                    val timestamp = d1Data?.date ?: d2Data?.date ?: ""
                    
                    // Emit only if we have something relevant (or just emit whatever we found to keep UI alive)
                    emit(TelemetryState(d1Data, d2Data, timestamp))
                }
                
                delay(3000) // Poll every 3 seconds (20 req/min)
            } catch (e: Exception) {
                e.printStackTrace()
                errorCount++
                val backoff = 1000L * (1 shl minOf(errorCount, 4))
                delay(backoff)
            }
        }
    }
    
    private fun minOf(a: Int, b: Int): Int = if (a <= b) a else b
}
