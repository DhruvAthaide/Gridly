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
import androidx.compose.ui.draw.blur
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
    val session = state.activeSession
    val context = LocalContext.current
    
    // Filter Sheet State
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = com.dhruvathaide.gridly.ui.theme.DarkAsphalt,
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text(
                    text = "INTEL SOURCES",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
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
                            onCheckedChange = null, // Handled by Row click
                            colors = CheckboxDefaults.colors(
                                checkedColor = com.dhruvathaide.gridly.ui.theme.F1Red,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = source.name,
                            color = if (source.isSelected) Color.White else Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Divider(color = com.dhruvathaide.gridly.ui.theme.CarbonFiber)
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.dhruvathaide.gridly.ui.theme.DarkAsphalt)
    ) {
        // 1. Background Texture
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // F1 Logo Placeholder / Text
                Text(
                    text = "F1 LIVE // HUB",
                    color = com.dhruvathaide.gridly.ui.theme.F1Red,
                    style = MaterialTheme.typography.displayMedium, // Monospace bold
                    fontSize = 20.sp,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
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
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                     ) {
                          // Angled Header
                          Box(
                              modifier = Modifier
                                  .clip(com.dhruvathaide.gridly.ui.components.F1AngledShape)
                                  .background(com.dhruvathaide.gridly.ui.theme.F1Red)
                                  .padding(horizontal = 24.dp, vertical = 6.dp)
                          ) {
                              Text(
                                  text = "LATEST INTEL",
                                  color = Color.White,
                                  style = MaterialTheme.typography.labelSmall,
                                  fontWeight = FontWeight.Bold
                              )
                          }
                          
                         IconButton(onClick = { showFilterSheet = true }) {
                             Icon(
                                 imageVector = Icons.Default.Menu,
                                 contentDescription = "Filter",
                                 tint = Color.Gray
                             )
                         }
                      }
                      // Red Line
                      Box(
                          modifier = Modifier
                             .fillMaxWidth()
                             .height(2.dp)
                             .background(com.dhruvathaide.gridly.ui.components.F1RedStrip)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
         // Circuit Map Watermark
         val trackRes = when {
            session.circuitShortName?.contains("Monaco", ignoreCase = true) == true -> R.drawable.track_monaco
            session.circuitShortName?.contains("Abu Dhabi", ignoreCase = true) == true -> R.drawable.track_abu_dhabi
            session.circuitShortName?.contains("Bahrain", ignoreCase = true) == true -> R.drawable.track_bahrain
            else -> R.drawable.track_monaco
        }
        
        Image(
            painter = painterResource(id = trackRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(400.dp)
                .offset(x = 100.dp)
                .alpha(0.1f),
            colorFilter = ColorFilter.tint(Color.White)
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            // "UP NEXT" Tag
            Text(
                text = "UP NEXT",
                color = com.dhruvathaide.gridly.ui.theme.CyberCyan,
                style = MaterialTheme.typography.labelSmall
            )
            
            // Circuit Name (Huge)
            Text(
                text = session.circuitShortName?.uppercase() ?: "UNKNOWN",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                lineHeight = 48.sp
            )
            
            // Location / Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(com.dhruvathaide.gridly.ui.theme.F1Red))
                Spacer(modifier = Modifier.width(8.dp))
                val location = session.location?.uppercase() ?: "UNKNOWN"
                Text(
                    text = location,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun EmptyHeroCard() {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
             Text(
                text = "OFF SEASON",
                style = MaterialTheme.typography.displayLarge,
                color = Color.Gray.copy(alpha=0.3f)
            )
            Text("AWAITING 2026 CALENDAR", color = com.dhruvathaide.gridly.ui.theme.F1Red, style = MaterialTheme.typography.labelSmall)
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
    
    val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        CompactCountdownUnit(days.toInt(), "DAYS")
        CompactCountdownUnit(hours.toInt(), "HRS")
        CompactCountdownUnit(minutes.toInt(), "MINS")
    }
}

@Composable
fun CompactCountdownUnit(value: Int, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontSize = 32.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = com.dhruvathaide.gridly.ui.theme.F1Red,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
    }
}

@Composable
fun NewsItemRow(item: MockDataProvider.NewsItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Time / Category Column
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.End
        ) {
             Text(
                text = item.timeAgo.uppercase(),
                color = com.dhruvathaide.gridly.ui.theme.CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
             Text(
                text = item.title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
    // Divider
    Divider(
        color = Color(0xFF333333), 
        modifier = Modifier.padding(start = 96.dp, end = 24.dp), 
        thickness = 1.dp
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
