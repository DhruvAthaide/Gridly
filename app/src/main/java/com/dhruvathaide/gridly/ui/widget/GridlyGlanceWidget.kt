package com.dhruvathaide.gridly.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class GridlyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GridlyGlanceWidget()
}

class GridlyGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In a real app, fetch data here or observe a repository
        val sessionType = "RACE" // Mocked
        val topDrivers = listOf("VER", "NOR", "LEC", "HAM", "PIA")

        provideContent {
            GridlyGlanceTheme(sessionType) {
                WidgetContent(sessionType, topDrivers)
            }
        }
    }

    @Composable
    private fun WidgetContent(sessionType: String, topDrivers: List<String>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SESSION: $sessionType",
                style = TextStyle(color = GlanceTheme.colors.primary),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )

            // Simulated LazyColumn using Column for Widget (Glance LazyColumn exists but normal Column is fine for finite items)
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                topDrivers.forEachIndexed { index, driver ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${index + 1}. $driver",
                            style = TextStyle(color = GlanceTheme.colors.onSurface)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridlyGlanceTheme(sessionType: String, content: @Composable () -> Unit) {
    // Dynamic color based on session type
    val primaryColor = when (sessionType) {
        "PRACTICE" -> Color.Blue
        "QUALIFYING" -> Color.Green
        "RACE" -> Color.Red
        else -> Color.Gray
    }

    val colors = androidx.glance.material3.ColorProviders(
        light = androidx.compose.material3.lightColorScheme(
            primary = primaryColor,
            background = Color.White,
            onSurface = Color.Black
        ),
        dark = androidx.compose.material3.darkColorScheme(
            primary = primaryColor,
            background = Color(0xFF121212),
            onSurface = Color.White
        )
    )

    GlanceTheme(colors = colors) {
        content()
    }
}
