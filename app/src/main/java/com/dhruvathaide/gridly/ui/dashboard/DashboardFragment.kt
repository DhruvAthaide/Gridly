package com.dhruvathaide.gridly.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.ui.MainViewModel

class DashboardFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                com.dhruvathaide.gridly.ui.theme.GridlyTheme {
                    DashboardScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_overview),
        stringResource(R.string.tab_telemetry),
        stringResource(R.string.tab_strategy)
    )

    // Error Handling
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.isError) {
        if (state.isError) {
            val result = snackbarHostState.showSnackbar(
                message = state.errorMessage ?: "Unknown Error",
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        containerColor = com.dhruvathaide.gridly.ui.theme.DarkAsphalt,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // Pit Wall Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.dhruvathaide.gridly.ui.theme.DarkAsphalt)
            ) {
                 Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PIT WALL // DATA LINK",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    
                    // Live Indicator
                    if (state.activeSession != null) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, com.dhruvathaide.gridly.ui.theme.F1Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = com.dhruvathaide.gridly.ui.theme.F1Red,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Technical Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = com.dhruvathaide.gridly.ui.theme.F1Red,
                            height = 3.dp
                        )
                    },
                    divider = {
                        Divider(color = com.dhruvathaide.gridly.ui.theme.CarbonFiber)
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedTabIndex == index) Color.White else Color.Gray,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(com.dhruvathaide.gridly.ui.theme.DarkAsphalt)
        ) {
            when (selectedTabIndex) {
                0 -> com.dhruvathaide.gridly.ui.dashboard.tabs.OverviewTab(viewModel, state)
                // 1 -> com.dhruvathaide.gridly.ui.dashboard.tabs.TelemetryTab(viewModel, state)
                // 2 -> com.dhruvathaide.gridly.ui.dashboard.tabs.StrategyTab(state)
            }
        }
    }
}
