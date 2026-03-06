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
import java.time.Instant
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class DashboardUiState(
    val driver1: DriverDto? = null,
    val driver2: DriverDto? = null,
    val driver1Telemetry: List<TelemetryDto> = emptyList(),
    val driver2Telemetry: List<TelemetryDto> = emptyList(),
    val activeSession: SessionDto? = null,
    val raceSession: SessionDto? = null,
    val meetingSessions: List<SessionDto> = emptyList(),
    val currentWeather: com.dhruvathaide.gridly.data.remote.model.WeatherDto? = null,
    val rainProbability: Double = 0.0,
    val overtakePrediction: String = "N/A",
    val pitWindowStatus: String = "CLOSED",
    val availableDrivers: List<DriverDto> = emptyList(),
    val d1TyreCompound: String = "-",
    val d2TyreCompound: String = "-",
    val d1Interval: String = "-",
    val d2Interval: String = "-",
    val d1Sectors: Triple<String, String, String>? = null,
    val d2Sectors: Triple<String, String, String>? = null,
    val d1TyreLife: Int = 0,
    val d2TyreLife: Int = 0,
    val d1PitStops: Int = 0,
    val d2PitStops: Int = 0,
    val gapHistory: List<Float> = emptyList(),
    val raceControlMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isError: Boolean = false,
    val newsFeed: List<com.dhruvathaide.gridly.data.MockDataProvider.NewsItem> = emptyList(),
    val newsFilters: List<MainViewModel.FeedSource> = emptyList(),
    val driverStandings: List<com.dhruvathaide.gridly.data.MockDataProvider.DriverStanding> = emptyList(),
    val constructorStandings: List<com.dhruvathaide.gridly.data.MockDataProvider.ConstructorStanding> = emptyList(),
    val battleModeLap: Int = 1,
    val maxLaps: Int = 70,
    val battleModeDriver1: DriverDto? = null,
    val battleModeDriver2: DriverDto? = null,
    val battleModeTelemetryD1: List<TelemetryDto> = emptyList(),
    val battleModeTelemetryD2: List<TelemetryDto> = emptyList(),
    val isBattleModeLoading: Boolean = false,
    val teamRadioMessages: List<com.dhruvathaide.gridly.data.remote.model.TeamRadioDto> = emptyList(),
    val strategyStints: List<com.dhruvathaide.gridly.data.remote.model.StintDto> = emptyList(),
    // Analysis tab
    val analysisLaps: List<LapDto> = emptyList(),
    val analysisSelectedDrivers: Set<Int> = emptySet(),
    val isAnalysisLoading: Boolean = false,
    // Session history
    val seasonSessions: List<SessionDto> = emptyList(),
    val selectedHistorySession: SessionDto? = null
)

class MainViewModel : ViewModel() {

    private val repository = TelemetryRepository(F1ApiService)
    private val strategyEngine = StrategyEngine()

    private val _snapshotState = MutableStateFlow(DashboardUiState())

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
            val current1 = state.driver1Telemetry.toMutableList().apply {
                telem.driver1Data?.let { add(it); if (size > 100) removeAt(0) }
            }
            val current2 = state.driver2Telemetry.toMutableList().apply {
                telem.driver2Data?.let { add(it); if (size > 100) removeAt(0) }
            }

            // Build gap history from interval data
            val newGapHistory = state.gapHistory.toMutableList()
            val d1Speed = telem.driver1Data?.speed
            val d2Speed = telem.driver2Data?.speed
            if (d1Speed != null && d2Speed != null) {
                val gap = (d1Speed - d2Speed).toFloat() / 10f
                newGapHistory.add(gap)
                if (newGapHistory.size > 50) newGapHistory.removeAt(0)
            }

            state.copy(
                driver1Telemetry = current1,
                driver2Telemetry = current2,
                gapHistory = newGapHistory,
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
        _snapshotState.update { it.copy(newsFilters = allFeeds.map { f -> f.copy(isSelected = true) }) }
        fetchLatestSession()
        startStrategyPolling()

        // React to production mode changes
        viewModelScope.launch {
            com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.collectLatest {
                fetchLatestSession()
            }
        }
    }

    fun retry() {
        _snapshotState.update { it.copy(isError = false, errorMessage = null, isLoading = true) }
        fetchLatestSession()
        val state = _snapshotState.value
        if (state.activeSession != null && state.driver1 != null && state.driver2 != null) {
            viewModelScope.launch {
                updateStrategyData(state.activeSession.sessionKey, state.driver1.driverNumber, state.driver2.driverNumber)
            }
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
                    val now = Instant.now()

                    // Step 1: Get Race sessions to identify the relevant race weekend
                    var raceSessions = F1ApiService.getSessions(year = currentYear, sessionType = "Race")
                    if (raceSessions.isEmpty()) {
                        raceSessions = F1ApiService.getSessions(year = currentYear - 1, sessionType = "Race")
                    }

                    // Find the current or next upcoming race
                    val relevantRace = raceSessions.firstOrNull { session ->
                        try {
                            val end = Instant.parse(session.dateEnd)
                            end.isAfter(now)
                        } catch (e: Exception) { false }
                    } ?: raceSessions.lastOrNull()

                    if (relevantRace != null) {
                        // Step 2: Fetch ALL sessions for this meeting (FP1, FP2, FP3, Quali, Sprint, Race)
                        val allMeetingSessions = F1ApiService.getSessions(year = relevantRace.year)
                            .filter { it.meetingKey == relevantRace.meetingKey }
                            .sortedBy { it.dateStart }

                        // Step 3: Find whichever session is currently LIVE or next UPCOMING
                        val liveSession = allMeetingSessions.firstOrNull { session ->
                            try {
                                val start = Instant.parse(session.dateStart)
                                val end = Instant.parse(session.dateEnd)
                                now.isAfter(start) && now.isBefore(end)
                            } catch (e: Exception) { false }
                        }

                        val nextUpcoming = allMeetingSessions.firstOrNull { session ->
                            try {
                                val start = Instant.parse(session.dateStart)
                                start.isAfter(now)
                            } catch (e: Exception) { false }
                        }

                        // Priority: live session > next upcoming session > race session
                        val activeSession = liveSession ?: nextUpcoming ?: relevantRace

                        _snapshotState.update {
                            it.copy(
                                activeSession = activeSession,
                                raceSession = relevantRace,
                                meetingSessions = allMeetingSessions
                            )
                        }

                        loadDrivers(activeSession.sessionKey)
                        buildStandingsFromResults(raceSessions)
                    } else {
                        _snapshotState.update {
                            it.copy(activeSession = null, raceSession = null, availableDrivers = emptyList())
                        }
                    }

                    fetchNews()

                } else {
                    loadMockData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val isProduction = com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.value
                if (!isProduction) loadMockData()
                else {
                    _snapshotState.update {
                        it.copy(
                            activeSession = null,
                            raceSession = null,
                            availableDrivers = emptyList(),
                            isError = true,
                            errorMessage = "Failed to load session data: ${e.localizedMessage}"
                        )
                    }
                    fetchNews()
                }
            } finally {
                _snapshotState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Build driver and constructor standings from race result positions.
     * OpenF1 doesn't have a championship standings endpoint, so we use
     * the /position endpoint from the last completed race to show latest results.
     * For true championship standings, we show the mock data as fallback.
     */
    private fun buildStandingsFromResults(raceSessions: List<SessionDto>) {
        viewModelScope.launch {
            try {
                val now = Instant.now()
                val completedRaces = raceSessions.filter { session ->
                    try {
                        Instant.parse(session.dateEnd).isBefore(now)
                    } catch (e: Exception) { false }
                }

                val lastRace = completedRaces.lastOrNull() ?: return@launch

                // Get drivers for the last session
                val drivers = F1ApiService.getDrivers(lastRace.sessionKey)
                if (drivers.isEmpty()) return@launch

                val uniqueDrivers = drivers.distinctBy { it.driverNumber }

                // Get final laps to determine finishing order
                val laps = F1ApiService.getLaps(lastRace.sessionKey)
                val lastLapByDriver = laps.groupBy { it.driverNumber }
                    .mapValues { (_, laps) -> laps.maxByOrNull { it.lapNumber } }

                // Build standings from lap count (more laps = higher finish) and lap times
                val driverStandings = uniqueDrivers.mapNotNull { driver ->
                    val lastLap = lastLapByDriver[driver.driverNumber]
                    val maxLapNumber = lastLap?.lapNumber ?: 0
                    Pair(driver, maxLapNumber)
                }
                .sortedByDescending { it.second }
                .mapIndexed { index, (driver, _) ->
                    // Approximate points from single race result
                    val points = when (index) {
                        0 -> 25; 1 -> 18; 2 -> 15; 3 -> 12; 4 -> 10
                        5 -> 8; 6 -> 6; 7 -> 4; 8 -> 2; 9 -> 1
                        else -> 0
                    }
                    com.dhruvathaide.gridly.data.MockDataProvider.DriverStanding(
                        position = index + 1,
                        driver = driver,
                        points = points,
                        wins = if (index == 0) 1 else 0,
                        podiums = if (index < 3) 1 else 0,
                        recentForm = listOf("${index + 1}")
                    )
                }

                // Build constructor standings
                val constructorStandings = driverStandings
                    .groupBy { it.driver.teamName }
                    .map { (teamName, standings) ->
                        val teamPoints = standings.sumOf { it.points }
                        val teamColor = standings.firstOrNull()?.driver?.teamColour ?: "FFFFFF"
                        val driverAcronyms = standings.map { it.driver.nameAcronym }.take(2)
                        com.dhruvathaide.gridly.data.MockDataProvider.ConstructorStanding(
                            position = 0,
                            teamName = teamName,
                            teamColour = teamColor,
                            points = teamPoints,
                            driver1 = driverAcronyms.getOrElse(0) { "" },
                            driver2 = driverAcronyms.getOrElse(1) { "" }
                        )
                    }
                    .sortedByDescending { it.points }
                    .mapIndexed { index, cs ->
                        cs.copy(position = index + 1)
                    }

                _snapshotState.update {
                    it.copy(
                        driverStandings = driverStandings,
                        constructorStandings = constructorStandings
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Standings are non-critical; don't show error for this
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

            if (selectedUrls.isNotEmpty()) {
                val news = try {
                    kotlinx.coroutines.withTimeout(12000L) {
                        F1ApiService.fetchRssNews(selectedUrls)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }

                if (news.isNotEmpty()) {
                    val uiNews = news.map { dto ->
                        com.dhruvathaide.gridly.data.MockDataProvider.NewsItem(
                            id = dto.link.hashCode(),
                            title = dto.title,
                            subtitle = dto.description,
                            timeAgo = formatTimeAgo(dto.pubDate),
                            category = "F1 NEWS",
                            categoryColor = "FF0000",
                            url = dto.link
                        )
                    }
                    _snapshotState.update { it.copy(newsFeed = uiNews) }
                } else {
                    _snapshotState.update { it.copy(newsFeed = com.dhruvathaide.gridly.data.MockDataProvider.mockNews) }
                }
            } else {
                _snapshotState.update { it.copy(newsFeed = com.dhruvathaide.gridly.data.MockDataProvider.mockNews) }
            }
        }
    }

    private fun formatTimeAgo(pubDate: String): String {
        return try {
            val formats = listOf(
                DateTimeFormatter.RFC_1123_DATE_TIME,
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.ENGLISH)
            )
            var parsed: ZonedDateTime? = null
            for (fmt in formats) {
                try {
                    parsed = ZonedDateTime.parse(pubDate, fmt)
                    break
                } catch (_: Exception) {}
            }
            if (parsed == null) return "Now"

            val duration = Duration.between(parsed.toInstant(), Instant.now())
            val minutes = duration.toMinutes()
            when {
                minutes < 1 -> "Now"
                minutes < 60 -> "${minutes}m ago"
                minutes < 1440 -> "${minutes / 60}h ago"
                minutes < 10080 -> "${minutes / 1440}d ago"
                else -> "${minutes / 10080}w ago"
            }
        } catch (e: Exception) {
            "Now"
        }
    }

    private fun loadMockData() {
        val mockSession = com.dhruvathaide.gridly.data.MockDataProvider.mockSession
        _snapshotState.update {
            it.copy(
                activeSession = mockSession,
                raceSession = mockSession,
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
            )
        }
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
                    if (mockDrivers.size >= 2) selectDrivers(mockDrivers[0], mockDrivers[1])
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
                if (mockDrivers.size >= 2) selectDrivers(mockDrivers[0], mockDrivers[1])
            }
        }
    }

    fun selectDrivers(d1: DriverDto, d2: DriverDto) {
        _snapshotState.update { it.copy(driver1 = d1, driver2 = d2) }
    }

    private fun startStrategyPolling() {
        strategyJob?.cancel()
        strategyJob = viewModelScope.launch {
            while (true) {
                val state = _snapshotState.value
                if (state.activeSession != null && state.driver1 != null && state.driver2 != null) {
                    val isProduction = com.dhruvathaide.gridly.ui.theme.ThemeManager.isProductionMode.value
                    if (isProduction) {
                        updateStrategyData(state.activeSession.sessionKey, state.driver1.driverNumber, state.driver2.driverNumber)
                    }
                }
                delay(10000) // Poll every 10s instead of 5s to reduce API load
            }
        }
    }

    private suspend fun updateStrategyData(sessionKey: Int, d1Num: Int, d2Num: Int) {
        try {
            // Batch 1: Intervals + Stints (most important for live display)
            val intervals = F1ApiService.getIntervals(sessionKey)
            val stints = F1ApiService.getStints(sessionKey)

            // Batch 2: Race control + Weather (secondary importance)
            val rc = F1ApiService.getRaceControl(sessionKey)
            val weatherList = F1ApiService.getWeather(sessionKey)

            // Batch 3: Lap data for sectors (only fetch latest lap per driver)
            val laps1 = F1ApiService.getLaps(sessionKey, driverNumber = d1Num)
            val laps2 = F1ApiService.getLaps(sessionKey, driverNumber = d2Num)

            val lastLap1 = laps1.maxByOrNull { it.lapNumber }
            val lastLap2 = laps2.maxByOrNull { it.lapNumber }

            val d1Secs = lastLap1?.let {
                Triple(
                    formatSectorTime(it.durationSector1),
                    formatSectorTime(it.durationSector2),
                    formatSectorTime(it.durationSector3)
                )
            }
            val d2Secs = lastLap2?.let {
                Triple(
                    formatSectorTime(it.durationSector1),
                    formatSectorTime(it.durationSector2),
                    formatSectorTime(it.durationSector3)
                )
            }

            val i1 = intervals.filter { it.driverNumber == d1Num }.maxByOrNull { it.date }
            val i2 = intervals.filter { it.driverNumber == d2Num }.maxByOrNull { it.date }

            val d1Stints = stints.filter { it.driverNumber == d1Num }
            val d2Stints = stints.filter { it.driverNumber == d2Num }
            val s1 = d1Stints.maxByOrNull { it.lapStart ?: 0 }
            val s2 = d2Stints.maxByOrNull { it.lapStart ?: 0 }

            // Calculate tyre life from current stint
            val currentLap1 = lastLap1?.lapNumber ?: 0
            val currentLap2 = lastLap2?.lapNumber ?: 0
            val d1TyreAge = if (s1 != null) {
                val stintStart = s1.lapStart ?: 0
                val tyreAge = s1.tyreAgeAtStart ?: 0
                (currentLap1 - stintStart) + tyreAge
            } else 0
            val d2TyreAge = if (s2 != null) {
                val stintStart = s2.lapStart ?: 0
                val tyreAge = s2.tyreAgeAtStart ?: 0
                (currentLap2 - stintStart) + tyreAge
            } else 0

            // Pit stop count = number of stints - 1
            val d1Pits = (d1Stints.size - 1).coerceAtLeast(0)
            val d2Pits = (d2Stints.size - 1).coerceAtLeast(0)

            val latestFlag = rc.lastOrNull { it.category == "Flag" || it.category == "SafetyCar" }
            val latestWeather = weatherList.lastOrNull()

            // Determine max laps from lap data
            val maxLapNum = maxOf(currentLap1, currentLap2, _snapshotState.value.maxLaps)

            _snapshotState.update { state ->
                state.copy(
                    d1Interval = formatInterval(i1?.interval, i1?.gapToLeader, state.d1Interval),
                    d2Interval = formatInterval(i2?.interval, i2?.gapToLeader, state.d2Interval),
                    d1TyreCompound = s1?.compound?.uppercase() ?: state.d1TyreCompound,
                    d2TyreCompound = s2?.compound?.uppercase() ?: state.d2TyreCompound,
                    d1TyreLife = d1TyreAge,
                    d2TyreLife = d2TyreAge,
                    d1PitStops = d1Pits,
                    d2PitStops = d2Pits,
                    raceControlMessage = latestFlag?.let { "${it.flag ?: ""} ${it.message ?: ""}".trim() },
                    currentWeather = latestWeather,
                    rainProbability = latestWeather?.rainfall?.toDouble() ?: state.rainProbability,
                    d1Sectors = d1Secs,
                    d2Sectors = d2Secs,
                    strategyStints = stints,
                    maxLaps = maxLapNum,
                    isError = false,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Non-blocking error - only show if we have no data at all
            val state = _snapshotState.value
            if (state.d1Interval == "-" && state.d2Interval == "-") {
                _snapshotState.update { it.copy(isError = true, errorMessage = "Connection unstable: ${e.message}") }
            }
        }
    }

    private fun formatSectorTime(seconds: Double?): String {
        if (seconds == null) return "-"
        return String.format("%.3f", seconds)
    }

    private fun formatInterval(interval: String?, gapToLeader: String?, fallback: String): String {
        return when {
            interval != null && interval.isNotBlank() -> "+${interval}s"
            gapToLeader != null && gapToLeader.isNotBlank() -> gapToLeader
            else -> fallback
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
                _snapshotState.update {
                    it.copy(
                        battleModeTelemetryD1 = t1,
                        battleModeTelemetryD2 = t2,
                        battleModeDriver1 = d1,
                        battleModeDriver2 = d2
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _snapshotState.update { it.copy(isBattleModeLoading = false) }
            }
        }
    }

    // --- Analysis Tab ---
    fun loadAnalysisData(sessionKey: Int) {
        viewModelScope.launch {
            val currentData = _snapshotState.value.analysisLaps
            if (currentData.isNotEmpty() && currentData.firstOrNull()?.sessionKey == sessionKey) return@launch

            _snapshotState.update { it.copy(isAnalysisLoading = true) }
            try {
                val laps = F1ApiService.getLaps(sessionKey)
                val validLaps = laps.filter { it.lapDuration != null && it.lapDuration > 0 }

                // Auto-select top 5 drivers by most laps
                val topDrivers = validLaps.groupBy { it.driverNumber }
                    .entries
                    .sortedByDescending { it.value.size }
                    .take(5)
                    .map { it.key }
                    .toSet()

                _snapshotState.update {
                    it.copy(
                        analysisLaps = validLaps,
                        analysisSelectedDrivers = if (it.analysisSelectedDrivers.isEmpty()) topDrivers else it.analysisSelectedDrivers,
                        isAnalysisLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _snapshotState.update { it.copy(isAnalysisLoading = false) }
            }
        }
    }

    fun toggleAnalysisDriver(driverNumber: Int) {
        _snapshotState.update { state ->
            val current = state.analysisSelectedDrivers.toMutableSet()
            if (current.contains(driverNumber)) {
                current.remove(driverNumber)
            } else {
                if (current.size < 6) current.add(driverNumber) // Max 6 for readability
            }
            state.copy(analysisSelectedDrivers = current)
        }
    }

    // --- Session History ---
    fun loadSeasonSessions() {
        viewModelScope.launch {
            try {
                val currentYear = java.time.Year.now().value
                var sessions = F1ApiService.getSessions(year = currentYear)
                if (sessions.isEmpty()) {
                    sessions = F1ApiService.getSessions(year = currentYear - 1)
                }
                _snapshotState.update { it.copy(seasonSessions = sessions) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectHistorySession(session: SessionDto) {
        _snapshotState.update { it.copy(selectedHistorySession = session) }
        viewModelScope.launch {
            // Load drivers and analysis data for selected session
            try {
                val drivers = F1ApiService.getDrivers(session.sessionKey)
                val uniqueDrivers = drivers.distinctBy { it.driverNumber }.sortedBy { it.driverNumber }
                val stints = F1ApiService.getStints(session.sessionKey)
                _snapshotState.update {
                    it.copy(
                        availableDrivers = uniqueDrivers,
                        strategyStints = stints,
                        analysisLaps = emptyList(),
                        analysisSelectedDrivers = emptySet()
                    )
                }
                loadAnalysisData(session.sessionKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Team Radio Logic ---
    fun refreshTeamRadio(sessionKey: Int) {
        viewModelScope.launch {
            try {
                val radios = F1ApiService.getTeamRadio(sessionKey)
                val valid = radios.filter { it.recordingUrl.isNotEmpty() }.sortedByDescending { it.date }.take(50)
                _snapshotState.update { it.copy(teamRadioMessages = valid) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
