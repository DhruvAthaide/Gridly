package com.dhruvathaide.gridly.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dhruvathaide.gridly.R
import android.graphics.Color
import androidx.glance.appwidget.cornerRadius
import com.dhruvathaide.gridly.widget.data.WidgetDataManager
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RaceCountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch data via manager (which hits cache or API)
        // Ideally the Worker updates the cache, and we just read here.
        // But `provideGlance` re-composes when `update` is called.
        val nextRace = WidgetDataManager.getNextRace(context)
        
        provideContent {
            RaceWidgetContent(nextRace)
        }
    }

    @Composable
    private fun RaceWidgetContent(session: SessionDto?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.parseColor("#121212")))
                .padding(12.dp)
                .cornerRadius(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (session != null) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Image(
                        provider = ImageProvider(R.drawable.ic_flag_checkered),
                        contentDescription = null,
                        modifier = GlanceModifier.width(16.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "NEXT GRAND PRIX",
                        style = TextStyle(
                            color = ColorProvider(Color.parseColor("#AAAAAA")),
                            fontSize = 10.sp
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(8.dp))
    
                Text(
                    text = "${session.location.uppercase()} ", // Flag logic tricky without assets map
                    style = TextStyle(
                        color = ColorProvider(Color.WHITE),
                        fontSize = 18.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
                
                 Text(
                    text = session.sessionName,
                    style = TextStyle(
                        color = ColorProvider(Color.GRAY),
                        fontSize = 12.sp
                    )
                )
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                // Timer Logic
                val now = Instant.now()
                val start = try { Instant.parse(session.dateStart) } catch(e: Exception) { now }
                
                if (start.isAfter(now)) {
                    val days = ChronoUnit.DAYS.between(now, start)
                    val hours = ChronoUnit.HOURS.between(now, start) % 24
                    
                     Text(
                        text = "${days}d ${hours}h",
                        style = TextStyle(
                            color = ColorProvider(Color.parseColor("#FF1801")),
                            fontSize = 24.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold
                        )
                    )
                     Text(
                        text = "LIGHTS OUT",
                        style = TextStyle(
                            color = ColorProvider(Color.GRAY),
                            fontSize = 10.sp
                        )
                    )
                } else {
                     Text(
                        text = "LIVE / CONCLUDED",
                        style = TextStyle(
                            color = ColorProvider(Color.parseColor("#00FF00")),
                            fontSize = 18.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold
                        )
                    )
                }
               
            } else {
                Text(
                    text = "NO UPCOMING RACES",
                    style = TextStyle(color = ColorProvider(Color.GRAY))
                )
            }
        }
    }
}
