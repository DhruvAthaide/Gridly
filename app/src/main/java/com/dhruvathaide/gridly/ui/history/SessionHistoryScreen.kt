package com.dhruvathaide.gridly.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.theme.*

@Composable
fun SessionHistoryScreen(
    viewModel: MainViewModel,
    onSessionSelected: (SessionDto) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSeasonSessions()
    }

    val sessions = state.seasonSessions
    val groupedSessions = sessions.groupBy { it.meetingKey }
        .entries
        .sortedByDescending { it.value.first().dateStart }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAsphalt)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    text = "RACE ARCHIVE",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${groupedSessions.size} EVENTS",
                color = TextTertiary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyberCyan, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("LOADING RACE ARCHIVE...", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedSessions.forEach { (meetingKey, meetingSessions) ->
                    val raceSessions = meetingSessions.sortedBy { it.dateStart }
                    val firstSession = raceSessions.first()
                    val raceSession = raceSessions.find { it.sessionType == "Race" } ?: firstSession

                    item(key = meetingKey) {
                        RaceWeekendCard(
                            circuitName = raceSession.circuitShortName.uppercase(),
                            country = raceSession.countryName,
                            location = raceSession.location,
                            sessions = raceSessions,
                            onSessionClick = onSessionSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceWeekendCard(
    circuitName: String,
    country: String,
    location: String,
    sessions: List<SessionDto>,
    onSessionClick: (SessionDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = circuitName,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$location, $country",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sessions.size} SESSIONS",
                    color = CyberCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                val dateStr = try {
                    val dt = java.time.ZonedDateTime.parse(sessions.first().dateStart)
                    "${dt.dayOfMonth} ${dt.month.name.take(3)} ${dt.year}"
                } catch (e: Exception) { "" }

                Text(
                    text = dateStr,
                    color = TextTertiary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (expanded) {
            HorizontalDivider(color = BorderSubtle)
            sessions.forEach { session ->
                val isRace = session.sessionType == "Race"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClick(session) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isRace) F1Red else TextTertiary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = session.sessionName.uppercase(),
                            color = if (isRace) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (isRace) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Text(
                        text = session.sessionType.uppercase(),
                        color = when (session.sessionType) {
                            "Race" -> F1Red
                            "Qualifying" -> CyberCyan
                            else -> TextTertiary
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
