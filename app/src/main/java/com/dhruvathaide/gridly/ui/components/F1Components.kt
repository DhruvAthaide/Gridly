package com.dhruvathaide.gridly.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.ui.theme.CarbonFiber
import com.dhruvathaide.gridly.ui.theme.CyberCyan
import com.dhruvathaide.gridly.ui.theme.DarkAsphalt

// Angled Shape for "Fast" look (skewed headers)
val F1AngledShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width - 20f, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

@Composable
fun PitWallCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Brush.verticalGradient(listOf(Color.Gray.copy(alpha=0.3f), Color.Transparent)), RoundedCornerShape(8.dp))
            .background(CarbonFiber, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (title != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333)) // Header BG
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }
            // Technical Separator Line
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(F1RedStrip))
        }
        
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            content()
        }
    }
}

val F1RedStrip = Brush.horizontalGradient(
    0.0f to Color(0xFFFF1801),
    0.3f to Color(0xFFFF1801),
    0.31f to Color.Transparent
)

@Composable
fun TelemetryValue(
    label: String,
    value: String,
    unit: String = "",
    color: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TechnicalEmptyState(
    message: String = "SYSTEM STANDBY",
    subMessage: String = "AWAITING DATA LINK",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scanAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .background(com.dhruvathaide.gridly.ui.theme.DarkAsphalt) 
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        // 1. Grid Background
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            val step = 40.dp.toPx()
            val w = size.width
            val h = size.height
            
            // Vertical Lines
            var x = 0f
            while (x <= w) {
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, h),
                    strokeWidth = 1f
                )
                x += step
            }
            // Horizontal Lines
            var y = 0f
            while (y <= h) {
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(w, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        // 2. Content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Blinking Status Light
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(com.dhruvathaide.gridly.ui.theme.F1Red.copy(alpha = scanAlpha), androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.displayMedium,
                color = Color.Gray.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "NO LIVE TELEMETRY", // Static technical label
                style = MaterialTheme.typography.labelSmall,
                color = com.dhruvathaide.gridly.ui.theme.F1Red,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier
                    .border(1.dp, com.dhruvathaide.gridly.ui.theme.F1Red, RoundedCornerShape(2.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                fontSize = 10.sp
            )
        }
        
        // 3. Scanning Line Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to CyberCyan.copy(alpha = 0.05f * scanAlpha),
                        1f to Color.Transparent
                    )
                )
        )
    }
}
