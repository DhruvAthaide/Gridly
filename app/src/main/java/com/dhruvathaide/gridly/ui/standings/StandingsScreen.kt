package com.dhruvathaide.gridly.ui.standings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.ui.components.ConstructorsStandingsChart
import com.dhruvathaide.gridly.ui.components.F1AngledShape
import com.dhruvathaide.gridly.ui.components.F1RedStrip

@Composable
fun StandingsScreen(
    viewModel: com.dhruvathaide.gridly.ui.MainViewModel,
    onDriverClick: (MockDataProvider.DriverStanding) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Drivers, 1 = Constructors
    
    val driverStandings = state.driverStandings
    val constructorStandings = state.constructorStandings
    
    val isEmpty = driverStandings.isEmpty() && constructorStandings.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.dhruvathaide.gridly.ui.theme.DarkAsphalt)
            .statusBarsPadding()
    ) {
        // --- Header ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(com.dhruvathaide.gridly.ui.theme.F1Red)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.title_standings),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // F1 Style Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = com.dhruvathaide.gridly.ui.theme.F1Red,
                        height = 3.dp
                    )
                },
                divider = { Divider(color = com.dhruvathaide.gridly.ui.theme.CarbonFiber) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "DRIVERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == 0) Color.White else Color.Gray,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "CONSTRUCTORS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == 1) Color.White else Color.Gray,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isEmpty) {
            // EMPTY STATE
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flag_checkered),
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SEASON PENDING",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "AWAITING 2026 GRID",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.dhruvathaide.gridly.ui.theme.F1Red
                    )
                }
            }
        } else {
             if (selectedTab == 0) {
                DriverStandingsList(driverStandings, onDriverClick)
            } else {
                ConstructorsStandingsChart(constructorStandings)
            }
        }
    }
}

@Composable
fun DriverStandingsList(
    standings: List<MockDataProvider.DriverStanding>,
    onDriverClick: (MockDataProvider.DriverStanding) -> Unit
) {
    val top3 = standings.take(3)
    val rest = standings.drop(3)
    
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Hero Podium
        item {
            HeroPodium(top3)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("POS", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                Text("DRIVER", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("PTS", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            Divider(color = com.dhruvathaide.gridly.ui.theme.CarbonFiber, thickness = 1.dp)
        }
        
        // Rest of Grid
        items(rest) { standing ->
            DriverRow(standing, onDriverClick)
        }
    }
}

@Composable
fun HeroPodium(top3: List<MockDataProvider.DriverStanding>) {
    if (top3.size < 3) return
    val p1 = top3[0]
    val p2 = top3[1]
    val p3 = top3[2]
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // P2
        PodiumStep(p2, 2, Modifier.weight(1f).height(140.dp), com.dhruvathaide.gridly.ui.theme.SafetyYellow) // Using Yellow as generic highlight or sliver
        // P1
        PodiumStep(p1, 1, Modifier.weight(1.2f).height(180.dp), com.dhruvathaide.gridly.ui.theme.F1Red)
        // P3
        PodiumStep(p3, 3, Modifier.weight(1f).height(110.dp), com.dhruvathaide.gridly.ui.theme.CyberCyan)
    }
}

@Composable
fun PodiumStep(standing: MockDataProvider.DriverStanding, rank: Int, modifier: Modifier, accentColor: Color) {
    val teamColor = try {
        Color(android.graphics.Color.parseColor("#${standing.driver.teamColour}"))
    } catch (e: Exception) { Color.Gray }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Position Badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(accentColor, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
             Text(
                text = "$rank",
                color = Color.Black,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(teamColor.copy(alpha=0.9f), teamColor.copy(alpha=0.3f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha=0.2f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
        ) {
             Column(
                 modifier = Modifier.fillMaxSize().padding(8.dp),
                 horizontalAlignment = Alignment.CenterHorizontally,
                 verticalArrangement = Arrangement.Top
             ) {
                  Text(
                    text = standing.driver.nameAcronym,
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 16.sp
                )
                Text(
                    text = "${standing.points}",
                    color = Color.White.copy(alpha=0.8f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 12.sp
                )
             }
        }
    }
}

@Composable
fun DriverRow(
    standing: MockDataProvider.DriverStanding,
    onClick: (MockDataProvider.DriverStanding) -> Unit
) {
    val teamColor = try {
        Color(android.graphics.Color.parseColor("#${standing.driver.teamColour}"))
    } catch (e: Exception) { Color.Gray }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(standing) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = "${standing.position}",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium, // Using bold F1 font
            modifier = Modifier.width(32.dp),
            fontSize = 16.sp
        )
        
        // Team Color Strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(teamColor)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Driver Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = standing.driver.fullName.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 14.sp
            )
            Text(
                text = standing.driver.teamName ?: "",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp
            )
        }
        
        // Points
        Text(
            text = "${standing.points} PTS",
            color = com.dhruvathaide.gridly.ui.theme.CyberCyan,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
    Divider(color = com.dhruvathaide.gridly.ui.theme.CarbonFiber, thickness = 0.5.dp)
}
