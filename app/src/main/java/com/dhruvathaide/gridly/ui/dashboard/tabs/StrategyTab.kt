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

@Composable
fun StrategyTab(state: DashboardUiState) { // Updated signature to match DashboardFragment call
    // Group stints by driver
    val driverStints = remember(state.strategyStints, state.availableDrivers) {
        state.availableDrivers.map { driver ->
            driver to state.strategyStints.filter { it.driverNumber == driver.driverNumber }.sortedBy { it.stintNumber }
        }
    }

    // Determine max laps for proper scaling (e.g. 50, 60, 70...)
    // If we have stints, check max lap_end. If not, use 78 (Monaco) or default.
    val maxLaps = remember(state.strategyStints) {
        state.strategyStints.maxByOrNull { it.lapEnd ?: 0 }?.lapEnd ?: 70
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "TYRE STRATEGY OVERVIEW",
            color = Color(0xFF00E5FF),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            fontFamily = FontFamily.Monospace
        )

        if (state.availableDrivers.isEmpty() || state.strategyStints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO STRATEGY DATA",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "WAITING FOR ACTIVE SESSION",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            // Header Row (Laps)
            Row(modifier = Modifier.fillMaxWidth().padding(start = 50.dp, bottom = 8.dp)) {
                Text("START", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text("LAP $maxLaps", color = Color.Gray, fontSize = 10.sp)
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(driverStints) { (driver, stints) ->
                    StrategyRow(driverName = driver.nameAcronym, stints = stints, maxLaps = maxLaps.toFloat())
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
            .height(40.dp),
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
                val end = stint.lapEnd ?: start 
                // If end is missing/current, assume it goes until now or max?
                // For live stints, lapEnd might be null. We should visually extend it or use current lap.
                // Let's assume passed maxLaps is reasonable current cap if end is null.
                val safeEnd = if (stint.lapEnd == null) maxLaps.toInt() else stint.lapEnd
                
                val duration = (safeEnd - start).coerceAtLeast(1)
                
                // Weight based on duration
                // We use weight for proportional width.
                // Note: Compose Row weight works best if all children have weights summing up to total.
                // But typically there are gaps or offsets.
                // Simpler: Just Box with weight = duration / total.
                // But we need to handle "gap" before first stint? (Usually starts at 0).
                // Assuming start is continuous for F1 usually.
                
                // Color Logic
                val color = when (stint.compound?.uppercase()) {
                    "SOFT" -> Color(0xFFFF5252)
                    "MEDIUM" -> Color(0xFFFFEB3B)
                    "HARD" -> Color.White
                    "INTERMEDIATE" -> Color(0xFF4CAF50)
                    "WET" -> Color(0xFF2196F3)
                    else -> Color.Gray
                }
                
                Box(
                    modifier = Modifier
                        .weight(duration.toFloat())
                        .fillMaxHeight()
                        .background(color)
                        .border(1.dp, Color.Black)
                ) {
                    if (duration > 5) { // Only show text if stint is long enough
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
            // Calculate total laps covered
            val totalCovered = stints.sumOf { ((it.lapEnd ?: maxLaps.toInt()) - (it.lapStart ?: 0)) }
            if (totalCovered < maxLaps) {
                Spacer(modifier = Modifier.weight((maxLaps - totalCovered).coerceAtLeast(0f)))
            }
        }
    }
}
