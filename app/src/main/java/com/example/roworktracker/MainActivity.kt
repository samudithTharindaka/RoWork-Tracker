package com.example.roworktracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private lateinit var textViewTimer: TextView
    private val CHANNEL_ID = "SettingsNotificationChannel"

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeRemaining = intent?.getLongExtra("timeRemaining", 0L) ?: 0L
            val isTimerRunning = intent?.getBooleanExtra("isTimerRunning", false) ?: false

            // Update the UI
            updateTimerUI(timeRemaining, isTimerRunning)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewTimer = findViewById(R.id.TextTime1)

        val homeBtn: ImageButton = findViewById(R.id.imageButtonMenu)
        val settingsBtn: ImageButton = findViewById(R.id.imageButton3Menu)
        val todoBtn: ImageButton = findViewById(R.id.imageButton2Menu)

        homeBtn.setOnClickListener {
            Toast.makeText(this, "You are already in home", Toast.LENGTH_SHORT).show()
        }

        todoBtn.setOnClickListener {
            Log.d("MainActivity", "To-Do button clicked")
            startActivity(Intent(this, ToDosManageActivity::class.java))
            Toast.makeText(this, "To-Do clicked", Toast.LENGTH_SHORT).show()
        }

        settingsBtn.setOnClickListener {
            sendSettingsNotification()
            Toast.makeText(this, "No Settings available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Register the receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, IntentFilter("TIMER_UPDATED"))
    }

    override fun onStop() {
        super.onStop()
        // Unregister the receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver)
    }

    private fun updateTimerUI(timeRemaining: Long, isTimerRunning: Boolean) {
        val secondsRemaining = (timeRemaining / 1000).toInt()
        textViewTimer.hint = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60)
        if (!isTimerRunning) {
            textViewTimer.hint = String.format("%02d:%02d (Done)", secondsRemaining / 60, secondsRemaining % 60)
        }
    }

    private fun sendSettingsNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Settings")
            .setContentText("Settings are not available yet.")
            .setSmallIcon(R.drawable.group_23) // Replace with your notification icon
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Settings Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }

}
