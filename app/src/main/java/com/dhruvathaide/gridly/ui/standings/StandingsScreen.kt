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
import com.dhruvathaide.gridly.ui.theme.*

@Composable
fun StandingsScreen(
    viewModel: com.dhruvathaide.gridly.ui.MainViewModel,
    onDriverClick: (MockDataProvider.DriverStanding) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val driverStandings = state.driverStandings
    val constructorStandings = state.constructorStandings
    val isEmpty = driverStandings.isEmpty() && constructorStandings.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAsphalt)
            .statusBarsPadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(F1Red)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.title_standings),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = F1Red,
                        height = 2.dp
                    )
                },
                divider = { HorizontalDivider(color = BorderSubtle) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "DRIVERS",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == 0) TextPrimary else TextTertiary,
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
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == 1) TextPrimary else TextTertiary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isEmpty) {
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flag_checkered),
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SEASON PENDING",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AWAITING RACE DATA",
                        style = MaterialTheme.typography.labelMedium,
                        color = F1Red
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
        item {
            HeroPodium(top3)
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("POS", color = TextTertiary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                Text("DRIVER", color = TextTertiary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("PTS", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
            }
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
        }

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
            .height(200.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumStep(p2, 2, Modifier.weight(1f).height(140.dp), Color(0xFFC0C0C0))
        PodiumStep(p1, 1, Modifier.weight(1.2f).height(180.dp), Color(0xFFFFD700))
        PodiumStep(p3, 3, Modifier.weight(1f).height(110.dp), Color(0xFFCD7F32))
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
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = Color.Black,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(teamColor.copy(alpha = 0.85f), teamColor.copy(alpha = 0.25f))
                    )
                )
                .border(1.dp, teamColor.copy(alpha = 0.3f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = standing.driver.nameAcronym,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${standing.points}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
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
        Text(
            text = "${standing.position}",
            color = TextPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(32.dp),
            fontWeight = FontWeight.Bold
        )

        // Team Color Strip
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(teamColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = standing.driver.fullName.uppercase(),
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = standing.driver.teamName ?: "",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp
            )
        }

        Text(
            text = "${standing.points} PTS",
            color = CyberCyan,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
}
