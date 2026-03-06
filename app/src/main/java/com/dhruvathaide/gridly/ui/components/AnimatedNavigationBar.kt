package com.dhruvathaide.gridly.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.ui.theme.CyberCyan
import com.dhruvathaide.gridly.ui.theme.DarkAsphalt
import com.dhruvathaide.gridly.ui.theme.F1Red
import com.dhruvathaide.gridly.ui.theme.TextSecondary
import com.dhruvathaide.gridly.ui.theme.TextTertiary

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

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(64.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141422),
                        Color(0xFF0D0D18)
                    )
                )
            )
    ) {
        // Subtle top border glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            F1Red.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == index

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                val iconAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.45f,
                    label = "iconAlpha"
                )

                val textAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    label = "textAlpha"
                )

                Column(
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
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        tint = if (isSelected) CyberCyan else TextSecondary,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(scale)
                            .alpha(iconAlpha)
                    )

                    if (isSelected) {
                        Text(
                            text = item.title.uppercase(),
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .alpha(textAlpha)
                                .padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(val icon: Int, val title: String)
