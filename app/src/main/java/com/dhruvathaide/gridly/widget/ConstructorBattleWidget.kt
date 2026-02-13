package com.dhruvathaide.gridly.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dhruvathaide.gridly.widget.data.WidgetDataManager
import com.dhruvathaide.gridly.data.remote.model.ConstructorStandingDto
import com.dhruvathaide.gridly.widget.worker.WidgetUpdateWorker

class ConstructorBattleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ConstructorBattleWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.enqueue(context)
        WidgetUpdateWorker.updateNow(context)
    }
}

class ConstructorBattleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val standings = WidgetDataManager.getConstructorStandings(context).sortedBy { it.position }.take(5)
        
        provideContent {
            GlanceTheme {
                WidgetContent(standings)
            }
        }
    }

    @Composable
    private fun WidgetContent(standings: List<ConstructorStandingDto>) {
         Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1E1E1E))) // Darker Gray
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
             // Header
             Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CONSTRUCTORS",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFE10600)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )
             }
             
             if (standings.isEmpty()) {
                 Text(
                     text = "NO DATA AVAILABLE",
                     style = TextStyle(color = ColorProvider(Color.Gray))
                 )
             } else {
                 Column(modifier = GlanceModifier.fillMaxWidth()) {
                    standings.forEach { standing ->
                        ConstructorRow(standing)
                    }
                }
             }
        }
    }
    
    @Composable
    private fun ConstructorRow(standing: ConstructorStandingDto) {
         Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Text(
                text = "${standing.position}",
                style = TextStyle(
                    color = ColorProvider(Color.Gray),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.width(20.dp)
            )
            
            Text(
                text = standing.teamName.uppercase(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                 modifier = GlanceModifier.defaultWeight()
            )
            
            Text(
                text = "${standing.points.toInt()} PTS",
                style = TextStyle(
                    color = ColorProvider(Color.LightGray),
                    fontSize = 12.sp
                )
            )
        }
        

        // Progress Bar
        val maxPoints = 700.0f
        val progress = (standing.points / maxPoints).toFloat().coerceIn(0f, 1f)
        
        androidx.glance.appwidget.LinearProgressIndicator(
            progress = progress,
            modifier = GlanceModifier.fillMaxWidth().height(4.dp),
            color = ColorProvider(Color(0xFFE10600)),
            backgroundColor = ColorProvider(Color.DarkGray)
        )
        
        Spacer(modifier = GlanceModifier.height(4.dp))
    }
}
