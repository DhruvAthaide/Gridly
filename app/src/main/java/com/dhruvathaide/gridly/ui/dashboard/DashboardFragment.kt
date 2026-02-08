package com.dhruvathaide.gridly.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.components.DriverCompareCard
import com.dhruvathaide.gridly.ui.components.DriverSelectionDialog
import com.dhruvathaide.gridly.ui.components.SpeedTraceChart
import com.dhruvathaide.gridly.ui.components.TrackMap

class DashboardFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashboardScreen(viewModel)
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    
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
    
    // Background: Deep Cyberpunk Blue
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header: Status & Weather
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PIT WALL COMMAND",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                
                // Track Status Indicator
                Box(
                    modifier = Modifier
                        .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "TRACK CLEAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            // 2. Driver Comparison (or Empty State)
            if (state.driver1 != null || state.driver2 != null) {
                // LIVE DATA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DriverCompareCard(
                        driver = state.driver1,
                        telemetry = state.driver1Telemetry.lastOrNull(),
                        interval = state.d1Interval,
                        tyre = state.d1TyreCompound,
                        tyreLife = state.d1TyreLife,
                        pitStops = state.d1PitStops,
                        sectors = state.d1Sectors,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { setShowD1Dialog(true) }
                    )
                    
                    DriverCompareCard(
                        driver = state.driver2,
                        telemetry = state.driver2Telemetry.lastOrNull(),
                        interval = state.d2Interval,
                        tyre = state.d2TyreCompound,
                        tyreLife = state.d2TyreLife,
                        pitStops = state.d2PitStops,
                        sectors = state.d2Sectors,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { setShowD2Dialog(true) }
                    )
                }
            } else {
                // WAITING STATE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Text(
                            text = "AWAITING LIVE FEED",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.activeSession != null) "SESSION PENDING" else "NO DATA LINK",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            
            // 3. Gap Evolution Graph
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GAP EVOLUTION (LAST 10 LAPS)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                     com.dhruvathaide.gridly.ui.components.GapEvolutionChart(
                        gapHistory = state.gapHistory,
                        modifier = Modifier.fillMaxSize()
                     )
                }
            }
            
            // Removed Track Map as per user request (unreliable live tracker)

            // 5. Race Control Terminal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "> RACE CONTROL FEED",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val msg = state.raceControlMessage ?: "SYSTEM NORMAL..."
                Text(
                    text = "> $msg",
                    color = if (state.raceControlMessage != null) Color(0xFFFFEB3B) else Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Spacer for floating nav bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
