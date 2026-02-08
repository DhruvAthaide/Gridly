package com.dhruvathaide.gridly.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dhruvathaide.gridly.R

@Composable
fun TrackMap(
    modifier: Modifier = Modifier
) {
    // Simulated track path (rounded rectangleish for demo)
    // In a real app, this would be SVG path data mapped to the image
    
    val infiniteTransition = rememberInfiniteTransition(label = "trackAnimation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Box(
        modifier = modifier
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Track Image Background
        // Using a generic track placeholder if specific one isn't dynamic. 
        // For now, let's use Monaco as a high-fidelity example if available, or just a placeholder.
        // Assuming track_monaco.png exists from previous file list.
        Image(
            painter = painterResource(id = R.drawable.track_monaco),
            contentDescription = "Track Map",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw a simulated path overlay
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.8f)
                cubicTo(
                    size.width * 0.1f, size.height * 0.5f,
                    size.width * 0.1f, size.height * 0.2f,
                    size.width * 0.5f, size.height * 0.2f
                )
                cubicTo(
                    size.width * 0.9f, size.height * 0.2f,
                    size.width * 0.9f, size.height * 0.5f,
                    size.width * 0.8f, size.height * 0.8f
                )
                cubicTo(
                    size.width * 0.6f, size.height * 0.9f,
                    size.width * 0.4f, size.height * 0.9f,
                    size.width * 0.2f, size.height * 0.8f
                )
                close()
            }
            
            // Draw Track Line
            drawPath(
                path = path,
                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                style = Stroke(width = 4.dp.toPx())
            )

            // Animate Dot
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(path, false)
            
            // Compose PathMeasure uses getPosition() returning an Offset
            val position = pathMeasure.getPosition(pathMeasure.length * progress)

            drawCircle(
                color = Color(0xFFFF4081),
                radius = 8.dp.toPx(),
                center = position
            )
            
            // Glow effect
            drawCircle(
                color = Color(0xFFFF4081).copy(alpha = 0.4f),
                radius = 12.dp.toPx(),
                center = position
            )
        }
    }
}
