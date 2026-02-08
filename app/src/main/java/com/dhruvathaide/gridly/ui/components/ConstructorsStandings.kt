package com.dhruvathaide.gridly.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.ui.common.ResourceHelper

@Composable
fun ConstructorsStandingsChart(
    constructors: List<MockDataProvider.ConstructorStanding>
) {
    val maxPoints = constructors.maxOfOrNull { it.points } ?: 1
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(constructors) { team ->
            ConstructorRow(team, maxPoints)
        }
    }
}

@Composable
fun ConstructorRow(team: MockDataProvider.ConstructorStanding, maxPoints: Int) {
    var animate by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        animate = true
    }
    
    val fillFraction by animateFloatAsState(
        targetValue = if (animate) (team.points.toFloat() / maxPoints.toFloat()).coerceAtLeast(0.02f) else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = team.position * 100)
    )
    
    val teamColor = try {
        Color(android.graphics.Color.parseColor("#${team.teamColour}"))
    } catch (e: Exception) { Color.Gray }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header: Logo, Name, Drivers
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${team.position}.",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )
            
            Text(
                text = team.teamName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "${team.points} PTS",
                color = teamColor,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
        
        // Bar Chart Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction)
                    .fillMaxHeight()
                    .background(teamColor)
            )
            
            // Driver Acronyms inside the bar (if it fits) or overlay
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (fillFraction > 0.3f) {
                     Text(
                        text = "${team.driver1} / ${team.driver2}",
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
