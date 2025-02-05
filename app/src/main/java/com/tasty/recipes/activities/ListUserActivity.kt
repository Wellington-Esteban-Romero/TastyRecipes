package com.tasty.recipes.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasty.recipes.R
import com.tasty.recipes.adapters.ListUserAdapter
import com.tasty.recipes.adapters.UsersAdapter
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ActivityListUserBinding
import com.tasty.recipes.databinding.ActivityMainBinding

class ListUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListUserBinding
    private lateinit var listUserAdapter: ListUserAdapter

    private val userList: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupRecyclerViews()
    }

    private fun setupRecyclerViews () {
        listUserAdapter = ListUserAdapter(userList) {

        }

        binding.rvListUsers.apply {
            layoutManager =
                LinearLayoutManager(this@ListUserActivity, LinearLayoutManager.HORIZONTAL,
                    false)
            adapter = listUserAdapter
        }
    }
}