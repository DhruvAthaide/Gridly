package com.dhruvathaide.gridly.ui.circuit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.ui.common.ResourceHelper
import com.dhruvathaide.gridly.ui.theme.*

data class CircuitInfo(
    val length: String,
    val laps: String,
    val record: String,
    val drsZones: String
)

private val circuitDatabase = mapOf(
    "bahrain" to CircuitInfo("5.412 km", "57", "1:31.447", "3"),
    "jeddah" to CircuitInfo("6.174 km", "50", "1:30.734", "3"),
    "saudi arabia" to CircuitInfo("6.174 km", "50", "1:30.734", "3"),
    "australia" to CircuitInfo("5.278 km", "58", "1:19.813", "4"),
    "japan" to CircuitInfo("5.807 km", "53", "1:30.983", "2"),
    "china" to CircuitInfo("5.451 km", "56", "1:32.238", "2"),
    "miami" to CircuitInfo("5.412 km", "57", "1:29.708", "3"),
    "emilia-romagna" to CircuitInfo("4.909 km", "63", "1:15.484", "2"),
    "emilia romagna" to CircuitInfo("4.909 km", "63", "1:15.484", "2"),
    "monaco" to CircuitInfo("3.337 km", "78", "1:12.909", "1"),
    "canada" to CircuitInfo("4.361 km", "70", "1:13.078", "2"),
    "spain" to CircuitInfo("4.657 km", "66", "1:16.330", "2"),
    "austria" to CircuitInfo("4.318 km", "71", "1:05.619", "3"),
    "great britain" to CircuitInfo("5.891 km", "52", "1:27.097", "2"),
    "hungary" to CircuitInfo("4.381 km", "70", "1:17.103", "2"),
    "belgium" to CircuitInfo("7.004 km", "44", "1:46.286", "2"),
    "netherlands" to CircuitInfo("4.259 km", "72", "1:11.097", "2"),
    "italy" to CircuitInfo("5.793 km", "53", "1:21.046", "2"),
    "azerbaijan" to CircuitInfo("6.003 km", "51", "1:43.009", "2"),
    "singapore" to CircuitInfo("4.940 km", "62", "1:35.867", "3"),
    "united states" to CircuitInfo("5.513 km", "56", "1:36.169", "2"),
    "mexico" to CircuitInfo("4.304 km", "71", "1:17.774", "3"),
    "brazil" to CircuitInfo("4.309 km", "71", "1:10.540", "2"),
    "las vegas" to CircuitInfo("6.201 km", "50", "1:35.490", "2"),
    "qatar" to CircuitInfo("5.419 km", "57", "1:24.319", "3"),
    "abu dhabi" to CircuitInfo("5.281 km", "58", "1:26.103", "2")
)

private fun getCircuitInfo(session: SessionDto?): CircuitInfo {
    if (session == null) return CircuitInfo("-", "-", "-", "-")
    val country = session.countryName.lowercase()
    val location = session.location.lowercase()
    val circuitName = session.circuitShortName.lowercase()

    return circuitDatabase.entries.firstOrNull { (key, _) ->
        country.contains(key) || location.contains(key) || circuitName.contains(key)
    }?.value ?: CircuitInfo("-", "-", "-", "-")
}

@Composable
fun CircuitScreen(
    session: SessionDto?,
    weather: com.dhruvathaide.gridly.data.remote.model.WeatherDto?,
    meetingSessions: List<SessionDto> = emptyList()
) {
    val context = LocalContext.current
    var showWeatherDialog by remember { mutableStateOf(false) }

    val country = session?.countryName ?: "Unknown"
    val location = session?.location ?: "Unknown"
    val circuitName = session?.circuitShortName?.uppercase() ?: "UNKNOWN CIRCUIT"

    val mapId = ResourceHelper.getTrackMap(context, country, location)
    val circuitInfo = remember(session) { getCircuitInfo(session) }

    if (showWeatherDialog) {
        AlertDialog(
            onDismissRequest = { showWeatherDialog = false },
            containerColor = SurfaceElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
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
                    val temp = weather?.airTemperature ?: 25.0
                    val isRaining = (weather?.rainfall ?: 0) > 0
                    val humidity = weather?.humidity ?: 50.0

                    ForecastRow("FRIDAY (FP1/FP2)", "${temp.toInt()}C", if (isRaining) "60%" else "${(humidity * 0.2).toInt()}%")
                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    ForecastRow("SATURDAY (QUALI)", "${(temp - 1).toInt()}C", if (isRaining) "50%" else "${(humidity * 0.15).toInt()}%")
                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    ForecastRow("SUNDAY (RACE)", "${(temp + 2).toInt()}C", if (isRaining) "70%" else "${(humidity * 0.25).toInt()}%")
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeatherDialog = false }) {
                    Text("CLOSE", color = CyberCyan)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAsphalt)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column {
                Text(
                    text = circuitName,
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$location, $country",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonFiber)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = mapId),
                    contentDescription = "Track Map",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (mapId != R.drawable.ic_track_placeholder && mapId != R.drawable.ic_trophy)
                        ColorFilter.tint(TextPrimary) else null
                )
            }
        }

        item {
            StatsGridRow(circuitInfo)
        }

        item {
            WeatherCard(weather) { showWeatherDialog = true }
        }

        item {
            ScheduleCard(session, meetingSessions)
        }
    }
}

@Composable
fun ForecastRow(day: String, temp: String, rain: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = day, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Column(horizontalAlignment = Alignment.End) {
            Text(text = temp, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "$rain Rain", color = CyberCyan, fontSize = 12.sp)
        }
    }
}

@Composable
fun StatsGridRow(info: CircuitInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(label = "LENGTH", value = info.length, icon = R.drawable.ic_length, modifier = Modifier.weight(1f))
            StatCard(label = "LAPS", value = info.laps, icon = R.drawable.ic_flag_checkered, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(label = "RECORD", value = info.record, icon = R.drawable.ic_timer, modifier = Modifier.weight(1f))
            StatCard(label = "DRS ZONES", value = info.drsZones, icon = R.drawable.ic_speed, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(TextTertiary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = TextTertiary, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun WeatherCard(
    weatherDto: com.dhruvathaide.gridly.data.remote.model.WeatherDto?,
    onClick: () -> Unit
) {
    val condition = if (weatherDto != null) {
        val isRaining = weatherDto.rainfall > 0
        Triple(
            if (isRaining) "WET" else "DRY",
            "${weatherDto.airTemperature.toInt()}C",
            "${weatherDto.trackTemperature.toInt()}C"
        )
    } else {
        Triple("NO DATA", "-", "-")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "TRACK CONDITIONS", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = condition.first, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Tap for forecast", color = CyberCyan, fontSize = 10.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            WeatherStat(label = "AIR", value = condition.second)
            Spacer(modifier = Modifier.width(16.dp))
            WeatherStat(label = "TRACK", value = condition.third, color = CyberCyan)
        }
    }
}

@Composable
fun WeatherStat(label: String, value: String, color: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, style = MaterialTheme.typography.labelSmall)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ScheduleCard(session: SessionDto?, meetingSessions: List<SessionDto> = emptyList()) {
    val sessionsToShow = if (meetingSessions.isNotEmpty()) {
        meetingSessions.sortedBy { it.dateStart }
    } else if (session != null) {
        listOf(session)
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "WEEKEND SCHEDULE",
            color = TextTertiary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (sessionsToShow.isEmpty()) {
            Text(
                text = "No schedule available",
                color = TextSecondary,
                fontSize = 14.sp
            )
        } else {
            sessionsToShow.forEachIndexed { index, s ->
                val dateInfo = try {
                    val start = java.time.ZonedDateTime.parse(s.dateStart)
                    val dayOfWeek = start.dayOfWeek.name.take(3)
                    val time = String.format("%02d:%02d", start.hour, start.minute)
                    Pair(dayOfWeek, time)
                } catch (e: Exception) {
                    Pair("---", "--:--")
                }

                val status = try {
                    val now = java.time.Instant.now()
                    val start = java.time.Instant.parse(s.dateStart)
                    val end = java.time.Instant.parse(s.dateEnd)
                    when {
                        now.isAfter(end) -> "COMPLETED"
                        now.isAfter(start) -> "LIVE"
                        else -> "UPCOMING"
                    }
                } catch (e: Exception) { "UPCOMING" }

                val isRace = s.sessionType == "Race"
                val isActive = s.sessionKey == session?.sessionKey

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
                                .width(3.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        status == "LIVE" -> CyberCyan
                                        status == "COMPLETED" -> TextTertiary
                                        isRace -> F1Red
                                        else -> BorderMedium
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = s.sessionName.uppercase(),
                                color = if (status == "COMPLETED") TextTertiary else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isRace || isActive) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "${dateInfo.first} ${s.sessionType}",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = dateInfo.second,
                            color = if (status == "COMPLETED") TextTertiary else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                        Text(
                            text = status,
                            color = when (status) {
                                "LIVE" -> CyberCyan
                                "COMPLETED" -> TextTertiary
                                else -> F1Red
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (index < sessionsToShow.size - 1) {
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            }
        }
    }
}
