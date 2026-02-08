package com.dhruvathaide.gridly.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.R

@Composable
fun AnimatedNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        NavItem(R.drawable.ic_flag_checkered, "Home"),
        NavItem(R.drawable.ic_speed, "Pit Wall"),
        NavItem(R.drawable.ic_trophy, "Standings"),
        NavItem(R.drawable.ic_circuit, "Circuit"),
        NavItem(R.drawable.ic_settings, "Settings")
    )

    // Cyberpunk Glass Background
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(72.dp) // Reduced height
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(36.dp),
                spotColor = Color(0xFF00E5FF).copy(alpha = 0.6f) // Sharper glow
            )
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE60F172A), // Deep Blue/Black 90%
                        Color(0xFF020617)  // Darker bottom 100%
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                
                val isSelected = selectedItem == index
                
                // Scale Animation
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                // Text Alpha Animation
                val textAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.6f,
                    label = "textAlpha"
                )

                // Color Animation
                val contentColor = if (isSelected) Color(0xFF00E5FF) else Color.White
                
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing background indicator for selected item
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            // Outer soft glow
                             Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        spotColor = Color(0xFF00E5FF)
                                    )
                                    .background(
                                        color = Color(0xFF00E5FF).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            )
                            // Inner intense glow
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF00E5FF).copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title,
                            tint = contentColor.copy(alpha = if(isSelected) 1f else 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale)
                        )
                    }

                    // Text Label
                    Text(
                        text = item.title,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier
                            .alpha(textAlpha)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

data class NavItem(val icon: Int, val title: String)
