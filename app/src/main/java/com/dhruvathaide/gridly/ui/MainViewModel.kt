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
                val isProduction = com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.value
                
                if (isProduction) {
                    // REAL API MODE - STRICT 2026
                    // 1. Fetch 2026 Races
                    val sessions = F1ApiService.getSessions(year = 2026, sessionType = "Race")
                    
                    // 2. Find Current or Next Race
                    // logic: Find first session that hasn't finished yet (dateEnd > now)
                    // If all finished, take the last one (Post-season state)
                    val now = java.time.Instant.now()
                    
                    val relevantSession = sessions.firstOrNull { session ->
                         try {
                             val end = java.time.Instant.parse(session.dateEnd)
                             end.isAfter(now)
                         } catch (e: Exception) { false }
                    } ?: sessions.lastOrNull() // Fallback to last race if all are done
                    
                    if (relevantSession != null) {
                        try {
                            val start = java.time.Instant.parse(relevantSession.dateStart)
                            val end = java.time.Instant.parse(relevantSession.dateEnd)
                            
                            val isLive = now.isAfter(start) && now.isBefore(end)
                            
                            if (isLive) {
                                // LIVE RACE: Full Data
                                _uiState.update { it.copy(activeSession = relevantSession) }
                                loadDrivers(relevantSession.sessionKey)
                            } else {
                                // FUTURE or PAST RACE (Next up)
                                // Show on Home Screen, but CLEAR Dashboard Data
                                _uiState.update { it.copy(
                                    activeSession = relevantSession,
                                    availableDrivers = emptyList(), // Clear drivers so Dashboard shows empty/waiting
                                    driver1 = null,
                                    driver2 = null
                                ) }
                            }
                        } catch (e: Exception) {
                            // Date parse error, default to Future state behavior
                             _uiState.update { it.copy(activeSession = relevantSession, availableDrivers = emptyList()) }
                        }
                    } else {
                        // NO 2026 DATA
                         _uiState.update { it.copy(activeSession = null, availableDrivers = emptyList()) }
                    }
                    
                    // 3. Fetch Real News
                    fetchNews()
                    
                    // 4. Standings: Clear for 2026 (Since we are strict) or Fetch if API supported
                    // OpenF1 doesn't simplify standings easily without massive calculation or manual points summation.
                    // For this "Strict 2026" request, it's safer to show EMPTY than mock.
                    _uiState.update { it.copy(
                        driverStandings = emptyList(),
                        constructorStandings = emptyList()
                    ) }
                    
                } else {
                    // DEMO MODE (Forced Mock)
                     loadMockData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Error State
                val isProduction = com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.value
                if (!isProduction) loadMockData()
                else {
                    _uiState.update { it.copy(activeSession = null, availableDrivers = emptyList()) }
                    fetchNews() // Try fetch news even if session failed
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    // --- News Feeds ---
    data class FeedSource(val name: String, val url: String, val isSelected: Boolean = true)
    
    // Known Feeds
    private val allFeeds = listOf(
        FeedSource("Motorsport.com", "https://www.motorsport.com/rss/f1/news/"),
        FeedSource("Autosport", "https://www.autosport.com/rss/feed/f1"),
        FeedSource("BBC Sport", "https://feeds.bbci.co.uk/sport/formula1/rss.xml"),
        FeedSource("Crash.net", "https://www.crash.net/rss/f1"),
        FeedSource("GPFans", "https://www.gpfans.com/rss/f1-news.xml")
    )

    fun loadFilters(context: android.content.Context) {
        val saved = com.dhruvathaide.gridly.data.local.Prefs.getNewsFilters(context)
        
        // If no saved filters (first run), select all (or defaults)
        val initialSelection = if (saved.isEmpty()) {
            allFeeds.map { it.copy(isSelected = true) }
        } else {
            allFeeds.map { it.copy(isSelected = saved.contains(it.url)) }
        }
        
        _uiState.update { it.copy(newsFilters = initialSelection) }
        
        // Fetch news with these filters
        fetchNews()
    }
    
    fun toggleNewsFilter(context: android.content.Context, url: String) {
        val current = _uiState.value.newsFilters
        val updated = current.map { 
            if (it.url == url) it.copy(isSelected = !it.isSelected) else it 
        }
        
        _uiState.update { it.copy(newsFilters = updated) }
        
        // Save to Prefs
        val selectedUrls = updated.filter { it.isSelected }.map { it.url }.toSet()
        com.dhruvathaide.gridly.data.local.Prefs.saveNewsFilters(context, selectedUrls)
        
        // Refresh News
        fetchNews()
    }

    private fun fetchNews() {
        viewModelScope.launch {
            // Get selected URLs
            val selectedUrls = _uiState.value.newsFilters.filter { it.isSelected }.map { it.url }
            
            if (selectedUrls.isNotEmpty()) {
                val news = F1ApiService.fetchRssNews(selectedUrls)
                val uiNews = news.map { dto ->
                     com.dhruvathaide.gridly.data.MockDataProvider.NewsItem(
                        id = dto.link.hashCode(),
                        title = dto.title,
                        subtitle = dto.description, // Short description
                        timeAgo = "Today", // Simplified for now
                        category = "F1 NEWS",
                        categoryColor = "FF0000", // Red
                        url = dto.link
                     )
                }
                _uiState.update { it.copy(newsFeed = uiNews) }
            } else {
                _uiState.update { it.copy(newsFeed = emptyList()) }
            }
        }
    }
    
    private fun loadMockData() {
        // Use the specific Mock Session (Monaco) to ensure Track Map loads
        val mockSession = com.dhruvathaide.gridly.data.MockDataProvider.mockSession
         _uiState.update { it.copy(
             activeSession = mockSession,
             availableDrivers = com.dhruvathaide.gridly.data.MockDataProvider.getDrivers(),
             newsFeed = com.dhruvathaide.gridly.data.MockDataProvider.mockNews, // Load Mock News
             driverStandings = com.dhruvathaide.gridly.data.MockDataProvider.driverStandings, // Mock Standings
             constructorStandings = com.dhruvathaide.gridly.data.MockDataProvider.constructorStandings // Mock Standings
         ) }
         
         if (com.dhruvathaide.gridly.data.MockDataProvider.getDrivers().size >= 2) {
            selectDrivers(com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()[4], com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()[0]) // Max vs Lando
         }
    }

    private fun loadDrivers(sessionKey: Int) {
        viewModelScope.launch {
            try {
                // Fetch real drivers from API
                val drivers = F1ApiService.getDrivers(sessionKey)
                
                // If API returns empty (common for future seasons), use Mock
                if (drivers.isEmpty()) {
                    val mockDrivers = com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()
                    _uiState.update { it.copy(availableDrivers = mockDrivers) }
                    selectDrivers(mockDrivers[4], mockDrivers[0]) // Max vs Lando default
                } else {
                     // Filter duplicates
                    val uniqueDrivers = drivers.distinctBy { it.driverNumber }.sortedBy { it.driverNumber }
    
                    _uiState.update { it.copy(availableDrivers = uniqueDrivers) }
                    
                    // Fetch Strategy Data (Stints) for all drivers once
                    val allStints = F1ApiService.getStints(sessionKey)
                    _uiState.update { it.copy(strategyStints = allStints) }
                    
                    // Default selection
                    if (uniqueDrivers.size >= 2) {
                        selectDrivers(uniqueDrivers[0], uniqueDrivers[1])
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                 // Fallback to mock
                 val mockDrivers = com.dhruvathaide.gridly.data.MockDataProvider.getDrivers()
                 _uiState.update { it.copy(availableDrivers = mockDrivers) }
                 selectDrivers(mockDrivers[4], mockDrivers[0])
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
                        if (current1.size > 100) current1.removeAt(0)
                    }
                    
                    val current2 = _uiState.value.driver2Telemetry.toMutableList()
                    state.driver2Data?.let {
                        current2.add(it)
                        if (current2.size > 100) current2.removeAt(0)
                    }

                    // --- Real Data Polling (Simplified for Sync) ---
                    // Ideally this should be separate flows, but for the demo we'll fetch occasionally 
                    // or just once per loop if performance allows (likely need optimization later).
                    // For now, let's just fetch Stints/Intervals in a separate coroutine or "fire and forget" update 
                    // to avoid blocking the high-freq telemetry.
                    // Actually, let's just do a periodic check logic here or simplistic fetch.
                    
                    // We need to fetch Stints to find the active stint for the tyre compound.
                    // This is "heavy", so maybe only every 100th update or similar?
                    // Let's just fetch it for now, assuming the loop isn't too fast (telemetry buffer controls rate somewhat).
                    
                    // Fetch Intervals
                    var d1Int = _uiState.value.d1Interval
                    var d2Int = _uiState.value.d2Interval
                    
                    // Note: This is network intensive in a loop. In production, use a separate 5s polling timer.
                    // I will implement a "Throttle" simple logic: e.g. every 2 seconds roughly.
                    // But for this "One-Stop App" demo, let's just mock the 'fetch' call being periodic.
                    // I'll launch a separate coroutine for slow-data if not already running?
                    // No, let's keep it simple: Just update the UI state with existing, and I'll add a helper to update Slow Data.
                    
                    _uiState.update { 
                        it.copy(
                            driver1Telemetry = current1,
                            driver2Telemetry = current2,
                            // Overtake logic remains client-side physics for now
                            overtakePrediction = calculateOvertake(state), 
                            rainProbability = (Math.random() * 20) // Still mocked until Weather endpoint linked in separate poll
                        ) 
                    }
                }
        }
        
        // Start a separate "Slow Data" poller for Intervals/Tyres/Weather
        viewModelScope.launch {
            while(true) {
                updateStrategyData(sessionKey, d1Num, d2Num)
                delay(3000) // 3 seconds poll
            }
        }
    }

    private suspend fun updateStrategyData(sessionKey: Int, d1Num: Int, d2Num: Int) {
        try {
            // 1. Intervals
            val intervals = F1ApiService.getIntervals(sessionKey)
            val i1 = intervals.find { it.driverNumber == d1Num }
            val i2 = intervals.find { it.driverNumber == d2Num }
            
            // 2. Stints (Tyres)
            val stints = F1ApiService.getStints(sessionKey)
            val s1 = stints.filter { it.driverNumber == d1Num }.maxByOrNull { it.lapStart ?: 0 }
            val s2 = stints.filter { it.driverNumber == d2Num }.maxByOrNull { it.lapStart ?: 0 }

            // 3. Race Control (Flags)
            val rc = F1ApiService.getRaceControl(sessionKey)
            val latestFlag = rc.lastOrNull { it.category == "Flag" || it.category == "SafetyCar" }
            
            // 4. Weather
            // Get latest weather data
            val weatherList = F1ApiService.getWeather(sessionKey)
            val latestWeather = weatherList.lastOrNull()
            
            _uiState.update { state ->
                state.copy(
                    d1Interval = i1?.interval?.let { "+${it}s" } ?: i1?.gapToLeader ?: state.d1Interval,
                    d2Interval = i2?.interval?.let { "+${it}s" } ?: i2?.gapToLeader ?: state.d2Interval,
                    d1TyreCompound = s1?.compound?.let { "$it (Lap ${s1.tyreAgeAtStart ?: 0})" } ?: state.d1TyreCompound,
                    d2TyreCompound = s2?.compound?.let { "$it (Lap ${s2.tyreAgeAtStart ?: 0})" } ?: state.d2TyreCompound,
                    raceControlMessage = latestFlag?.let { "${it.flag} (${it.message})" },
                    currentWeather = latestWeather,
                    rainProbability = latestWeather?.rainfall?.toDouble() ?: state.rainProbability
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    // --- Battle Mode Logic ---
    
    fun setBattleModeLap(lap: Int) {
        _uiState.update { it.copy(battleModeLap = lap) }
        loadBattleTelemetry()
    }
    
    fun setBattleModeDrivers(d1: DriverDto?, d2: DriverDto?) {
        _uiState.update { it.copy(battleModeDriver1 = d1, battleModeDriver2 = d2) }
        loadBattleTelemetry()
    }
    
    private fun loadBattleTelemetry() {
        // Use current state to get parameters
        val s = _uiState.value
        val sessionKey = s.activeSession?.sessionKey ?: return
        // Default to main selected drivers if battle mode drivers are null
        val d1 = s.battleModeDriver1 ?: s.driver1 ?: return
        val d2 = s.battleModeDriver2 ?: s.driver2 ?: return
        val lap = s.battleModeLap
        
        viewModelScope.launch {
            _uiState.update { it.copy(isBattleModeLoading = true) }
            try {
                // Fetch full lap telemetry for both drivers
                val t1 = F1ApiService.getTelemetry(sessionKey, d1.driverNumber, lapNumber = lap)
                val t2 = F1ApiService.getTelemetry(sessionKey, d2.driverNumber, lapNumber = lap)
                
                _uiState.update { it.copy(
                    battleModeTelemetryD1 = t1,
                    battleModeTelemetryD2 = t2,
                    // Also update the drivers in case they were defaulted
                    battleModeDriver1 = d1,
                    battleModeDriver2 = d2
                ) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isBattleModeLoading = false) }
            }
        }
    }
    
    // --- Team Radio Logic ---
    fun refreshTeamRadio(sessionKey: Int) {
        viewModelScope.launch {
            try {
                // Fetch last 50 messages
                val radios = F1ApiService.getTeamRadio(sessionKey)
                // Filter for audio that exists
                val valid = radios.filter { it.recordingUrl.isNotEmpty() }.sortedByDescending { it.date }.take(50)
                _uiState.update { it.copy(teamRadioMessages = valid) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
