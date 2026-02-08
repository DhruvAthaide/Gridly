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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import com.dhruvathaide.gridly.ui.dashboard.tabs.OverviewTab
import com.dhruvathaide.gridly.ui.dashboard.tabs.TelemetryTab
import com.dhruvathaide.gridly.ui.dashboard.tabs.StrategyTab
import com.dhruvathaide.gridly.ui.dashboard.tabs.RadioTab
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
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "TELEMETRY", "STRATEGY", "RADIO")
    
    // Background: Deep Cyberpunk Blue
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF020617),
            contentColor = Color(0xFF00E5FF),
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF00E5FF),
                    height = 2.dp
                )
            },
            divider = {
                Divider(color = Color(0xFF1E293B))
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    selectedContentColor = Color(0xFF00E5FF),
                    unselectedContentColor = Color.Gray
                )
            }
        }
        
        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> OverviewTab(viewModel, state)
                1 -> TelemetryTab(viewModel, state)
                2 -> StrategyTab(state)
                3 -> RadioTab(viewModel, state)
            }
        }
    }
}
