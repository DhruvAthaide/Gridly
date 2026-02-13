package com.dhruvathaide.gridly.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.dhruvathaide.gridly.widget.worker.WidgetUpdateWorker

class RaceCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RaceCountdownWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.enqueue(context)
        // Also try an immediate update
        WidgetUpdateWorker.updateNow(context)
    }
}
