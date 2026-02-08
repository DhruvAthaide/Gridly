package com.dhruvathaide.gridly.ui.dashboard.tabs

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.DashboardUiState
import java.io.IOException

@Composable
fun RadioTab(
    viewModel: MainViewModel,
    state: DashboardUiState
) {
    // 1. Fetch on load
    LaunchedEffect(state.activeSession) {
        state.activeSession?.let {
            viewModel.refreshTeamRadio(it.sessionKey)
        }
    }

    // 2. Audio Player State
    val mediaPlayer = remember { MediaPlayer() }
    var currentPlayingUrl by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    fun playAudio(url: String) {
        try {
            if (currentPlayingUrl == url && isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
            } else {
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { 
                    it.start() 
                    currentPlayingUrl = url
                    isPlaying = true
                }
                mediaPlayer.setOnCompletionListener {
                    isPlaying = false
                    currentPlayingUrl = null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Reset state on error
            currentPlayingUrl = null
            isPlaying = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "TEAM RADIO FEED",
            color = Color(0xFF00E5FF),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            fontFamily = FontFamily.Monospace
        )

        if (state.teamRadioMessages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO RADIO CHATTER",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                     Text(
                        text = if (state.activeSession == null) "WAITING FOR ACTIVE SESSION" else "LISTENING FOR TRANSMISSIONS...",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "AUDIO ONLY FEED (TAP TO PLAY)",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(state.teamRadioMessages) { msg ->
                    RadioItem(
                        msg = msg,
                        isPlaying = currentPlayingUrl == msg.recordingUrl && isPlaying,
                        onPlayClick = { playAudio(msg.recordingUrl) }
                    )
                }
            }
        }
    }
}

@Composable
fun RadioItem(
    msg: com.dhruvathaide.gridly.data.remote.model.TeamRadioDto,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .border(1.dp, if (isPlaying) Color(0xFF00E5FF) else Color(0xFF333333), RoundedCornerShape(8.dp))
            .clickable { onPlayClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isPlaying) Color(0xFF00E5FF) else Color(0xFF333333)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = if (isPlaying) Color.Black else Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "DRIVER #${msg.driverNumber}",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AUDIO TRANSMISSION", // No transcript available yet
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = msg.date.substringAfter("T").substringBefore("."),
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
