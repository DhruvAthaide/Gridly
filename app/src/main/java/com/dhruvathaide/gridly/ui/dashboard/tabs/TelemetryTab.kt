package com.dhruvathaide.gridly.ui.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.DashboardUiState
import com.dhruvathaide.gridly.ui.components.DriverSelectionDialog
import com.dhruvathaide.gridly.ui.components.SpeedTraceChart
import com.dhruvathaide.gridly.ui.components.PitWallCard
import com.dhruvathaide.gridly.ui.components.TechnicalEmptyState
import com.dhruvathaide.gridly.ui.theme.CyberCyan
import com.dhruvathaide.gridly.ui.theme.F1Red

@Composable
fun TelemetryTab(
    viewModel: MainViewModel,
    state: DashboardUiState
) {
    // Initialize Battle Mode
    LaunchedEffect(Unit) {
        if (state.battleModeDriver1 == null && state.driver1 != null) {
            viewModel.setBattleModeDrivers(state.driver1, state.driver2)
        }
    }

    // Dialog State
    val (showD1Dialog, setShowD1Dialog) = remember { mutableStateOf(false) }
    val (showD2Dialog, setShowD2Dialog) = remember { mutableStateOf(false) }

    if (showD1Dialog) {
        DriverSelectionDialog(
            drivers = state.availableDrivers,
            onDriverSelected = {
                viewModel.setBattleModeDrivers(it, state.battleModeDriver2)
                setShowD1Dialog(false)
            },
            onDismissRequest = { setShowD1Dialog(false) }
        )
    }

    if (showD2Dialog) {
         DriverSelectionDialog(
            drivers = state.availableDrivers,
            onDriverSelected = {
                viewModel.setBattleModeDrivers(state.battleModeDriver1, it)
                setShowD2Dialog(false)
            },
            onDismissRequest = { setShowD2Dialog(false) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Controls / Header ---
        PitWallCard(title = "TELEMETRY CONTROLS") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lap Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (state.battleModeLap > 1) viewModel.setBattleModeLap(state.battleModeLap - 1)
                    }) {
                        Text("<", color = if(state.availableDrivers.isNotEmpty()) Color.Gray else Color.DarkGray, fontSize = 24.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LAP", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = "${state.battleModeLap}",
                            color = if(state.availableDrivers.isNotEmpty()) CyberCyan else Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    IconButton(onClick = {
                        if (state.battleModeLap < state.maxLaps) viewModel.setBattleModeLap(state.battleModeLap + 1)
                    }) {
                        Text(">", color = if(state.availableDrivers.isNotEmpty()) Color.Gray else Color.DarkGray, fontSize = 24.sp)
                    }
                }

                // Driver Comparison (Clickable)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver 1 Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if(state.availableDrivers.isNotEmpty()) CyberCyan.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha=0.1f))
                            .border(1.dp, if(state.availableDrivers.isNotEmpty()) CyberCyan else Color.Gray, RoundedCornerShape(4.dp))
                            .clickable(enabled = state.availableDrivers.isNotEmpty()) { setShowD1Dialog(true) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = state.battleModeDriver1?.nameAcronym ?: "D1",
                            color = if(state.availableDrivers.isNotEmpty()) CyberCyan else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text("VS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Black)

                    // Driver 2 Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if(state.availableDrivers.isNotEmpty()) F1Red.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha=0.1f))
                            .border(1.dp, if(state.availableDrivers.isNotEmpty()) F1Red else Color.Gray, RoundedCornerShape(4.dp))
                            .clickable(enabled = state.availableDrivers.isNotEmpty()) { setShowD2Dialog(true) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = state.battleModeDriver2?.nameAcronym ?: "D2",
                            color = if(state.availableDrivers.isNotEmpty()) F1Red else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Speed Trace ---
        PitWallCard(title = "SPEED TRACE", modifier = Modifier.weight(1f)) {
             Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                if (state.isBattleModeLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CyberCyan
                    )
                } else if (state.availableDrivers.isEmpty()) {
                    TechnicalEmptyState(
                        message = "SYSTEM OFFLINE",
                        subMessage = "AWAITING SESSION START",
                        modifier = Modifier.align(Alignment.Center).fillMaxSize()
                    )
                } else if (state.battleModeTelemetryD1.isEmpty() && state.battleModeTelemetryD2.isEmpty()) {
                    TechnicalEmptyState(
                        message = "NO TELEMETRY",
                        subMessage = "DRIVER DATA UNAVAILABLE",
                        modifier = Modifier.align(Alignment.Center).fillMaxSize()
                    )
                } else {
                    SpeedTraceChart(
                        data1 = state.battleModeTelemetryD1.map { it.speed.toFloat() },
                        data2 = state.battleModeTelemetryD2.map { it.speed.toFloat() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Legend
                    Row(modifier = Modifier.align(Alignment.TopEnd)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                           Box(modifier = Modifier.size(8.dp).background(CyberCyan))
                           Spacer(modifier = Modifier.width(4.dp))
                           Text(state.battleModeDriver1?.nameAcronym ?: "D1", color = Color.Gray, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                           Box(modifier = Modifier.size(8.dp).background(F1Red))
                           Spacer(modifier = Modifier.width(4.dp))
                           Text(state.battleModeDriver2?.nameAcronym ?: "D2", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
             }
        }

        // --- Throttle Trace ---
        PitWallCard(title = "THROTTLE INPUT", modifier = Modifier.weight(1f)) {
             Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                 if (state.availableDrivers.isEmpty()) {
                      TechnicalEmptyState(
                        message = "OFFLINE",
                        subMessage = "SYSTEM STANDBY",
                        modifier = Modifier.align(Alignment.Center).fillMaxSize()
                    )
                 } else if (!state.isBattleModeLoading && (state.battleModeTelemetryD1.isNotEmpty() || state.battleModeTelemetryD2.isNotEmpty())) {
                     SpeedTraceChart(
                        data1 = state.battleModeTelemetryD1.map { it.throttle.toFloat() },
                        data2 = state.battleModeTelemetryD2.map { it.throttle.toFloat() },
                        modifier = Modifier.fillMaxSize()
                    )
                 } else {
                      TechnicalEmptyState(
                        message = "NO DATA LINK",
                        subMessage = "CHECK SOURCE CONNECTION",
                        modifier = Modifier.align(Alignment.Center).fillMaxSize()
                    )
                 }
             }
        }
    }
}
