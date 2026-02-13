package com.dhruvathaide.gridly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvathaide.gridly.data.remote.F1ApiService
import com.dhruvathaide.gridly.data.remote.model.DriverDto
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto
import com.dhruvathaide.gridly.data.remote.model.LapDto
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val driver1: DriverDto? = null,
    val driver2: DriverDto? = null,
    val driver1Telemetry: List<TelemetryDto> = emptyList(),
    val driver2Telemetry: List<TelemetryDto> = emptyList(),
    val activeSession: SessionDto? = null,
    val currentWeather: com.dhruvathaide.gridly.data.remote.model.WeatherDto? = null,
    val rainProbability: Double = 0.0,
    val overtakePrediction: String = "N/A",
    val pitWindowStatus: String = "CLOSED",
    val availableDrivers: List<DriverDto> = emptyList(),
    // Phase 4 Data
    val d1TyreCompound: String = "SOFT",
    val d2TyreCompound: String = "MED",
    val d1Interval: String = "+0.0s",
    val d2Interval: String = "+1.2s",
    
    // New: Sector Times (formatted as "S1 | S2 | S3" or separated)
    // New: Sector Times (formatted as "S1 | S2 | S3" or separated)
    val d1Sectors: Triple<String, String, String>? = null,
    val d2Sectors: Triple<String, String, String>? = null,
    
    // Phase 3: Strategy & Analysis
    val d1TyreLife: Int = 0,
    val d2TyreLife: Int = 0,
    val d1PitStops: Int = 0,
    val d2PitStops: Int = 0,
    val gapHistory: List<Float> = emptyList(),
    
    val raceControlMessage: String? = null, // New: Real Race Control messages
    val isLoading: Boolean = false,
    
    // Error Handling
    val errorMessage: String? = null,
    val isError: Boolean = false,

    // Production Refinements
    val newsFeed: List<com.dhruvathaide.gridly.data.MockDataProvider.NewsItem> = emptyList(),
    val newsFilters: List<MainViewModel.FeedSource> = emptyList(), // Filter State
    val driverStandings: List<com.dhruvathaide.gridly.data.MockDataProvider.DriverStanding> = emptyList(),
    val constructorStandings: List<com.dhruvathaide.gridly.data.MockDataProvider.ConstructorStanding> = emptyList(),
    
    // Battle Mode State
    val battleModeLap: Int = 1,
    val maxLaps: Int = 70, // Default max laps for slider
    val battleModeDriver1: DriverDto? = null,
    val battleModeDriver2: DriverDto? = null,
    val battleModeTelemetryD1: List<TelemetryDto> = emptyList(),
    val battleModeTelemetryD2: List<TelemetryDto> = emptyList(),
    val isBattleModeLoading: Boolean = false,
    
    // Team Radio
    val teamRadioMessages: List<com.dhruvathaide.gridly.data.remote.model.TeamRadioDto> = emptyList(),
    
    // Strategy
    val strategyStints: List<com.dhruvathaide.gridly.data.remote.model.StintDto> = emptyList()
)

class MainViewModel : ViewModel() {

    private val repository = TelemetryRepository(F1ApiService)
    private val strategyEngine = StrategyEngine()

    // Internal mutable state for "static" or user-driven data (Drivers, Session, content)
    private val _snapshotState = MutableStateFlow(DashboardUiState())
    
    // Exposed State: Combines snapshot with active telemetry ONLY when observed (WhileSubscribed)
    // Exposed State: Combines snapshot (News, Drivers) with live Telemetry
    // We use `combine` so that updates to _snapshotState (like News loading) are immediately emitted,
    // while `telemetryFlow` only restarts when drivers/session actually change.
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = 
        kotlinx.coroutines.flow.combine(
            _snapshotState,
            _snapshotState
                .map { Triple(it.activeSession, it.driver1, it.driver2) }
                .distinctUntilChanged { old, new ->
                    old.first?.sessionKey == new.first?.sessionKey &&
                    old.second?.driverNumber == new.second?.driverNumber &&
                    old.third?.driverNumber == new.third?.driverNumber
                }
                .flatMapLatest { (session, d1, d2) ->
                    if (session != null && d1 != null && d2 != null) {
                         repository.getSyncedTelemetry(session.sessionKey, d1.driverNumber, d2.driverNumber)
                    } else {
                         kotlinx.coroutines.flow.flowOf(TelemetryState(null, null, ""))
                    }
                }
        ) { state, telem ->
             // Merge telemetry into the current snapshot
             val current1 = state.driver1Telemetry.toMutableList().apply {
                  telem.driver1Data?.let { add(it); if (size > 100) removeAt(0) }
             }
             val current2 = state.driver2Telemetry.toMutableList().apply {
                  telem.driver2Data?.let { add(it); if (size > 100) removeAt(0) }
             }
             
             state.copy(
                 driver1Telemetry = current1,
                 driver2Telemetry = current2,
                 overtakePrediction = calculateOvertake(telem)
             )
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    private var strategyJob: Job? = null

    // --- News Feeds ---
    data class FeedSource(val name: String, val url: String, val isSelected: Boolean = true)
    private val allFeeds = listOf(
        FeedSource("Motorsport.com", "https://www.motorsport.com/rss/f1/news/"),
        FeedSource("Autosport", "https://www.autosport.com/rss/feed/f1"),
        FeedSource("BBC Sport", "https://feeds.bbci.co.uk/sport/formula1/rss.xml"),
        FeedSource("Crash.net", "https://www.crash.net/rss/f1"),
        FeedSource("GPFans", "https://www.gpfans.com/rss/f1-news.xml")
    )

    init {
        // Initialize default news filters (All selected by default)
        _snapshotState.update { it.copy(newsFilters = allFeeds.map { f -> f.copy(isSelected = true) }) }

        // Find the latest session on init
        fetchLatestSession()
        
        // Start low-freq polling for strategy data (Intervals, Weather)
        startStrategyPolling()
    }
    
    fun retry() {
        if (_snapshotState.value.isError) {
             fetchLatestSession()
        }
    }

    fun clearError() {
        _snapshotState.update { it.copy(isError = false, errorMessage = null) }
    }

    private fun fetchLatestSession() {
        viewModelScope.launch {
            _snapshotState.update { it.copy(isLoading = true, isError = false, errorMessage = null) }
            try {
                val isProduction = com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.value
                
                if (isProduction) {
                    val currentYear = java.time.Year.now().value
                    var sessions = F1ApiService.getSessions(year = currentYear, sessionType = "Race")
                    
                    val now = java.time.Instant.now()
                    val relevantSession = sessions.firstOrNull { session ->
                         try {
                             val end = java.time.Instant.parse(session.dateEnd)
                             end.isAfter(now)
                         } catch (e: Exception) { false }
                    } ?: sessions.lastOrNull()
                    
                    if (relevantSession != null) {
                         _snapshotState.update { it.copy(activeSession = relevantSession) }
                         loadDrivers(relevantSession.sessionKey)
                    } else {
                         _snapshotState.update { it.copy(activeSession = null, availableDrivers = emptyList()) }
                    }
                    
                    fetchNews()
                    _snapshotState.update { it.copy(driverStandings = emptyList(), constructorStandings = emptyList()) }
                    
                } else {
                     loadMockData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val isProduction = com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.value
                if (!isProduction) loadMockData()
                else {
                    _snapshotState.update { it.copy(
                        activeSession = null, 
                        availableDrivers = emptyList(),
                        isError = true,
                        errorMessage = "Failed to load session data: ${e.localizedMessage}"
                    ) }
                    // Load news even if session fails
                    fetchNews()
                }
            } finally {
                _snapshotState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    // --- News Feeds ---


    fun loadFilters(context: android.content.Context) {
        val saved = com.dhruvathaide.gridly.data.local.Prefs.getNewsFilters(context)
        val initialSelection = if (saved.isEmpty()) {
            allFeeds.map { it.copy(isSelected = true) }
        } else {
            allFeeds.map { it.copy(isSelected = saved.contains(it.url)) }
        }
        _snapshotState.update { it.copy(newsFilters = initialSelection) }
        fetchNews()
    }
    
    fun toggleNewsFilter(context: android.content.Context, url: String) {
        val current = _snapshotState.value.newsFilters
        val updated = current.map { if (it.url == url) it.copy(isSelected = !it.isSelected) else it }
        _snapshotState.update { it.copy(newsFilters = updated) }
        val selectedUrls = updated.filter { it.isSelected }.map { it.url }.toSet()
        com.dhruvathaide.gridly.data.local.Prefs.saveNewsFilters(context, selectedUrls)
        fetchNews()
    }

    private var fetchNewsJob: Job? = null

    private fun fetchNews() {
        fetchNewsJob?.cancel()
        fetchNewsJob = viewModelScope.launch {
            val selectedUrls = _snapshotState.value.newsFilters.filter { it.isSelected }.map { it.url }
            println("GridlyLog: Starting News Fetch for ${selectedUrls.size} sources...")
            
            if (selectedUrls.isNotEmpty()) {
                val news = try {
                    kotlinx.coroutines.withTimeout(8000L) {
                        F1ApiService.fetchRssNews(selectedUrls)
                    }
                } catch (e: Exception) {
                    println("GridlyLog: News Fetch Failed/Timed Out: ${e.message}")
                    e.printStackTrace()
                    emptyList()
                }
                
                if (news.isNotEmpty()) {
                    println("GridlyLog: News Fetch Success: ${news.size} items found.")
                    val uiNews = news.map { dto ->
                         com.dhruvathaide.gridly.data.MockDataProvider.NewsItem(
                            id = dto.link.hashCode(),
                            title = dto.title,
                            subtitle = dto.description,
                            timeAgo = "Today",
                            category = "F1 NEWS",
                            categoryColor = "FF0000",
                            url = dto.link
                         )
                    }
                    _snapshotState.update { it.copy(newsFeed = uiNews) }
                } else {
                    println("GridlyLog: News Fetch Empty or Failed (Zero valid items). Falling back to MOCK data.")
                    _snapshotState.update { it.copy(newsFeed = com.dhruvathaide.gridly.data.MockDataProvider.mockNews) }
                }
            } else {
                println("GridlyLog: No News Sources selected. Using MOCK data.")
                _snapshotState.update { it.copy(newsFeed = com.dhruvathaide.gridly.data.MockDataProvider.mockNews) }
            }
        }
    }

    private fun loadMockData() {
        val mockSession = com.dhruvathaide.gridly.data.MockDataProvider.mockSession
         _snapshotState.update { it.copy(
             activeSession = mockSession,
             availableDrivers = com.dhruvathaide.gridly.data.MockDataProvider.getDrivers(),
             newsFeed = com.dhruvathaide.gridly.data.MockDataProvider.mockNews,
             driverStandings = com.dhruvathaide.gridly.data.MockDataProvider.driverStandings,
             constructorStandings = com.dhruvathaide.gridly.data.MockDataProvider.constructorStandings,
             strategyStints = com.dhruvathaide.gridly.data.MockDataProvider.mockStints,
             teamRadioMessages = com.dhruvathaide.gridly.data.MockDataProvider.mockTeamRadio,
             driver1Telemetry = com.dhruvathaide.gridly.data.MockDataProvider.getMockTelemetry(1),
             driver2Telemetry = com.dhruvathaide.gridly.data.MockDataProvider.getMockTelemetry(2),
             battleModeTelemetryD1 = com.dhruvathaide.gridly.data.MockDataProvider.getMockTelemetry(1),
             battleModeTelemetryD2 = com.dhruvathaide.gridly.data.MockDataProvider.getMockTelemetry(2),
             d1Interval = "+0.0s",
             d2Interval = "+1.42s",
             d1TyreCompound = "SOFT (L12)",
             d2TyreCompound = "MEDIUM (L14)"
         ) }
         if (com.dhruvathaide.gridly.data.MockDataProvider.getDrivers().size >= 2) {
            selectDrivers(com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()[0], com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()[1])
         }
    }

    private fun loadDrivers(sessionKey: Int) {
        viewModelScope.launch {
            try {
                val drivers = F1ApiService.getDrivers(sessionKey)
                if (drivers.isEmpty()) {
                    val mockDrivers = com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()
                    _snapshotState.update { it.copy(availableDrivers = mockDrivers) }
                    selectDrivers(mockDrivers[4], mockDrivers[0])
                } else {
                    val uniqueDrivers = drivers.distinctBy { it.driverNumber }.sortedBy { it.driverNumber }
                    _snapshotState.update { it.copy(availableDrivers = uniqueDrivers) }
                    val allStints = F1ApiService.getStints(sessionKey)
                    _snapshotState.update { it.copy(strategyStints = allStints) }
                    if (uniqueDrivers.size >= 2) selectDrivers(uniqueDrivers[0], uniqueDrivers[1])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                 val mockDrivers = com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()
                 _snapshotState.update { it.copy(availableDrivers = mockDrivers) }
                 selectDrivers(mockDrivers[4], mockDrivers[0])
            }
        }
    }

    fun selectDrivers(d1: DriverDto, d2: DriverDto) {
        // Just update the snapshot. The `flatMapLatest` flow will pick this up and restart telemetry with new drivers.
        _snapshotState.update { it.copy(driver1 = d1, driver2 = d2) }
    }
    
    private fun startStrategyPolling() {
        // We use a separate job that checks the UI state (via snapshot) to see if it should fetch
        strategyJob?.cancel()
        strategyJob = viewModelScope.launch {
            while(true) {
                // Only poll if we have an active session and drivers selected, effectively syncing loosely with the UI flow
                val state = _snapshotState.value
                if (state.activeSession != null && state.driver1 != null && state.driver2 != null) {
                    updateStrategyData(state.activeSession.sessionKey, state.driver1.driverNumber, state.driver2.driverNumber)
                }
                delay(5000) // Staggered to 5s to reduce load
            }
        }
    }

    private suspend fun updateStrategyData(sessionKey: Int, d1Num: Int, d2Num: Int) {
        try {
            // Sequential calls to respect rate limiter without blocking everything at once
            val intervals = F1ApiService.getIntervals(sessionKey)
            
            // Allow breathing room
            // delay(100) 
            
            val stints = F1ApiService.getStints(sessionKey)
            
            val rc = F1ApiService.getRaceControl(sessionKey)
            
            val weatherList = F1ApiService.getWeather(sessionKey)

            // Validating Laps for Sectors using our new API method
            val laps1 = F1ApiService.getLaps(sessionKey, driverNumber = d1Num)
            val laps2 = F1ApiService.getLaps(sessionKey, driverNumber = d2Num)
            
            val lastLap1 = laps1.maxByOrNull { it.lapNumber }
            val lastLap2 = laps2.maxByOrNull { it.lapNumber }
            
            val d1Secs = lastLap1?.let { 
                 Triple(
                     it.durationSector1?.toString() ?: "-", 
                     it.durationSector2?.toString() ?: "-", 
                     it.durationSector3?.toString() ?: "-"
                 )
            }
            val d2Secs = lastLap2?.let { 
                 Triple(
                     it.durationSector1?.toString() ?: "-", 
                     it.durationSector2?.toString() ?: "-", 
                     it.durationSector3?.toString() ?: "-"
                 )
            }
            
             val i1 = intervals.find { it.driverNumber == d1Num }
            val i2 = intervals.find { it.driverNumber == d2Num }
            
            val s1 = stints.filter { it.driverNumber == d1Num }.maxByOrNull { it.lapStart ?: 0 }
            val s2 = stints.filter { it.driverNumber == d2Num }.maxByOrNull { it.lapStart ?: 0 }

            val latestFlag = rc.lastOrNull { it.category == "Flag" || it.category == "SafetyCar" }
            val latestWeather = weatherList.lastOrNull()
            // ... (rest of logic remains)
            
            // FIX: We should separate "Static Config" (Drivers/Session) from "Data Updates".
            // Since we merged them in one class, this is tricky.
            // But `flatMapLatest` only re-executes if the UPSTREAM emits.
            // If we update _snapshotState, it emits.
            // Use distinctUntilChanged { old, new -> old.activeSession == new.activeSession && old.driver1 == new.driver1 ... }
            // But we can't easily do partial distinct.
            
            // FOR NOW: We will update the _snapshotState, BUT we need to prevent the telemetry flow from resetting.
            // Better approach: `uiState` combines `_snapshotState` and `telemetry`.
            
            // Let's modify the `uiState` logic slightly to rely on distinctUntilChanged
            // See the fix in the class structure above (I will need to ensure I import distinctUntilChanged)
            
             _snapshotState.update { state ->
                state.copy(
                    d1Interval = i1?.interval?.let { "+${it}s" } ?: i1?.gapToLeader ?: state.d1Interval,
                    d2Interval = i2?.interval?.let { "+${it}s" } ?: i2?.gapToLeader ?: state.d2Interval,
                    d1TyreCompound = s1?.compound?.let { "$it (Lap ${s1.tyreAgeAtStart ?: 0})" } ?: state.d1TyreCompound,
                    d2TyreCompound = s2?.compound?.let { "$it (Lap ${s2.tyreAgeAtStart ?: 0})" } ?: state.d2TyreCompound,
                    raceControlMessage = latestFlag?.let { "${it.flag} (${it.message})" },
                    currentWeather = latestWeather,
                    rainProbability = latestWeather?.rainfall?.toDouble() ?: state.rainProbability,
                    d1Sectors = d1Secs,
                    d2Sectors = d2Secs
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateOvertake(state: TelemetryState): String {
        val d1 = state.driver1Data ?: return "N/A"
        val d2 = state.driver2Data ?: return "N/A"
        return if (d2.speed > d1.speed * 1.05) "CLOSING" else "HOLDING"
    }

    // --- Battle Mode Logic ---
    fun setBattleModeLap(lap: Int) {
        _snapshotState.update { it.copy(battleModeLap = lap) }
        loadBattleTelemetry()
    }
    
    fun setBattleModeDrivers(d1: DriverDto?, d2: DriverDto?) {
        _snapshotState.update { it.copy(battleModeDriver1 = d1, battleModeDriver2 = d2) }
        loadBattleTelemetry()
    }
    
    private fun loadBattleTelemetry() {
        val s = _snapshotState.value
        val sessionKey = s.activeSession?.sessionKey ?: return
        val d1 = s.battleModeDriver1 ?: s.driver1 ?: return
        val d2 = s.battleModeDriver2 ?: s.driver2 ?: return
        val lap = s.battleModeLap
        
        viewModelScope.launch {
            _snapshotState.update { it.copy(isBattleModeLoading = true) }
            try {
                val t1 = F1ApiService.getTelemetry(sessionKey, d1.driverNumber, lapNumber = lap)
                val t2 = F1ApiService.getTelemetry(sessionKey, d2.driverNumber, lapNumber = lap)
                _snapshotState.update { it.copy(
                    battleModeTelemetryD1 = t1,
                    battleModeTelemetryD2 = t2,
                    battleModeDriver1 = d1,
                    battleModeDriver2 = d2
                ) }
            } catch (e: Exception) { e.printStackTrace() }
            finally { _snapshotState.update { it.copy(isBattleModeLoading = false) } }
        }
    }
    
    // --- Team Radio Logic ---
    fun refreshTeamRadio(sessionKey: Int) {
        viewModelScope.launch {
            try {
                val radios = F1ApiService.getTeamRadio(sessionKey)
                val valid = radios.filter { it.recordingUrl.isNotEmpty() }.sortedByDescending { it.date }.take(50)
                _snapshotState.update { it.copy(teamRadioMessages = valid) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
