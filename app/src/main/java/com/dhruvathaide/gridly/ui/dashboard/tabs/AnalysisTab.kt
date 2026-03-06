package com.dhruvathaide.gridly.ui.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.ui.DashboardUiState
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.components.*
import com.dhruvathaide.gridly.ui.theme.*

@Composable
fun AnalysisTab(
    viewModel: MainViewModel,
    state: DashboardUiState
) {
    val analysisData = state.analysisLaps
    val selectedDrivers = state.analysisSelectedDrivers

    // Trigger data load when tab becomes visible
    LaunchedEffect(state.activeSession) {
        state.activeSession?.let { viewModel.loadAnalysisData(it.sessionKey) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RACE ANALYSIS",
                color = CyberCyan,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )

            // Session info
            state.activeSession?.let { session ->
                Text(
                    text = "${session.circuitShortName.uppercase()} // ${session.sessionName.uppercase()}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Driver filter chips
        if (state.availableDrivers.isNotEmpty()) {
            DriverFilterChips(
                drivers = state.availableDrivers,
                selectedDriverNumbers = selectedDrivers,
                onToggle = { viewModel.toggleAnalysisDriver(it) }
            )
        }

        if (state.isAnalysisLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberCyan)
            }
        } else if (analysisData.isEmpty()) {
            TechnicalEmptyState(
                message = "NO ANALYSIS DATA",
                subMessage = "AWAITING SESSION LAP DATA",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        } else {
            // Build chart data from selected drivers
            val lapTimesData = selectedDrivers.mapNotNull { driverNum ->
                val driver = state.availableDrivers.find { it.driverNumber == driverNum } ?: return@mapNotNull null
                val laps = analysisData.filter { it.driverNumber == driverNum && it.lapDuration != null && !it.isPitOutLap }
                if (laps.isEmpty()) return@mapNotNull null

                DriverLapTimes(
                    driverAcronym = driver.nameAcronym,
                    driverNumber = driver.driverNumber,
                    teamColorHex = driver.teamColour,
                    lapTimes = laps.associate { it.lapNumber to it.lapDuration!! }
                )
            }

            // 1. LAP TIMES CHART
            PitWallCard(title = "LAP TIMES") {
                if (lapTimesData.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        LapTimesChart(
                            driversData = lapTimesData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text("Select drivers to compare", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // 2. POSITION CHANGES CHART
            val positionData = buildPositionData(analysisData, state)
            if (positionData.isNotEmpty()) {
                PitWallCard(title = "POSITION CHANGES") {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        PositionChart(
                            driversData = positionData.filter { it.driverNumber in selectedDrivers },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 3. TYRE DEGRADATION
            selectedDrivers.take(2).forEach { driverNum ->
                val driver = state.availableDrivers.find { it.driverNumber == driverNum } ?: return@forEach
                val tyreDegData = buildTyreDegData(driverNum, analysisData, state.strategyStints)
                if (tyreDegData.stints.isNotEmpty()) {
                    PitWallCard(title = "TYRE DEG // ${driver.nameAcronym}") {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            TyreDegChart(
                                driverData = tyreDegData,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 4. SECTOR COMPARISON TABLE
            PitWallCard(title = "BEST SECTORS") {
                SectorComparisonTable(analysisData, state)
            }

            // 5. SPEED TRAP
            val speedTrapData = buildSpeedTrapData(analysisData, state)
            if (speedTrapData.isNotEmpty()) {
                PitWallCard(title = "SPEED TRAP") {
                    speedTrapData.take(10).forEachIndexed { index, (acronym, speed) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. $acronym",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format("%.1f", speed)} km/h",
                                color = CyberCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (index < speedTrapData.size - 1) {
                            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun DriverFilterChips(
    drivers: List<com.dhruvathaide.gridly.data.remote.model.DriverDto>,
    selectedDriverNumbers: Set<Int>,
    onToggle: (Int) -> Unit
) {
    // Show as a horizontally wrapping flow
    Column {
        Text(
            text = "SELECTED: ${selectedDriverNumbers.size} DRIVERS",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Show in rows of 5
        drivers.chunked(5).forEach { rowDrivers ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                rowDrivers.forEach { driver ->
                    val isSelected = driver.driverNumber in selectedDriverNumbers
                    val teamColor = try {
                        Color(android.graphics.Color.parseColor("#${driver.teamColour}"))
                    } catch (e: Exception) { Color.Gray }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) teamColor.copy(alpha = 0.3f) else CarbonFiber)
                            .border(
                                1.dp,
                                if (isSelected) teamColor else BorderSubtle,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onToggle(driver.driverNumber) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = driver.nameAcronym,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectorComparisonTable(
    laps: List<com.dhruvathaide.gridly.data.remote.model.LapDto>,
    state: DashboardUiState
) {
    // Calculate best sectors per driver
    val driverBestSectors = state.availableDrivers.mapNotNull { driver ->
        val driverLaps = laps.filter { it.driverNumber == driver.driverNumber && !it.isPitOutLap }
        if (driverLaps.isEmpty()) return@mapNotNull null

        val bestS1 = driverLaps.mapNotNull { it.durationSector1 }.minOrNull()
        val bestS2 = driverLaps.mapNotNull { it.durationSector2 }.minOrNull()
        val bestS3 = driverLaps.mapNotNull { it.durationSector3 }.minOrNull()
        val bestLap = driverLaps.mapNotNull { it.lapDuration }.minOrNull()

        if (bestS1 == null && bestS2 == null && bestS3 == null) return@mapNotNull null

        Triple(driver, Triple(bestS1, bestS2, bestS3), bestLap)
    }.sortedBy { it.third ?: Double.MAX_VALUE }

    if (driverBestSectors.isEmpty()) {
        Text("No sector data available", color = TextSecondary, fontSize = 12.sp)
        return
    }

    // Find absolute best for each sector
    val absBestS1 = driverBestSectors.mapNotNull { it.second.first }.minOrNull()
    val absBestS2 = driverBestSectors.mapNotNull { it.second.second }.minOrNull()
    val absBestS3 = driverBestSectors.mapNotNull { it.second.third }.minOrNull()

    // Header
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("DRIVER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
        Text("S1", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("S2", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("S3", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("BEST", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }

    driverBestSectors.take(10).forEach { (driver, sectors, bestLap) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = driver.nameAcronym,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.2f)
            )
            SectorTimeText(sectors.first, absBestS1, Modifier.weight(1f))
            SectorTimeText(sectors.second, absBestS2, Modifier.weight(1f))
            SectorTimeText(sectors.third, absBestS3, Modifier.weight(1f))
            Text(
                text = bestLap?.let { String.format("%.3f", it) } ?: "-",
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.2f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
private fun SectorTimeText(time: Double?, bestTime: Double?, modifier: Modifier = Modifier) {
    val color = when {
        time == null -> TextSecondary
        bestTime != null && time == bestTime -> Color(0xFFAA00FF) // Purple for absolute best
        bestTime != null && time < bestTime + 0.1 -> Color(0xFF00E676) // Green for close
        else -> Color(0xFFFFD740) // Yellow for slower
    }
    Text(
        text = time?.let { String.format("%.3f", it) } ?: "-",
        color = color,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        modifier = modifier,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

private fun buildPositionData(
    laps: List<com.dhruvathaide.gridly.data.remote.model.LapDto>,
    state: DashboardUiState
): List<DriverPositionData> {
    // Calculate position from lap times at each lap
    val allLapNumbers = laps.map { it.lapNumber }.distinct().sorted()
    if (allLapNumbers.isEmpty()) return emptyList()

    // Build cumulative times per driver
    val driverCumulativeTimes = mutableMapOf<Int, MutableMap<Int, Double>>()
    laps.filter { it.lapDuration != null }.groupBy { it.driverNumber }.forEach { (driverNum, driverLaps) ->
        var cumulative = 0.0
        driverLaps.sortedBy { it.lapNumber }.forEach { lap ->
            cumulative += lap.lapDuration!!
            driverCumulativeTimes.getOrPut(driverNum) { mutableMapOf() }[lap.lapNumber] = cumulative
        }
    }

    // Determine position at each lap
    val driverPositions = mutableMapOf<Int, MutableMap<Int, Int>>()
    allLapNumbers.forEach { lapNum ->
        val driversAtLap = driverCumulativeTimes
            .filter { it.value.containsKey(lapNum) }
            .map { (driverNum, times) -> Pair(driverNum, times[lapNum]!!) }
            .sortedBy { it.second }

        driversAtLap.forEachIndexed { index, (driverNum, _) ->
            driverPositions.getOrPut(driverNum) { mutableMapOf() }[lapNum] = index + 1
        }
    }

    return driverPositions.mapNotNull { (driverNum, positions) ->
        val driver = state.availableDrivers.find { it.driverNumber == driverNum } ?: return@mapNotNull null
        DriverPositionData(
            driverAcronym = driver.nameAcronym,
            driverNumber = driverNum,
            teamColorHex = driver.teamColour,
            positions = positions
        )
    }
}

private fun buildTyreDegData(
    driverNumber: Int,
    laps: List<com.dhruvathaide.gridly.data.remote.model.LapDto>,
    stints: List<com.dhruvathaide.gridly.data.remote.model.StintDto>
): DriverTyreDeg {
    val driverLaps = laps.filter { it.driverNumber == driverNumber && it.lapDuration != null && !it.isPitOutLap }
    val driverStints = stints.filter { it.driverNumber == driverNumber }
    val driver = driverLaps.firstOrNull()

    val stintLapTimes = driverLaps.mapNotNull { lap ->
        val stint = driverStints.find { s ->
            val start = s.lapStart ?: return@find false
            val end = s.lapEnd ?: return@find false
            lap.lapNumber in start..end
        } ?: return@mapNotNull null

        StintLapTime(
            compound = stint.compound ?: "UNKNOWN",
            lapNumber = lap.lapNumber,
            tyreAge = lap.lapNumber - (stint.lapStart ?: lap.lapNumber) + (stint.tyreAgeAtStart ?: 0),
            lapTime = lap.lapDuration!!
        )
    }

    return DriverTyreDeg(
        driverAcronym = "",
        stints = stintLapTimes
    )
}

private fun buildSpeedTrapData(
    laps: List<com.dhruvathaide.gridly.data.remote.model.LapDto>,
    state: DashboardUiState
): List<Pair<String, Double>> {
    return laps
        .filter { it.speedTrap != null && it.speedTrap > 0 }
        .groupBy { it.driverNumber }
        .mapNotNull { (driverNum, driverLaps) ->
            val driver = state.availableDrivers.find { it.driverNumber == driverNum } ?: return@mapNotNull null
            val maxSpeed = driverLaps.mapNotNull { it.speedTrap }.maxOrNull() ?: return@mapNotNull null
            Pair(driver.nameAcronym, maxSpeed)
        }
        .sortedByDescending { it.second }
}
