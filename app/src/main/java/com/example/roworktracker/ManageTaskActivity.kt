package com.example.roworktracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ManageTaskActivity : AppCompatActivity() {

    // UI elements
    private lateinit var taskNameTextView: TextView
    private lateinit var taskStatusTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var deleteButton: ImageButton
    private lateinit var backBtn:ImageButton

    // Task data
    private var taskName: String = ""
    private var taskStatus: String = ""
    private var timeRemaining: Long = 60000L
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
        backBtn = findViewById(R.id.imageButtonBack2)

        // Retrieve task data from intent
        taskName = intent.getStringExtra("taskName") ?: ""

        // Load saved task data from SharedPreferences
        loadTaskData()

        // Set task details in UI
        taskNameTextView.text = taskName
        taskStatusTextView.text = taskStatus
        updateTimerText(timeRemaining)

        // Register a broadcast receiver to update UI when the timer changes
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, IntentFilter("TIMER_UPDATED"))

        // Handle start/pause button click
        startPauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimerService()
            } else {
                startTimerService(timeRemaining)
            }
        }

        // Handle reset button click
        resetButton.setOnClickListener {
            resetTimerService()
        }

        deleteButton.setOnClickListener {
            if (taskStatus == "Ongoing") {
                // Vibrate and show notice if the task is ongoing
                vibrateAndShowNotice()
            } else {
                handleTaskDeletion()
            }
        }


        backBtn.setOnClickListener(){
            startActivity(Intent(this, ToDosManageActivity::class.java))
        }

        // Handle menu navigation buttons
        handleMenu()
    }

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                timeRemaining = intent.getLongExtra("timeRemaining", 60000L)
                isTimerRunning = intent.getBooleanExtra("isTimerRunning", false)
                val timerFinished = intent.getBooleanExtra("timerFinished", false)

                // Update status based on timer state
                taskStatus = when {
                    timerFinished -> "Completed"
                    isTimerRunning -> "Ongoing"
                    else -> "Paused"
                }

                // Update the UI elements
                updateTimerText(timeRemaining)
                taskStatusTextView.text = taskStatus
                startPauseButton.text = if (isTimerRunning) "Pause Time" else "Start Time"
                startPauseButton.setBackgroundColor(ContextCompat.getColor(this@ManageTaskActivity, if (isTimerRunning) R.color.red else R.color.blue))

                // Save the updated status in SharedPreferences
                updateTaskStateInPreferences()
            }
        }
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

    private fun startTimerService(timeRemaining: Long) {
        val intent = Intent(this, TimerService::class.java)
        intent.action = "START_TIMER"
        intent.putExtra("timeRemaining", timeRemaining)
        ContextCompat.startForegroundService(this, intent)

        // Update status and save to SharedPreferences
        taskStatus = "Ongoing"
        taskStatusTextView.text= "Ongoing"
        updateTaskStateInPreferences()
    }

    private fun pauseTimerService() {
        val intent = Intent(this, TimerService::class.java)
        intent.action = "PAUSE_TIMER"
        ContextCompat.startForegroundService(this, intent)

        // Update status and save to SharedPreferences
        taskStatus = "Paused"
        isTimerRunning = false // Update the state here
        startPauseButton.text = "Start Time" // Update the button text immediately after pausing
        taskStatusTextView.text= "Paused"
        startPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
        updateTaskStateInPreferences()
    }

    private fun resetTimerService() {
        val intent = Intent(this, TimerService::class.java)
        intent.action = "RESET_TIMER"
        ContextCompat.startForegroundService(this, intent)

        // Update status and save to SharedPreferences
        taskStatus = "Pending"
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

    private fun vibrateAndShowNotice() {
        // Vibrate for 500 milliseconds
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(500)
            }
        }

        // Show a toast message
        Toast.makeText(this, "Cannot delete an ongoing task!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver)
    }
}
