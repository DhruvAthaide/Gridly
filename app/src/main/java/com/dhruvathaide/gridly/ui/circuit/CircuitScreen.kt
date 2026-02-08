package com.dhruvathaide.gridly.ui.circuit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun CircuitScreen(
    session: SessionDto?, 
    weather: com.dhruvathaide.gridly.data.remote.model.WeatherDto?
) {
    val context = LocalContext.current
    var showWeatherDialog by remember { mutableStateOf(false) }
    
    // Fallback data if session is null
    val country = session?.countryName ?: "Monaco"
    val location = session?.location ?: "Monte Carlo"
    val trackName = "CIRCUIT DE ${location.uppercase()}"
    
    // Resolve Track Map
    val mapId = ResourceHelper.getTrackMap(context, country, location)
    
    if (showWeatherDialog) {
        AlertDialog(
            onDismissRequest = { showWeatherDialog = false },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color.Gray,
            title = {
                Text(
                    text = "TRACK FORECAST",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column {
                   // Mock 3-Day Forecast (since API gives only live usually or single point)
                   // We'll use the current weather for "Today" and mock next days relative to it
                   val temp = weather?.airTemperature ?: 25.0
                   
                   ForecastRow("FRIDAY (FP1/FP2)", "${temp.toInt()}°C", "0%", R.drawable.ic_speed) // Sun
                   Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                   ForecastRow("SATURDAY (QUALI)", "${(temp - 1).toInt()}°C", "10%", R.drawable.ic_speed) // Cloud
                   Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                   ForecastRow("SUNDAY (RACE)", "${(temp + 2).toInt()}°C", "40%", R.drawable.ic_speed) // Rain risk
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeatherDialog = false }) {
                    Text("CLOSE", color = Color(0xFF00E5FF))
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Cyberpunk BG
            .statusBarsPadding()
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
             WeatherCard(weather) { showWeatherDialog = true }
        }
        
        // Weekend Schedule
        item {
            ScheduleCard()
        }
    }
}

@Composable
fun ForecastRow(day: String, temp: String, rain: String, icon: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = day, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "Partly Cloudy", color = Color.Gray, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = temp, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "$rain Rain", color = Color(0xFF00E5FF), fontSize = 12.sp)
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
fun WeatherCard(
    weatherDto: com.dhruvathaide.gridly.data.remote.model.WeatherDto?,
    onClick: () -> Unit
) {
    // Use Real data or Mock if null
    val weatherDisplay = if (weatherDto != null) {
        MockDataProvider.WeatherForecast("LIVE", "${weatherDto.airTemperature} °C", "${weatherDto.rainfall} mm")
    } else {
        MockDataProvider.mockWeather
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { onClick() } // Clickable Trigger
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "TRACK CONDITIONS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = weatherDisplay.type, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Tap for forecast", color = Color(0xFF00E5FF), fontSize = 10.sp)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
             WeatherStat(label = "AIR", value = weatherDisplay.temp)
             Spacer(modifier = Modifier.width(16.dp))
             WeatherStat(label = "RAIN", value = weatherDisplay.chanceOfRain, color = Color(0xFF00E5FF))
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
