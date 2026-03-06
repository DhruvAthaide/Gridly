package com.dhruvathaide.gridly.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.ui.theme.*

val F1AngledShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width - 16f, 0f)
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
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        if (title != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(F1RedStrip)
            )
        }

        Column(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

val F1RedStrip = Brush.horizontalGradient(
    0.0f to F1Red,
    0.35f to F1Red,
    0.36f to Color.Transparent
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
            color = TextTertiary,
            fontSize = 10.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 3.dp)
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
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        // Subtle grid background
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().alpha(0.06f)) {
            val step = 32.dp.toPx()
            val w = size.width
            val h = size.height
            var x = 0f
            while (x <= w) {
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, h),
                    strokeWidth = 0.5f
                )
                x += step
            }
            var y = 0f
            while (y <= h) {
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(w, y),
                    strokeWidth = 0.5f
                )
                y += step
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(F1Red.copy(alpha = pulseAlpha), CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "NO LIVE TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = F1Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .border(1.dp, F1Red.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
