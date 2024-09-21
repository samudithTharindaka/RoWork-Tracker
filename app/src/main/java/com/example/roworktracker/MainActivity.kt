package com.example.roworktracker

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            val homeBtn: ImageButton = this.findViewById(R.id.imageButtonMenu)
            val settingsBtn: ImageButton = this.findViewById(R.id.imageButton3Menu)
            val todoBtn: ImageButton = this.findViewById(R.id.imageButton2Menu)

//        handle the menu
            homeBtn.setOnClickListener() {
                Toast.makeText(this, "You are already in home", Toast.LENGTH_SHORT).show()
            }

            settingsBtn.setOnClickListener() {

                Log.d("MainActivity", "Settings button clicked")
                // Check if Intent works
                try {
                    val intent = Intent(this@MainActivity, ToDosManageActivity::class.java)
                    startActivity(intent)
                    Log.d("MainActivity", "Intent started successfully")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error starting Intent: ${e.message}")
                }
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            }

            todoBtn.setOnClickListener() {
                Toast.makeText(this, "todo clicked", Toast.LENGTH_SHORT).show()
            }

        }catch (e: Exception) {
            Log.e("MainActivity", "Error starting Intent: ${e.message}")
        }
        }



}