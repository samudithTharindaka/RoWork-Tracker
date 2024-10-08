package com.example.roworktracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class TimerService : Service() {

    private var timeRemaining: Long = 0L
    private var isTimerRunning = false
    private var timer: CountDownTimer? = null
    private val CHANNEL_ID = "TimerServiceChannel"
    private var hasSent30SecondNotification = false // Flag to track 30-second notification

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "START_TIMER" -> {
                timeRemaining = intent.getLongExtra("timeRemaining", 60000L)
                hasSent30SecondNotification = false // Reset flag when starting timer
                startTimer()
            }
            "PAUSE_TIMER" -> pauseTimer()
            "RESET_TIMER" -> resetTimer()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                saveRemainingTimeToPreferences(timeRemaining) // Save remaining time
                sendTimerUpdate()

                // Check if remaining time is 30 seconds and notification has not been sent yet
                if (timeRemaining <= 30000 && !hasSent30SecondNotification) {
                    send30SecondNotification()
                    hasSent30SecondNotification = true // Update flag
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                sendTimerUpdate(true)
                stopSelf()
            }
        }.start()
        isTimerRunning = true
        updateNotification("Timer is running")
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        saveRemainingTimeToPreferences(timeRemaining) // Save remaining time when paused
        updateNotification("Timer is paused")
    }

    private fun resetTimer() {
        pauseTimer()
        timeRemaining = 60000L
        hasSent30SecondNotification = false // Reset flag when timer is reset
        saveRemainingTimeToPreferences(timeRemaining) // Reset saved time
        sendTimerUpdate()
    }

    private fun sendTimerUpdate(finished: Boolean = false) {
        val intent = Intent("TIMER_UPDATED")
        intent.putExtra("timeRemaining", timeRemaining)
        intent.putExtra("isTimerRunning", isTimerRunning)
        intent.putExtra("timerFinished", finished)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // Notify the widget to update
        val widgetUpdateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        sendBroadcast(widgetUpdateIntent)
    }

    private fun updateNotification(content: String) {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Task Timer")
            .setContentText(content)
            .setSmallIcon(R.drawable.group_23)
            .build()
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    // Function to save remaining time to SharedPreferences
    private fun saveRemainingTimeToPreferences(time: Long) {
        val sharedPreferences = getSharedPreferences("timerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("remainingTime", time)
        editor.apply()
    }

    // Function to send 30-second remaining notification
    private fun send30SecondNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Task Timer")
            .setContentText("Only 30 seconds remaining!")
            .setSmallIcon(R.drawable.group_23)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority for important alerts
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification) // Use a different notification ID to avoid conflict with startForeground
    }
}
