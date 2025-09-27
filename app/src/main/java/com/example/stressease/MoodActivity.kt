package com.example.stressease

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MoodActivity: AppCompatActivity() {
    private lateinit var moodInput: EditText
    private lateinit var analyzeBtn: Button
    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        moodInput = findViewById(R.id.moodInput)
        analyzeBtn = findViewById(R.id.analyzeBtn)
        resultView = findViewById(R.id.resultView)

        // Load last saved mood analysis on start
        loadLastMood()

        analyzeBtn.setOnClickListener {
            val moodText = moodInput.text.toString().trim()
            if (moodText.isNotEmpty()) {
                sendMoodToApi(moodText)
            } else {
                Toast.makeText(this, "Please enter your mood", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMoodToApi(userMood: String) {
        val request = mapOf("mood" to userMood)

        RetrofitClient.api.logMood(request).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val emotion = body?.get("emotion") ?: "Unknown"
                    val suggestion = body?.get("suggestion") ?: "No suggestion"

                    val resultText = "Detected Mood: $emotion\nSuggestion: $suggestion"
                    resultView.text = resultText

                    // Save locally with SharedPreferences
                    val sharedPref = getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit()
                        .putString("last_mood", userMood)
                        .putString("last_emotion", emotion)
                        .putString("last_suggestion", suggestion)
                        .apply()

                    Toast.makeText(applicationContext, "Mood saved ✅", Toast.LENGTH_SHORT).show()
                } else {
                    resultView.text = "Error: ${response.code()} ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                resultView.text = "Failed: ${t.message}"
            }
        })
    }

    private fun loadLastMood() {
        val sharedPref = getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
        val lastMood = sharedPref.getString("last_mood", null)
        val lastEmotion = sharedPref.getString("last_emotion", null)
        val lastSuggestion = sharedPref.getString("last_suggestion", null)

        if (lastMood != null && lastEmotion != null && lastSuggestion != null) {
            resultView.text = "Last Mood: $lastMood\nDetected Emotion: $lastEmotion\nSuggestion: $lastSuggestion"
        }
    }
}

