package com.dhruvathaide.gridly.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dhruvathaide.gridly.MainActivity
import com.dhruvathaide.gridly.widget.worker.WidgetUpdateWorker

class LatestIntelWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LatestIntelWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.enqueue(context)
    }
}

class LatestIntelWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In real app, fetch from DB/News Repo. For now, static or simple text.
        // We will just show a "Tap to read latest intel" if valid data isn't ready in WidgetManager.
        // Or we can add `getLatestNews` to WidgetDataManager.
        
        provideContent {
             GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF101010)))
                .clickable(
                    actionStartActivity(
                        android.content.Intent(
                            androidx.glance.LocalContext.current, 
                            MainActivity::class.java
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "LATEST INTEL",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFE10600)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = "Breaking: New Regulations Announced for 2026 Season",
                style = TextStyle(
                     color = ColorProvider(Color.White),
                     fontSize = 16.sp,
                     fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = "Tap to read more on Gridly",
                style = TextStyle(
                    color = ColorProvider(Color.Gray),
                    fontSize = 12.sp
                )
            )
        }
    }
}
