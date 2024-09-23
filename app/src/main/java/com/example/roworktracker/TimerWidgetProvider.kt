package com.example.roworktracker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class TimerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Retrieve the remaining time from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("timerPrefs", Context.MODE_PRIVATE)
        val remainingTime = sharedPreferences.getLong("remainingTime", 60000L)

        val secondsRemaining = (remainingTime / 1000).toInt()
        val timeText = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60)

        // Create the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.timer)
        views.setTextViewText(R.id.TextTimewi, timeText)

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    // Optionally, handle broadcasts to update the widget
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "TIMER_UPDATED") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(Intent(context, TimerWidgetProvider::class.java).component)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
