package com.dhruvathaide.gridly.widget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.*
import com.dhruvathaide.gridly.ui.widget.GridlyGlanceWidget
import com.dhruvathaide.gridly.widget.RaceCountdownWidget
import com.dhruvathaide.gridly.widget.data.WidgetDataManager
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Update Data
            WidgetDataManager.updateAllData(context)

            // 2. Refresh Widgets
            // Race Countdown
            val raceWidget = RaceCountdownWidget()
            val raceGlanceIds = GlanceAppWidgetManager(context).getGlanceIds(RaceCountdownWidget::class.java)
            raceGlanceIds.forEach { glanceId ->
                raceWidget.update(context, glanceId)
            }

            // Driver Standings
            val standingsWidget = GridlyGlanceWidget()
            val standingsGlanceIds = GlanceAppWidgetManager(context).getGlanceIds(GridlyGlanceWidget::class.java)
            standingsGlanceIds.forEach { glanceId ->
                standingsWidget.update(context, glanceId)
            }
            
            // Constructor Battle
            val constructorWidget = com.dhruvathaide.gridly.widget.ConstructorBattleWidget()
            val constructorGlanceIds = GlanceAppWidgetManager(context).getGlanceIds(com.dhruvathaide.gridly.widget.ConstructorBattleWidget::class.java)
            constructorGlanceIds.forEach { glanceId ->
                constructorWidget.update(context, glanceId)
            }
            
            // Latest Intel
            val newsWidget = com.dhruvathaide.gridly.widget.LatestIntelWidget()
            val newsGlanceIds = GlanceAppWidgetManager(context).getGlanceIds(com.dhruvathaide.gridly.widget.LatestIntelWidget::class.java)
            newsGlanceIds.forEach { glanceId ->
                newsWidget.update(context, glanceId)
            }
            
            // Live Pit Wall
            val pitWallWidget = com.dhruvathaide.gridly.widget.LivePitWallWidget()
            val pitWallGlanceIds = GlanceAppWidgetManager(context).getGlanceIds(com.dhruvathaide.gridly.widget.LivePitWallWidget::class.java)
            pitWallGlanceIds.forEach { glanceId ->
                pitWallWidget.update(context, glanceId)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "gridly_widget_update_work"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES // Minimum interval
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
        
        fun updateNow(context: Context) {
             val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .build()
             WorkManager.getInstance(context).enqueue(request)
        }
    }
}
