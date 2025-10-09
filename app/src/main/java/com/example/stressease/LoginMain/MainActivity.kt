package com.example.stressease.LoginMain

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.Chats.ChatActivity
import com.example.stressease.MoodActivity
import com.example.stressease.QuizActivity
import com.example.stressease.R
import com.example.stressease.ReportsActivity
import com.example.stressease.Summary

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find buttons by ID
        val btnQuiz: Button = findViewById(R.id.btnQuiz)
        val btnMood: Button = findViewById(R.id.btnMood)
        val btnChat: Button = findViewById(R.id.btnChat)
        val btnReports: Button = findViewById(R.id.btnReports)
        val btnJournal: Button = findViewById(R.id.btnJournal)
        val btnSummary: Button = findViewById(R.id.btnSummary)
        val btnBreathing: Button = findViewById(R.id.btnBreathing)
        val btnLogout: Button = findViewById(R.id.btnLogout)

        // Quiz
        btnQuiz.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        // Mood Logging
        btnMood.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }

        // Chat
        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Reports
        btnReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }



        // Summary
        btnSummary.setOnClickListener {
            startActivity(Intent(this, Summary::class.java))
        }


        // Logout
        btnLogout.setOnClickListener {
            // Clear session (optional)
            val prefs = getSharedPreferences("StressEasePrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

