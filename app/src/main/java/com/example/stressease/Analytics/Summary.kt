package com.example.stressease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.Leaderboard.Leaderboard
import com.example.stressease.LocalStorageOffline.SharedPreference

class Summary: AppCompatActivity() {
    private lateinit var tvSummaryTitle: TextView
    private lateinit var tvTotalChats: TextView
    private lateinit var tvTotalMoods: TextView
    private lateinit var tvMostCommonEmotion: TextView
    private lateinit var tvMostCommonMood: TextView
    private lateinit var tvOverallStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary)

        tvSummaryTitle = findViewById(R.id.tvSummaryTitle)
        tvTotalChats = findViewById(R.id.tvTotalChats)
        tvTotalMoods = findViewById(R.id.tvTotalMoods)
        tvMostCommonEmotion = findViewById(R.id.tvMostCommonEmotion)
        tvMostCommonMood = findViewById(R.id.tvMostCommonMood)
        tvOverallStatus = findViewById(R.id.tvOverallStatus)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnNext = findViewById<Button>(R.id.btnNext)

        val chatHistory = SharedPreference.loadChatList(this, "chat_history")
        val moodHistory = SharedPreference.loadStringList(this, "mood_history")

        val totalChats = chatHistory.size
        val totalMoods = moodHistory.size


        val emotionCounts = chatHistory.groupingBy { it.emotion }.eachCount()
        val mostCommonEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "None"


        val moodCounts = moodHistory.groupingBy { it }.eachCount()
        val mostCommonMood = moodCounts.maxByOrNull { it.value }?.key ?: "None"

        val overallStatus = when {
            (emotionCounts["Positive"] ?: 0) > (emotionCounts["Negative"] ?: 0) -> "Mostly Positive 😊"
            (emotionCounts["Negative"] ?: 0) > (emotionCounts["Positive"] ?: 0) -> "Mostly Negative 😟"
            else -> "Mixed Mood ⚖️"
        }
        tvSummaryTitle.text = "Overall Mood & Chat Summary"
        tvTotalChats.text = "Total Chats: $totalChats"
        tvTotalMoods.text = "Total Moods Logged: $totalMoods"
        tvMostCommonEmotion.text = "Most Common Emotion: $mostCommonEmotion"
        tvMostCommonMood.text = "Most Common Mood: $mostCommonMood"
        tvOverallStatus.text = "Overall Status: $overallStatus"
        btnBack.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
            finish()
        }
        btnNext.setOnClickListener {
            startActivity(Intent(this, Leaderboard::class.java))
            finish()
        }
    }

}

