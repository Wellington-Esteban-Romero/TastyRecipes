package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.tasty.recipes.R

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_started)

        findViewById<RelativeLayout>(R.id.get_started_button).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}