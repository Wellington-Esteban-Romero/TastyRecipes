package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.tasty.recipes.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val progressBar: ProgressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        // Set a timer or perform any startup tasks (e.g., check for updates)
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the splash activity
        }, 500) // Set delay time, e.g., 2 seconds
    }
}