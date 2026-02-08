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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.ui.components.ConstructorsStandingsChart

@Composable
fun StandingsScreen(
    onDriverClick: (MockDataProvider.DriverStanding) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Drivers, 1 = Constructors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Cyberpunk BG
            .padding(16.dp)
    ) {
        // Custom Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                .padding(4.dp)
        ) {
            TabButton(
                text = "DRIVERS",
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f)
            ) { selectedTab = 0 }
            
            TabButton(
                text = "CONSTRUCTORS",
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f)
            ) { selectedTab = 1 }
        }

        if (selectedTab == 0) {
            DriverStandingsList(MockDataProvider.driverStandings, onDriverClick)
        } else {
            ConstructorsStandingsChart(MockDataProvider.constructorStandings)
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFFFF0000) else Color.Transparent) // F1 Red
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Podium
        item {
            HeroPodium(top3)
        }
        
        // List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("POS / DRIVER", color = Color.Gray, fontSize = 10.sp)
                Text("FORM (L5)", color = Color.Gray, fontSize = 10.sp)
                Text("PTS", color = Color.Gray, fontSize = 10.sp)
            }
        }
        
        // Rest of Grid
        items(rest) { standing ->
            DriverRow(standing, onDriverClick)
        }
    }
}

@Composable
fun HeroPodium(top3: List<MockDataProvider.DriverStanding>) {
    // Layout: 2nd - 1st - 3rd
    if (top3.size < 3) return
    val p1 = top3[0]
    val p2 = top3[1]
    val p3 = top3[2]
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // P2
        PodiumStep(p2, 2, Modifier.weight(1f).height(160.dp), Color(0xFFC0C0C0)) // Silver
        // P1
        PodiumStep(p1, 1, Modifier.weight(1.2f).height(200.dp), Color(0xFFFFD700)) // Gold
        // P3
        PodiumStep(p3, 3, Modifier.weight(1f).height(140.dp), Color(0xFFCD7F32)) // Bronze
    }
}

@Composable
fun PodiumStep(standing: MockDataProvider.DriverStanding, rank: Int, modifier: Modifier, medalColor: Color) {
    val teamColor = try {
        Color(android.graphics.Color.parseColor("#${standing.driver.teamColour}"))
    } catch (e: Exception) { Color.Gray }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar / Name
        Text(
            text = standing.driver.nameAcronym,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "${standing.points} PTS",
            color = Color.Gray,
            fontSize = 11.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(teamColor.copy(alpha = 0.8f), teamColor.copy(alpha = 0.2f))
                    )
                )
                .border(1.dp, medalColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "$rank",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 16.dp)
            )
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
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { onClick(standing) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position & Color Strip
        Text(
            text = "${standing.position}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp)
        )
        
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(teamColor)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Driver Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = standing.driver.fullName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = standing.driver.teamName ?: "",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        
        // Form Guide
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            standing.recentForm.takeLast(3).forEach { result ->
                FormDot(result)
            }
        }
        
        // Points
        Text(
            text = "${standing.points} PTS",
            color = Color(0xFFFFC107), // Amber
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun FormDot(result: String) {
    val color = when(result) {
        "1" -> Color(0xFFFFD700) // Gold
        "2" -> Color(0xFFC0C0C0) // Silver
        "3" -> Color(0xFFCD7F32) // Bronze
        "DNF" -> Color.Red
        else -> Color.Gray
    }
    
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (result == "DNF") "X" else result,
            color = Color.Black,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
