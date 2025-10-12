package com.example.stressease.LoginMain

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.stressease.Chats.ChatFragment
import com.example.stressease.MoodFragment
import com.example.stressease.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val CHAT_TAG = "CHAT_FRAGMENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Default to home
        loadFragment(HomeFragment())
        bottomNav.selectedItemId = R.id.nav_home

        // ✅ Handle first-time selection
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
                    loadNewChatFragment() // ✅ Create brand new ChatFragment
                    true
                }

                else -> false
            }
        }

        // ✅ Handle re-selection (user taps Chat again)
        bottomNav.setOnItemReselectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_chat) {
                loadNewChatFragment() // Recreate ChatFragment again
            }
        }
    }

    // ✅ Always creates a new ChatFragment (clean chat)
    private fun loadNewChatFragment() {
        val fm = supportFragmentManager

        // Remove any existing ChatFragment instantly
        val existing = fm.findFragmentByTag(CHAT_TAG)
        if (existing != null) {
            fm.beginTransaction().remove(existing).commitNowAllowingStateLoss()
        }

        // Create and add a new ChatFragment with isNewSession flag
        val newChatFragment = ChatFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isNewSession", true)
            }
        }

        fm.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, newChatFragment, CHAT_TAG)
            .commit()
    }

    // ✅ Generic fragment loader for other tabs
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
