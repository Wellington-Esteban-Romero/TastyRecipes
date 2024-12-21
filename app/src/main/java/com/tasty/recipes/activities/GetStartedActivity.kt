package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.tasty.recipes.R
import com.tasty.recipes.utils.AuthHelper

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthHelper().getCurrentUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_get_started)

        findViewById<RelativeLayout>(R.id.get_started_button).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}