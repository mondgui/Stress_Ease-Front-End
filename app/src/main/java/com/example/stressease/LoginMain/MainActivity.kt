package com.example.stressease.LoginMain

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.stressease.Chats.ChatFragment
import com.example.stressease.MoodFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.stressease.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 🔹 Handle navigation if user came from QuizActivity
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "mood") {
            loadFragment(MoodFragment())
            bottomNav.selectedItemId = R.id.nav_mood
        } else {
            // Default fragment when app starts
            loadFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        // 🔹 Handle icon clicks
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Helper to replace fragment in container
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
