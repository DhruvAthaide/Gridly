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

@Composable
fun HomeScreen(
    viewModel: com.dhruvathaide.gridly.ui.MainViewModel,
    onNewsClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val session = state.activeSession
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Cyberpunk BG
    ) {
        // 1. Poster Background Art
        PosterBackground(session?.circuitShortName)
        
        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Spacer for top bar roughly
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header / Logo area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (session != null) "NEXT GRAND PRIX" else "SYSTEM STATUS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.ic_trophy),
                    contentDescription = "F1",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (session != null) {
                // REAL DATA Display
                Text(
                    text = session.circuitShortName?.uppercase() ?: "UNKNOWN",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    letterSpacing = (-2).sp,
                    lineHeight = 48.sp
                )
                
                // Format Date: "MONACO • 24/05/2026"
                val location = session.location?.uppercase() ?: "UNKNOWN"
                val date = try {
                    // Input: 2026-05-24T15:00:00
                    val instant = java.time.Instant.parse(session.dateStart) // Assumes UTC/ISO
                    val zone = java.time.ZoneId.systemDefault()
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    instant.atZone(zone).format(formatter)
                } catch (e: Exception) { 
                    // Fallback to simple substring
                     session.dateStart?.substring(0, 10) ?: ""
                }
                
                Text(
                    text = "$location • $date",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Countdown Ticker
                CyberpunkCountdown(session.dateStart)
            } else {
                // EMPTY / LOADING STATE
                Text(
                    text = "NO ACTIVE\nSESSION",
                    color = Color.DarkGray,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    letterSpacing = (-2).sp,
                    lineHeight = 48.sp
                )
                
                 Text(
                    text = "AWAITING TELEMETRY LINK...",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                // Placeholder countdown 00:00:00
                 Row(
                    modifier = Modifier.fillMaxWidth().alpha(0.3f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CountdownUnit(0, "DAYS")
                    CountdownSeparator()
                    CountdownUnit(0, "HRS")
                    CountdownSeparator()
                    CountdownUnit(0, "MIN")
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // "Latest News" Section
            Text(
                text = "LATEST INTEL",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            NewsFeedList(state.newsFeed) { url ->
                onNewsClick(url)
            }
        }
    }
}
@Composable
fun PosterBackground(circuitName: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Watermark Track Map
        val trackRes = when {
            circuitName?.contains("Monaco", ignoreCase = true) == true -> R.drawable.track_monaco
            circuitName?.contains("Abu Dhabi", ignoreCase = true) == true -> R.drawable.track_abu_dhabi
            circuitName?.contains("Bahrain", ignoreCase = true) == true -> R.drawable.track_bahrain
            else -> R.drawable.track_monaco // Default generic art for now
        }
        
        Image(
            painter = painterResource(id = trackRes),
            contentDescription = null,
            modifier = Modifier
                .size(600.dp) // Huge
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .alpha(if (circuitName != null) 0.1f else 0.05f), // Dimmer if no session
            colorFilter = ColorFilter.tint(Color.White),
            contentScale = ContentScale.Fit
        )
        
        // Bottom Gradient Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF020617), // Fade to solid BG color at bottom for list
                            Color(0xFF020617)
                        ),
                        startY = 0f,
                        endY = 1500f
                    )
                )
        )
    }
}

@Composable
fun CyberpunkCountdown(targetDateIso: String?) {
    // Parse Date or use future (now + 7 days) if null
    var remainingMillis by remember(targetDateIso) { 
        mutableStateOf(calculateRemaining(targetDateIso)) 
    }
    
    LaunchedEffect(targetDateIso) {
        remainingMillis = calculateRemaining(targetDateIso)
        while (remainingMillis > 0) {
            delay(1000)
            remainingMillis -= 1000
        }
    }
    
    val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountdownUnit(value = days.toInt(), label = "DAYS")
        CountdownSeparator()
        CountdownUnit(value = hours.toInt(), label = "HRS")
        CountdownSeparator()
        CountdownUnit(value = minutes.toInt(), label = "MIN")
        CountdownSeparator()
        CountdownUnit(value = seconds.toInt(), label = "SEC", isHighlighted = remainingMillis > 0)
    }
}

fun calculateRemaining(isoDate: String?): Long {
    if (isoDate == null) return 0L
    return try {
        // Robust Parsing for OpenF1 (usually "2026-05-24T15:00:00" or with Z)
        val instant = try {
            java.time.Instant.parse(isoDate)
        } catch (e: Exception) {
            // Fallback: Parse as Local and assume UTC (OpenF1 style)
            java.time.LocalDateTime.parse(isoDate).atZone(java.time.ZoneId.of("UTC")).toInstant()
        }
        val now = System.currentTimeMillis()
        (instant.toEpochMilli() - now).coerceAtLeast(0L)
    } catch (e: Exception) {
         e.printStackTrace()
         0L
    }
}

@Composable
fun CountdownUnit(value: Int, label: String, isHighlighted: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Animated Number (Simplified to Text for stability, could be AnimatedContent)
        Text(
            text = String.format("%02d", value),
            color = if (isHighlighted) Color.White else Color(0xFFEEEEEE),
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = if (isHighlighted) Color.Red else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CountdownSeparator() {
    Text(
        text = ":",
        color = Color.Gray,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp).offset(y = (-12).dp)
    )
}

@Composable
fun NewsFeedList(news: List<MockDataProvider.NewsItem>, onNewsClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(news) { item ->
            NewsCard(item) { onNewsClick(item.url) }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Bottom Nav clearance
        }
    }
}

@Composable
fun NewsCard(item: MockDataProvider.NewsItem, onClick: () -> Unit) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor("#${item.categoryColor}"))
    } catch(e: Exception) { Color.Gray }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .clickable { onClick() } // Clickable
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(categoryColor)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.category,
                    color = categoryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•  ${item.timeAgo}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = item.subtitle,
                color = Color.LightGray,
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
