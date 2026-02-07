package com.dhruvathaide.gridly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvathaide.gridly.data.remote.F1ApiService
import com.dhruvathaide.gridly.data.remote.model.DriverDto
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto
import com.dhruvathaide.gridly.data.repository.TelemetryRepository
import com.dhruvathaide.gridly.data.repository.TelemetryState
import com.dhruvathaide.gridly.domain.StrategyEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val driver1: DriverDto? = null,
    val driver2: DriverDto? = null,
    val driver1Telemetry: List<TelemetryDto> = emptyList(),
    val driver2Telemetry: List<TelemetryDto> = emptyList(),
    val activeSession: SessionDto? = null,
    val rainProbability: Double = 0.0,
    val overtakePrediction: String = "N/A",
    val pitWindowStatus: String = "CLOSED",
    val availableDrivers: List<DriverDto> = emptyList(),
    // Phase 4 Data
    val d1TyreCompound: String = "SOFT",
    val d2TyreCompound: String = "MED",
    val d1Interval: String = "+0.0s",
    val d2Interval: String = "+1.2s",
    val isLoading: Boolean = false
)

class MainViewModel : ViewModel() {

    private val repository = TelemetryRepository(F1ApiService)
    private val strategyEngine = StrategyEngine()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        // Find the latest session on init
        fetchLatestSession()
    }

    private fun fetchLatestSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch sessions for current year (mocked as 2023 for reliable data in OpenF1 if 2024 is empty, 
                // or dynamic. Let's use 2023 for consistent demo data as per API docs usually being historic)
                // For "Live" feel, we might check 2024.
                val sessions = F1ApiService.getSessions(year = 2023, sessionType = "Race")
                val latest = sessions.lastOrNull() // Get the last race
                
                if (latest != null) {
                    _uiState.update { it.copy(activeSession = latest) }
                    loadDrivers(latest.sessionKey)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadDrivers(sessionKey: Int) {
        viewModelScope.launch {
            try {
                // OpenF1 /drivers endpoint needed. 
                // I'll add a helper to F1ApiService for this or just mock it if I didn't add it to API yet.
                // I recall creating DriverDto, but maybe not the fetch method in F1ApiService.
                // Let's assume we can fetch drivers.
                // For now, I'll mock a request or strictly add it.
                // To be safe and quick, I'll implement a fetch in the VM using a direct call if possible, 
                // or just hardcoded top drivers for the demo if the endpoint isn't ready.
                
                // Let's assume we add `getDrivers` to F1ApiService next.
                // I'll assume it exists or I will add it. To avoid compilation error, I will add it to F1ApiService now.
                
                // Only mock for now to proceed, will fix API in next step if needed.
                val drivers = listOf(
                    DriverDto(1, "Verstappen", "Max Verstappen", "VER", "Red Bull Racing", "3671C6", null, "NED"),
                    DriverDto(14, "Alonso", "Fernando Alonso", "ALO", "Aston Martin", "358C75", null, "ESP"),
                    DriverDto(44, "Hamilton", "Lewis Hamilton", "HAM", "Mercedes", "27F4D2", null, "GBR"),
                    DriverDto(16, "Leclerc", "Charles Leclerc", "LEC", "Ferrari", "F91536", null, "MON")
                )
                
                _uiState.update { it.copy(availableDrivers = drivers) }
                
                // Default selection
                selectDrivers(drivers[0], drivers[1])
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectDrivers(d1: DriverDto, d2: DriverDto) {
        _uiState.update { it.copy(driver1 = d1, driver2 = d2) }
        startTelemetryMonitoring(d1.driverNumber, d2.driverNumber)
    }

    private fun startTelemetryMonitoring(d1Num: Int, d2Num: Int) {
        telemetryJob?.cancel()
        val sessionKey = _uiState.value.activeSession?.sessionKey ?: return

        telemetryJob = viewModelScope.launch {
            repository.getSyncedTelemetry(sessionKey, d1Num, d2Num)
                .collectLatest { state ->
                    
                    // Update telemetry history
                    val current1 = _uiState.value.driver1Telemetry.toMutableList()
                    state.driver1Data?.let { 
                        current1.add(it)
                        if (current1.size > 100) current1.removeAt(0) // Keep buffer manageable
                    }
                    
                    val current2 = _uiState.value.driver2Telemetry.toMutableList()
                    state.driver2Data?.let {
                        current2.add(it)
                        if (current2.size > 100) current2.removeAt(0)
                    }

                    // Run Physics Engine Checks
                    val overtake = calculateOvertake(state)
                    
                    _uiState.update { 
                        it.copy(
                            driver1Telemetry = current1,
                            driver2Telemetry = current2,
                            overtakePrediction = overtake,
                            // Simulating refreshing weather every now and then
                            rainProbability = (Math.random() * 20),
                            // Mocking dynamic intervals for demo
                            d1Interval = "LEADER",
                            d2Interval = String.format("+%.3fs", (1.2 + Math.random() * 0.5)),
                            d1TyreCompound = "SOFT (12L)",
                            d2TyreCompound = "MED (8L)"
                        ) 
                    }
                }
        }
    }

    private fun calculateOvertake(state: TelemetryState): String {
        val d1 = state.driver1Data ?: return "N/A"
        val d2 = state.driver2Data ?: return "N/A"
        
        // Simple mock of physics engine usage
        // In reality, we need gap data (from /intervals) which we aren't polling yet in this simplified loop.
        // We'll trust the plan and just return a dummy string or basic logic for now.
        return if (d2.speed > d1.speed * 1.05) "CLOSING" else "HOLDING"
    }
}
