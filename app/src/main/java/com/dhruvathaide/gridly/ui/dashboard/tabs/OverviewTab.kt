package com.dhruvathaide.gridly.ui.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.DashboardUiState
import com.dhruvathaide.gridly.ui.components.DriverCompareCard
import com.dhruvathaide.gridly.ui.components.DriverSelectionDialog
import androidx.compose.ui.res.stringResource
import com.dhruvathaide.gridly.R
// import com.dhruvathaide.gridly.ui.components.GapEvolutionChart // If needed directly or via composition

@Composable
fun OverviewTab(
    viewModel: MainViewModel,
    state: DashboardUiState
) {
    // Dialog State
    val (showD1Dialog, setShowD1Dialog) = remember { mutableStateOf(false) }
    val (showD2Dialog, setShowD2Dialog) = remember { mutableStateOf(false) }

    if (showD1Dialog) {
        DriverSelectionDialog(
            drivers = state.availableDrivers,
            onDriverSelected = { 
                viewModel.selectDrivers(it, state.driver2 ?: it) // Safe fallback
                setShowD1Dialog(false)
            },
            onDismissRequest = { setShowD1Dialog(false) }
        )
    }

    if (showD2Dialog) {
         DriverSelectionDialog(
            drivers = state.availableDrivers,
            onDriverSelected = { 
                viewModel.selectDrivers(state.driver1 ?: it, it) // Safe fallback
                setShowD2Dialog(false)
            },
            onDismissRequest = { setShowD2Dialog(false) }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Status & Weather
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.pit_wall_command),
                color = com.dhruvathaide.gridly.ui.theme.CyberCyan,
                style = MaterialTheme.typography.displayMedium, // Customized in Type.kt
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.shadow(8.dp, spotColor = com.dhruvathaide.gridly.ui.theme.CyberCyan)
            )
            
            // Track Status Indicator
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(com.dhruvathaide.gridly.ui.theme.InterGreen, Color(0xFF81C784))
                        ), 
                        RoundedCornerShape(4.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha=0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.track_clear), 
                    color = Color.Black, 
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 2. Driver Comparison (or Empty State)
        if (state.driver1 != null || state.driver2 != null) {
            // LIVE DATA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Increased height for better layout
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                com.dhruvathaide.gridly.ui.components.DriverCompareCard(
                    driver = state.driver1,
                    telemetry = state.driver1Telemetry.lastOrNull(),
                    interval = state.d1Interval,
                    tyre = state.d1TyreCompound,
                    tyreLife = state.d1TyreLife,
                    pitStops = state.d1PitStops,
                    sectors = state.d1Sectors ?: Triple("-", "-", "-"),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { setShowD1Dialog(true) }
                )
                
                 com.dhruvathaide.gridly.ui.components.DriverCompareCard(
                    driver = state.driver2,
                    telemetry = state.driver2Telemetry.lastOrNull(),
                    interval = state.d2Interval,
                    tyre = state.d2TyreCompound,
                    tyreLife = state.d2TyreLife,
                    pitStops = state.d2PitStops,
                    sectors = state.d2Sectors ?: Triple("-", "-", "-"),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { setShowD2Dialog(true) }
                )
            }
        } else {
            // WAITING STATE / ERROR STATE
            com.dhruvathaide.gridly.ui.components.PitWallCard {
                 Column(
                     modifier = Modifier.fillMaxWidth().padding(24.dp),
                     horizontalAlignment = Alignment.CenterHorizontally
                 ) {
                     Text(
                        text = if (state.isError) stringResource(R.string.connection_failed) else stringResource(R.string.awaiting_live_feed),
                        color = if (state.isError) com.dhruvathaide.gridly.ui.theme.F1Red else Color.Gray,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.errorMessage ?: (if (state.activeSession != null) stringResource(R.string.session_pending) else stringResource(R.string.no_data_link)),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    if (state.isError || (state.activeSession == null && !state.isLoading)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(containerColor = com.dhruvathaide.gridly.ui.theme.CyberCyan),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(stringResource(R.string.retry_connection), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                 }
            }
        }
        
        // 3. Gap Evolution Graph
        com.dhruvathaide.gridly.ui.components.PitWallCard(title = stringResource(R.string.gap_evolution)) {
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
             ) {
                 if (state.gapHistory.isNotEmpty()) {
                     com.dhruvathaide.gridly.ui.components.GapEvolutionChart(
                        gapHistory = state.gapHistory,
                        modifier = Modifier.fillMaxSize()
                     )
                 } else {
                     // Empty State
                     Column(
                         modifier = Modifier.align(Alignment.Center),
                         horizontalAlignment = Alignment.CenterHorizontally
                     ) {
                         Text(stringResource(R.string.no_telemetry_data), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                     }
                 }
             }
        }

        // 5. Race Control Terminal
        com.dhruvathaide.gridly.ui.components.PitWallCard(title = stringResource(R.string.race_control_feed)) {
             Column(modifier = Modifier.fillMaxWidth()) {
                val msg = state.raceControlMessage ?: stringResource(R.string.system_normal)
                Text(
                    text = "> $msg",
                    color = if (state.raceControlMessage != null) Color.White else Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace
                )
             }
        }
        
        // Spacer for floating nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}
