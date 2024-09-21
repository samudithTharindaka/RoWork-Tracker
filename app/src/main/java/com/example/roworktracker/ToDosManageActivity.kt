package com.example.roworktracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_to_dos_manage)

        // Link the UI elements to the XML layout
        editTextTask = findViewById(R.id.editTextTask)
        linearLayout3 = findViewById(R.id.linearLayout3)
        buttonAddTask = findViewById(R.id.buttonAddTask)

        // Load tasks from SharedPreferences
        loadTasksFromPreferences()

        buttonAddTask.setOnClickListener {
            addTask()
        }

        // Call handleMenu function
        handleMenu()
    }

    private fun addTask() {
        val taskText = editTextTask.text.toString().trim()
        if (taskText.isNotEmpty()) {
            val newTask = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = taskText
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.rounded_corners)
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@ToDosManageActivity, R.color.black))
            }

            linearLayout3.addView(newTask)
            saveTaskToPreferences(taskText)
            editTextTask.setText("")
        } else {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMenu() {
        val homeBtn: ImageButton = findViewById(R.id.imageButtonMenu)
        val settingsBtn: ImageButton = findViewById(R.id.imageButton3Menu)
        val todoBtn: ImageButton = findViewById(R.id.imageButton2Menu)

        settingsBtn.setOnClickListener {
            Log.d("MainActivity", "Settings button clicked")
            try {
                val intent = Intent(this@ToDosManageActivity, MainActivity::class.java)
                startActivity(intent)
                Log.d("MainActivity", "Intent started successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting Intent: ${e.message}")
            }
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTaskToPreferences(task: String) {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Append the new task to existing tasks
        val existingTasks = sharedPreferences.getStringSet("taskSet", mutableSetOf()) ?: mutableSetOf()
        existingTasks.add(task)
        editor.putStringSet("taskSet", existingTasks)
        editor.apply()
    }

    private fun loadTasksFromPreferences() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val taskSet = sharedPreferences.getStringSet("taskSet", mutableSetOf()) ?: mutableSetOf()

        taskSet.forEach { task ->
            val newTask = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = task
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.rounded_corners)
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@ToDosManageActivity, R.color.black))
            }
            linearLayout3.addView(newTask)
        }
    }
}
