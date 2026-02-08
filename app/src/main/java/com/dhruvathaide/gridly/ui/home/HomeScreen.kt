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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.MockDataProvider
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Cyberpunk BG
    ) {
        // 1. Poster Background Art
        PosterBackground()
        
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
                    text = "NEXT GRAND PRIX",
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
            
            // Race Title ("MONACO")
            Text(
                text = "MONACO",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(horizontal = 24.dp),
                letterSpacing = (-2).sp
            )
            
            Text(
                text = "CIRCUIT DE MONACO • 24 MAY",
                color = Color.Red, // F1 Red accent
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Countdown Ticker
            CyberpunkCountdown()
            
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
            
            NewsFeedList()
        }
    }
}

@Composable
fun PosterBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Watermark Track Map
        Image(
            painter = painterResource(id = R.drawable.track_monaco),
            contentDescription = null,
            modifier = Modifier
                .size(600.dp) // Huge
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .alpha(0.1f), // Subtle
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
fun CyberpunkCountdown() {
    // Target: 2 days, 14 hours from now (Mock)
    var remainingMillis by remember { mutableStateOf(2 * 24 * 3600 * 1000L + 14 * 3600 * 1000L) }
    
    LaunchedEffect(Unit) {
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
        CountdownUnit(value = seconds.toInt(), label = "SEC", isHighlighted = true)
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
fun NewsFeedList() {
    val news = remember { MockDataProvider.mockNews }
    
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(news) { item ->
            NewsCard(item)
        }
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Bottom Nav clearance
        }
    }
}

@Composable
fun NewsCard(item: MockDataProvider.NewsItem) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor("#${item.categoryColor}"))
    } catch(e: Exception) { Color.Gray }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
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
