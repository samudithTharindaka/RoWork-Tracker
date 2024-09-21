package com.example.roworktracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class ManageTaskActivity : AppCompatActivity() {

    // UI elements
    private lateinit var taskNameTextView: TextView
    private lateinit var taskStatusTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private  lateinit var deleteButton:ImageButton

    // Constants
    private val CHANNEL_ID = "task_notification_channel"

    // Task data
    private var taskName: String = ""
    private var taskStatus: String = ""
    private var timeRemaining: Long = 0L
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_task)

        // Link UI elements to XML layout
        taskNameTextView = findViewById(R.id.nameOfTask)
        taskStatusTextView = findViewById(R.id.statusOfTask)
        timerTextView = findViewById(R.id.textViewTimeRemaining)
        startPauseButton = findViewById(R.id.buttonStartPause)
        resetButton = findViewById(R.id.buttonReset)
        deleteButton = findViewById(R.id.deleteBtn1)

        // Retrieve task data from intent
        taskName = intent.getStringExtra("taskName") ?: ""

        // Load saved task data from SharedPreferences
        loadTaskData()

        // Set task details in UI
        taskNameTextView.text = taskName
        taskStatusTextView.text = taskStatus
        updateTimerText(timeRemaining)

        // Handle start/pause button click
        startPauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        // Handle reset button click
        resetButton.setOnClickListener {
            resetTimer()
        }

        deleteButton.setOnClickListener(){
            handleTaskDeletion()
        }

        // Handle menu navigation buttons
        handleMenu()
    }

    private fun handleMenu() {
        val homeBtn: ImageButton = findViewById(R.id.imageButtonMenu)
        val toDoBtn: ImageButton = findViewById(R.id.imageButton2Menu)
        val settingsBtn: ImageButton = findViewById(R.id.imageButton3Menu)

        homeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
        }

        toDoBtn.setOnClickListener {
            startActivity(Intent(this, ToDosManageActivity::class.java))
            Toast.makeText(this, "To-Do clicked", Toast.LENGTH_SHORT).show()
        }

        settingsBtn.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerText(timeRemaining)
            }

            override fun onFinish() {
                isTimerRunning = false
                taskStatus = "Completed"
                taskStatusTextView.text = taskStatus
                updateTaskStateInPreferences()
                Toast.makeText(this@ManageTaskActivity, "Task completed!", Toast.LENGTH_SHORT).show()
            }
        }.start()
        isTimerRunning = true
        taskStatus = "Ongoing"
        taskStatusTextView.text = taskStatus
        startPauseButton.text = "Pause Time"
        startPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        updateTaskStateInPreferences()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        taskStatus = if (timeRemaining > 0) "Paused" else "Completed"
        taskStatusTextView.text = taskStatus
        startPauseButton.text = "Start Time"
        startPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
        updateTaskStateInPreferences()
    }

    private fun resetTimer() {
        pauseTimer()
        timeRemaining = 60000L // Reset to default 1 minute
        taskStatus = "Pending"
        updateTimerText(timeRemaining)
        updateTaskStateInPreferences()
    }

    private fun updateTimerText(timeInMillis: Long) {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = formattedTime
    }

    private fun loadTaskData() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val existingTasks = sharedPreferences.getString("taskList", "") ?: ""
        val taskData = existingTasks.split("|").find { it.startsWith(taskName) }
        if (taskData != null) {
            val data = taskData.split(";")
            if (data.size == 3) {
                taskStatus = data[1]
                timeRemaining = data[2].toLong()
            } else {
                taskStatus = "Pending"
                timeRemaining = 60000L // Default to 1 minute
            }
        }
    }

    private fun updateTaskStateInPreferences() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val existingTasks = sharedPreferences.getString("taskList", "") ?: return

        val updatedTasks = existingTasks.split("|").map { taskData ->
            val data = taskData.split(";")
            if (data.size == 3 && data[0] == taskName) {
                "${data[0]};$taskStatus;$timeRemaining"
            } else {
                taskData
            }
        }.joinToString("|")

        editor.putString("taskList", updatedTasks)
        editor.apply()
    }

    private fun handleTaskDeletion() {
        if (taskStatus == "Ongoing") {
            vibrateDevice()
            createNotificationChannel()
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.group_23) // Replace with your own icon
                .setContentTitle("Cannot Delete Task")
                .setContentText("Cannot delete an ongoing task.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            NotificationManagerCompat.from(this).notify(1, notification)
            Toast.makeText(this, "Cannot delete ongoing task", Toast.LENGTH_SHORT).show()
        } else {
            deleteTaskFromPreferences()
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ToDosManageActivity::class.java))
            finish()
        }
    }

    private fun deleteTaskFromPreferences() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val existingTasks = sharedPreferences.getString("taskList", "") ?: ""

        val updatedTasks = existingTasks.split("|").filterNot { it.startsWith(taskName) }.joinToString("|")
        editor.putString("taskList", updatedTasks)
        editor.apply()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notification"
            val descriptionText = "Notifications for task management"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
}
