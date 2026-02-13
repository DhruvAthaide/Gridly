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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dhruvathaide.gridly.MainActivity
import com.dhruvathaide.gridly.widget.data.WidgetDataManager
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.data.remote.model.RaceControlDto
import com.dhruvathaide.gridly.widget.worker.WidgetUpdateWorker

class LivePitWallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LivePitWallWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.enqueue(context)
        WidgetUpdateWorker.updateNow(context)
    }
}

class LivePitWallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val session = WidgetDataManager.getLiveSession(context)
        val latestFlag = WidgetDataManager.getLatestFlag(context)
        val standings = WidgetDataManager.getDriverStandings(context).sortedBy { it.position }.take(3)
        val drivers = WidgetDataManager.getDrivers(context)
        
        provideContent {
             GlanceTheme {
                WidgetContent(session, latestFlag, standings, drivers)
            }
        }
    }

    @Composable
    private fun WidgetContent(
        session: SessionDto?, 
        flag: RaceControlDto?, 
        standings: List<com.dhruvathaide.gridly.data.remote.model.DriverStandingDto>,
        drivers: List<com.dhruvathaide.gridly.data.remote.model.DriverDto>
    ) {
        val flagColor = when(flag?.flag) {
            "RED" -> Color(0xFFE10600)
            "YELLOW" -> Color(0xFFFFD700)
            "DOUBLE YELLOW" -> Color(0xFFFFD700)
            "GREEN" -> Color(0xFF00D26A)
            else -> Color(0xFFE10600) // Default to Gridly Red if unknown or null (brand color)
        }
        
        val headerText = flag?.flag ?: "PIT WALL"
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF121212)))
                .clickable(
                    actionStartActivity(
                        android.content.Intent(
                             androidx.glance.LocalContext.current, 
                             MainActivity::class.java
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header (Status Bar)
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ColorProvider(flagColor))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerText,
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = session?.sessionType?.uppercase() ?: "LIVE",
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            if (standings.isEmpty()) {
                 Text(
                     text = "WAITING FOR DATA...",
                     style = TextStyle(color = ColorProvider(Color.Gray))
                 )
            } else {
                 Column(modifier = GlanceModifier.fillMaxWidth()) {
                    standings.forEach { standing ->
                        val driver = drivers.find { it.driverNumber == standing.driverNumber }
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
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
                                text = driver?.nameAcronym ?: "UNK",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                             Text(
                                text = "Int: --", // Interval handling needs more complex logic/API
                                style = TextStyle(
                                    color = ColorProvider(Color.Gray),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
