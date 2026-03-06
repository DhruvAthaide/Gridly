package com.dhruvathaide.gridly.data.repository

import com.dhruvathaide.gridly.data.remote.F1ApiService
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
     * Polls telemetry for two specific drivers using targeted API calls.
     * Uses date cursor for incremental updates to minimize payload size.
     */
    fun getSyncedTelemetry(sessionKey: Int, driver1: Int, driver2: Int): Flow<TelemetryState> = flow {
        var lastDate: String? = null
        var errorCount = 0

        while (true) {
            try {
                // Fetch telemetry for each driver separately — much smaller payloads
                val (d1List, d2List) = coroutineScope {
                    val d1 = async {
                        apiService.getTelemetry(
                            sessionKey = sessionKey,
                            driverNumber = driver1,
                            dateStart = lastDate
                        )
                    }
                    val d2 = async {
                        apiService.getTelemetry(
                            sessionKey = sessionKey,
                            driverNumber = driver2,
                            dateStart = lastDate
                        )
                    }
                    Pair(d1.await(), d2.await())
                }

                errorCount = 0

                val d1Data = d1List.maxByOrNull { it.date }
                val d2Data = d2List.maxByOrNull { it.date }

                // Update cursor to latest timestamp
                val maxDate = listOfNotNull(d1Data?.date, d2Data?.date).maxOrNull()
                if (maxDate != null) lastDate = maxDate

                if (d1Data != null || d2Data != null) {
                    val timestamp = d1Data?.date ?: d2Data?.date ?: ""
                    emit(TelemetryState(d1Data, d2Data, timestamp))
                }

                delay(4000) // Poll every 4 seconds
            } catch (e: Exception) {
                e.printStackTrace()
                errorCount++
                val backoff = 2000L * (1 shl minOf(errorCount, 4))
                delay(backoff)
            }
        }
    }

    private fun minOf(a: Int, b: Int): Int = if (a <= b) a else b
}
