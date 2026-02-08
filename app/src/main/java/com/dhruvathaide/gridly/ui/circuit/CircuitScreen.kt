package com.dhruvathaide.gridly.ui.circuit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.ui.common.ResourceHelper

@Composable
fun CircuitScreen(session: SessionDto?) {
    val context = LocalContext.current
    
    // Fallback data if session is null
    val country = session?.countryName ?: "Monaco"
    val location = session?.location ?: "Monte Carlo"
    val trackName = "CIRCUIT DE ${location.uppercase()}"
    
    // Resolve Track Map
    val mapId = ResourceHelper.getTrackMap(context, country, location)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Cyberpunk BG
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        
        // Header
        item {
             Column {
                Text(
                    text = trackName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "$location, $country",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
             }
        }
        
        // Track Map (White Tint Fix)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = mapId),
                    contentDescription = "Track Map",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    // CRITICAL FIX: Tint the black track image to White
                    colorFilter = if (mapId != R.drawable.ic_track_placeholder && mapId != R.drawable.ic_trophy) 
                                  ColorFilter.tint(Color.White) else null
                )
            }
        }
        
        // Stats Grid
        item {
            StatsGridRow()
        }
        
        // Weather
        item {
             WeatherCard()
        }
        
        // Weekend Schedule
        item {
            ScheduleCard()
        }
    }
}

@Composable
fun StatsGridRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(label = "LENGTH", value = "3.337 km", icon = R.drawable.ic_length, modifier = Modifier.weight(1f))
        StatCard(label = "LAPS", value = "78", icon = R.drawable.ic_flag_checkered, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(label = "RECORD", value = "1:12.909", icon = R.drawable.ic_timer, modifier = Modifier.weight(1f))
        StatCard(label = "DRS ZONES", value = "1", icon = R.drawable.ic_speed, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeatherCard() {
    val weather = MockDataProvider.mockWeather
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "TRACK CONDITIONS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = weather.type, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
             WeatherStat(label = "AIR", value = weather.temp)
             Spacer(modifier = Modifier.width(16.dp))
             WeatherStat(label = "RAIN", value = weather.chanceOfRain, color = Color(0xFF00E5FF))
        }
    }
}

@Composable
fun WeatherStat(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScheduleCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text(
            text = "WEEKEND SCHEDULE",
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        MockDataProvider.mockWeekendSchedule.forEach { session ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (session.status == "COMPLETED") Color.Gray else Color(0xFFFF0000))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = session.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = session.day, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                
                Text(
                    text = session.time,
                    color = if (session.status == "COMPLETED") Color.Gray else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (session != MockDataProvider.mockWeekendSchedule.last()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF333333)))
            }
        }
    }
}
