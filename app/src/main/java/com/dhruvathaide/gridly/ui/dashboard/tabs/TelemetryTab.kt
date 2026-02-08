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

@Composable
fun TelemetryTab(
    viewModel: MainViewModel,
    state: DashboardUiState
) {
    // Initialize Battle Mode with current Dashboard drivers if not set
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

    if (state.availableDrivers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NO LIVE TELEMETRY",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ... (Existing Controls)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        if (state.battleModeLap > 1) viewModel.setBattleModeLap(state.battleModeLap - 1) 
                    }) {
                        Text("<", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = "LAP ${state.battleModeLap}",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = { 
                        if (state.battleModeLap < state.maxLaps) viewModel.setBattleModeLap(state.battleModeLap + 1)
                    }) {
                        Text(">", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Driver Names (Clickable)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                            .clickable { setShowD1Dialog(true) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.battleModeDriver1?.nameAcronym ?: state.driver1?.nameAcronym ?: "D1",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text("VS", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF4081).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFF4081), RoundedCornerShape(4.dp))
                            .clickable { setShowD2Dialog(true) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.battleModeDriver2?.nameAcronym ?: state.driver2?.nameAcronym ?: "D2",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Speed Chart
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (state.isBattleModeLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF00E5FF)
                    )
                } else if (state.battleModeTelemetryD1.isEmpty() && state.battleModeTelemetryD2.isEmpty()) {
                    Text(
                        text = "NO TELEMETRY DATA FOR LAP ${state.battleModeLap}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column {
                        Text("SPEED TRACE", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                        SpeedTraceChart(
                            data1 = state.battleModeTelemetryD1.map { it.speed.toFloat() },
                            data2 = state.battleModeTelemetryD2.map { it.speed.toFloat() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Throttle/Brake Chart
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                 if (!state.isBattleModeLoading && (state.battleModeTelemetryD1.isNotEmpty() || state.battleModeTelemetryD2.isNotEmpty())) {
                    Column {
                        Text("THROTTLE TRACE", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                        SpeedTraceChart(
                            data1 = state.battleModeTelemetryD1.map { it.throttle.toFloat() },
                            data2 = state.battleModeTelemetryD2.map { it.throttle.toFloat() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                 }
            }
        }
    }
}
