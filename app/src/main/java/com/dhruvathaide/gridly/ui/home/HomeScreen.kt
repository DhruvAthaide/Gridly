package com.dhruvathaide.gridly.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.ui.theme.*
import com.dhruvathaide.gridly.ui.components.F1AngledShape
import com.dhruvathaide.gridly.ui.components.F1RedStrip
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: com.dhruvathaide.gridly.ui.MainViewModel,
    onNewsClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val session = state.raceSession ?: state.activeSession
    val context = LocalContext.current

    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text(
                    text = "INTEL SOURCES",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                state.newsFilters.forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleNewsFilter(context, source.url) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = source.isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = F1Red,
                                uncheckedColor = TextTertiary,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = source.name,
                            color = if (source.isSelected) TextPrimary else TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(color = DividerColor)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAsphalt)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRIDLY",
                    color = F1Red,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // HERO SECTION
                item {
                    if (session != null) {
                        HeroRaceCard(session)
                    } else {
                        EmptyHeroCard()
                    }
                }

                // COUNTDOWN SECTION
                item {
                    if (session != null) {
                        F1Countdown(session.dateStart)
                    }
                }

                // NEWS HEADER
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(F1AngledShape)
                                .background(F1Red)
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "LATEST INTEL",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Filter",
                                tint = TextTertiary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(F1RedStrip)
                    )
                }

                // NEWS LIST
                items(state.newsFeed) { item ->
                    NewsItemRow(item) { onNewsClick(item.url) }
                }
            }
        }
    }
}

@Composable
fun HeroRaceCard(session: com.dhruvathaide.gridly.data.remote.model.SessionDto) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        val trackRes = com.dhruvathaide.gridly.ui.common.ResourceHelper.getTrackMap(
            context,
            session.countryName ?: "",
            session.location ?: ""
        )

        Image(
            painter = painterResource(id = trackRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(360.dp)
                .offset(x = 80.dp)
                .alpha(0.07f),
            colorFilter = ColorFilter.tint(CyberCyan)
        )

        // Bottom gradient fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkAsphalt)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "UP NEXT",
                color = CyberCyan,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = session.circuitShortName?.uppercase() ?: "UNKNOWN",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(F1Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = (session.location?.uppercase() ?: "UNKNOWN"),
                    color = TextSecondary,
                    style = MaterialTheme.typography.titleSmall,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun EmptyHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "OFF SEASON",
                style = MaterialTheme.typography.displayLarge,
                color = TextTertiary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AWAITING RACE CALENDAR",
                color = F1Red,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun F1Countdown(targetDateIso: String?) {
    var remainingMillis by remember(targetDateIso) {
        mutableStateOf(calculateRemaining(targetDateIso))
    }

    LaunchedEffect(targetDateIso) {
        while (remainingMillis > 0) {
            delay(1000)
            remainingMillis = calculateRemaining(targetDateIso)
        }
    }

    if (remainingMillis <= 0L) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(F1Red, F1RedDark)
                    )
                )
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LIGHTS OUT",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    } else {
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonFiber)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CountdownUnit(days.toInt(), "DAYS")
            CountdownDivider()
            CountdownUnit(hours.toInt(), "HRS")
            CountdownDivider()
            CountdownUnit(minutes.toInt(), "MINS")
            CountdownDivider()
            CountdownUnit(seconds.toInt(), "SECS")
        }
    }
}

@Composable
fun CountdownUnit(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = F1Red,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CountdownDivider() {
    Text(
        text = ":",
        color = TextTertiary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
fun NewsItemRow(item: MockDataProvider.NewsItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = item.timeAgo.uppercase(),
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
    HorizontalDivider(
        color = DividerColor,
        modifier = Modifier.padding(start = 90.dp, end = 20.dp),
        thickness = 0.5.dp
    )
}

fun calculateRemaining(targetDateIso: String?): Long {
    if (targetDateIso == null) return 0L
    return try {
        val target = Instant.parse(targetDateIso).toEpochMilli()
        val now = Instant.now().toEpochMilli()
        (target - now).coerceAtLeast(0L)
    } catch (e: Exception) { 0L }
}
