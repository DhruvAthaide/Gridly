package com.dhruvathaide.gridly.ui.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.data.remote.model.StintDto
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.DashboardUiState
import com.dhruvathaide.gridly.ui.components.PitWallCard
import com.dhruvathaide.gridly.ui.components.TechnicalEmptyState
import com.dhruvathaide.gridly.ui.theme.CyberCyan
import com.dhruvathaide.gridly.ui.theme.F1Red
import com.dhruvathaide.gridly.ui.theme.SafetyYellow

@Composable
fun StrategyTab(state: DashboardUiState) {
    // Group stints by driver
    val driverStints = remember(state.strategyStints, state.availableDrivers) {
        state.availableDrivers.map { driver ->
            driver to state.strategyStints.filter { it.driverNumber == driver.driverNumber }.sortedBy { it.stintNumber }
        }
    }

    val maxLaps = remember(state.strategyStints) {
        state.strategyStints.maxByOrNull { it.lapEnd ?: 0 }?.lapEnd ?: 78
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PitWallCard(title = "TYRE STRATEGY OVERVIEW", modifier = Modifier.fillMaxSize()) {
            if (state.availableDrivers.isEmpty() || state.strategyStints.isEmpty()) {
                TechnicalEmptyState(
                    message = "NO RACE STRATEGY",
                    subMessage = "AWAITING STINT DATA",
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            } else {
                // Header Row (Laps)
                Row(modifier = Modifier.fillMaxWidth().padding(start = 50.dp, bottom = 8.dp, top = 12.dp)) {
                    Text("START", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("LAP $maxLaps", color = Color.Gray, fontSize = 10.sp)
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(driverStints) { (driver, stints) ->
                        StrategyRow(driverName = driver.nameAcronym, stints = stints, maxLaps = maxLaps.toFloat())
                    }
                }
            }
        }
    }
}

@Composable
fun StrategyRow(driverName: String, stints: List<StintDto>, maxLaps: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp), // Slightly slimmer
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Driver Acronym
        Text(
            text = driverName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(50.dp)
        )

        // Stint Bar Container
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            stints.forEach { stint ->
                val start = stint.lapStart ?: 0
                val safeEnd = if (stint.lapEnd == null) maxLaps.toInt() else stint.lapEnd!!
                
                val duration = (safeEnd - start).coerceAtLeast(1)
                
                // Color Logic (Official Pirelli Colors)
                val color = when (stint.compound?.uppercase()) {
                    "SOFT" -> F1Red // Red
                    "MEDIUM" -> SafetyYellow // Yellow
                    "HARD" -> Color.White // White
                    "INTERMEDIATE" -> Color(0xFF4CAF50) // Green
                    "WET" -> Color(0xFF2196F3) // Blue
                    else -> Color.Gray
                }
                
                // Using weight proportional to laps
                Box(
                    modifier = Modifier
                        .weight(duration.toFloat())
                        .fillMaxHeight()
                        .background(color)
                        .border(1.dp, Color.Black)
                ) {
                    if (duration > 5) { // Only show label if space permits
                        Text(
                            text = stint.compound?.take(1) ?: "",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            // Filler if race not finished
            val totalCovered = stints.sumOf { ((it.lapEnd ?: maxLaps.toInt()) - (it.lapStart ?: 0)) }
            if (totalCovered < maxLaps) {
                Spacer(modifier = Modifier.weight((maxLaps - totalCovered).coerceAtLeast(0.1f))) // Ensure minimal weight > 0
            }
        }
    }
}
