package com.example.roworktracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ToDosManageActivity : AppCompatActivity() {

    private lateinit var editTextTask: EditText
    private lateinit var linearLayout3: LinearLayout
    private lateinit var buttonAddTask: ImageButton
    private lateinit var handler: Handler
    private lateinit var refreshRunnable: Runnable
    private lateinit var backBtn:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_to_dos_manage)

        // Link the UI elements to the XML layout
        editTextTask = findViewById(R.id.editTextTask)
        linearLayout3 = findViewById(R.id.linearLayout3)
        buttonAddTask = findViewById(R.id.buttonAddTask)
        backBtn= findViewById(R.id.imageButtonBack)


        // Load tasks from SharedPreferences
        loadTasksFromPreferences()

        buttonAddTask.setOnClickListener {
            addTask()
        }
        backBtn.setOnClickListener(){
            val intent = Intent(this@ToDosManageActivity, MainActivity::class.java)
            startActivity(intent)
        }

        // Call handleMenu function
        handleMenu()


        // Initialize the Handler and Runnable for refreshing the UI
        handler = Handler(Looper.getMainLooper())
        refreshRunnable = object : Runnable {
            override fun run() {
                // Refresh the tasks or update the UI every second
                loadTasksFromPreferences()

                // Schedule the next update after 1 second (1000 milliseconds)
                handler.postDelayed(this, 1000)
            }
        }

        // Start the repeated refresh every second
        handler.post(refreshRunnable)
    }

    private fun addTask() {
        val taskName = editTextTask.text.toString().trim()
        val taskStatus = "Pending" // Default status when a task is added
        val timeRemaining = 60000L // Default time remaining, e.g., 1 minute in milliseconds
       val piority = false

        if (taskName.isNotEmpty()) {
            val taskData = Task(taskName, taskStatus, timeRemaining,piority)
            saveTaskToPreferences(taskData)
            loadTasksFromPreferences() // Reload to reflect new task in the UI
            editTextTask.setText("") // Clear the input field
        } else {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMenu() {
        val homeBtn: ImageButton = findViewById(R.id.imageButtonMenu)
        val toDoBtn: ImageButton = findViewById(R.id.imageButton2Menu)
        val settingsBtn: ImageButton = findViewById(R.id.imageButton3Menu)

        homeBtn.setOnClickListener {

            try {
                val intent = Intent(this@ToDosManageActivity, MainActivity::class.java)
                startActivity(intent)

            } catch (e: Exception) {
                Log.e("todo", "Error starting Intent: ${e.message}")
            }
            Toast.makeText(this, "home clicked ", Toast.LENGTH_SHORT).show()
        }

        toDoBtn.setOnClickListener {
            Log.d("todo", "Settings button clicked")

            Toast.makeText(this, "You are in the Todo page ", Toast.LENGTH_SHORT).show()
        }

        settingsBtn.setOnClickListener {
            Log.d("todo", "Settings button clicked")
            Toast.makeText(this, "Settings clicked ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTaskToPreferences(task: Task) {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing tasks
        val existingTasks = sharedPreferences.getString("taskList", "") ?: ""

        // Append the new task
        val updatedTasks = if (existingTasks.isNotEmpty()) {
            "$existingTasks|${task.name};${task.status};${task.timeRemaining}"
        } else {
            "${task.name};${task.status};${task.timeRemaining}"
        }

        // Save the updated tasks back to SharedPreferences
        editor.putString("taskList", updatedTasks)
        editor.apply()
    }

    private fun loadTasksFromPreferences() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val taskList = sharedPreferences.getString("taskList", "") ?: return

        linearLayout3.removeAllViews() // Clear existing views to avoid duplicates
        Log.d("ToDosManageActivity", "Loaded tasks: $taskList")

        taskList.split("|").forEach { taskData ->
            val data = taskData.split(";")
            if (data.size == 3) {
                val taskName = data[0].trim()
                val taskStatus = data[1].trim()
                val timeRemaining = data[2].trim().toLong()

                // Inflate the task item layout
                val taskView = layoutInflater.inflate(R.layout.task_item, linearLayout3, false)

                // Get references to the UI elements
                val taskTextView: TextView = taskView.findViewById(R.id.textViewTask)
                val deleteButton: ImageButton = taskView.findViewById(R.id.buttonDeleteTask)
                val startButton: ImageButton = taskView.findViewById(R.id.buttonStartTask)

                // Set task information
                taskTextView.text = "$taskName ($taskStatus)"

                // Set up button click listeners
                deleteButton.setOnClickListener {
                    // Handle delete task
                    deleteTask(taskData)
                }

                startButton.setOnClickListener {
                    // Handle start task
                    startTask(taskName, taskStatus, timeRemaining)
                }

                taskTextView.setOnClickListener {
                    // Handle edit task on text click
                    showEditTaskDialog(taskData)
                }

                // Add the inflated view to the main LinearLayout
                linearLayout3.addView(taskView)
            }
        }
    }

    private fun showEditTaskDialog(taskData: String) {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Parse the existing task data
        val data = taskData.split(";")
        if (data.size == 3) {
            val currentTaskName = data[0].trim()
            val currentTaskStatus = data[1].trim()
            val currentTimeRemaining = data[2].trim().toLong()

            // Create the dialog
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
            val editTextTaskName: EditText = dialogView.findViewById(R.id.editTextTaskName)
            val editTextTaskTime: EditText = dialogView.findViewById(R.id.editTextTaskTime)

            // Set current values in the dialog
            editTextTaskName.setText(currentTaskName)
            editTextTaskTime.setText(currentTimeRemaining.toString())

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    // Get new values from the dialog
                    val newTaskName = editTextTaskName.text.toString().trim()
                    val newTimeRemaining = editTextTaskTime.text.toString().trim().toLongOrNull() ?: currentTimeRemaining

                    // Create the new task data string
                    val newTaskData = "$newTaskName;$currentTaskStatus;$newTimeRemaining"

                    // Replace the old task data with the new one
                    val updatedTasks = sharedPreferences.getString("taskList", "")?.split("|")
                        ?.map { if (it.trim() == taskData.trim()) newTaskData else it }
                        ?.joinToString("|")

                    // Save the updated tasks back to SharedPreferences
                    editor.putString("taskList", updatedTasks)
                    editor.apply()

                    // Refresh the UI to reflect changes
                    loadTasksFromPreferences()
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }
    }

    private fun deleteTask(taskData: String) {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing tasks
        val existingTasks = sharedPreferences.getString("taskList", "") ?: ""

        // Remove the task (using "|" as the delimiter)
        val updatedTasks = existingTasks.split("|")
            .filter { it.trim() != taskData.trim() }
            .joinToString("|")

        // Save the updated tasks back to SharedPreferences
        editor.putString("taskList", updatedTasks)
        editor.apply()

        // Reload the tasks to reflect the changes in the UI
        loadTasksFromPreferences()
    }

    private fun startTask(taskName: String, taskStatus: String, timeRemaining: Long) {
        // Handle the start task logic, e.g., navigate to another activity
        val intent = Intent(this, ManageTaskActivity::class.java).apply {
            putExtra("taskName", taskName)
            putExtra("taskStatus", taskStatus)
           putExtra("timeRemaining", timeRemaining)
        }
        startActivity(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Remove callbacks to stop the refreshing when the activity is destroyed
        handler.removeCallbacks(refreshRunnable)
    }

//    data class Task(
//        val name: String,
//        val status: String,
//        val timeRemaining: Long
//    )
}
