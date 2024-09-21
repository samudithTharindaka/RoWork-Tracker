package com.example.roworktracker


data class Task(
    val name: String,
    var status: String, // e.g., "Pending", "Completed"
    var timeRemaining: Long // time remaining in milliseconds
)
