package com.dhruvathaide.gridly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dhruvathaide.gridly.ui.theme.DarkAsphalt

@Composable
fun PitWallEmptyScreen(
    message: String = "AWAITING LIVE RACE",
    subMessage: String = "NO DATA AVAILABLE"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAsphalt)
            .padding(16.dp)
    ) {
        TechnicalEmptyState(
            message = message,
            subMessage = subMessage,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize() // Make the TechnicalEmptyState fill the screen for better impact
        )
    }
}
