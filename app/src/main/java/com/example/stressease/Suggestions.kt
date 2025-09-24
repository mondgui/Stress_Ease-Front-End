package com.example.stressease

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Suggestions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_suggestion)

            val score = intent.getIntExtra("score", 0)
            val suggestion = intent.getStringExtra("suggestion") ?: "No suggestion available"

            val scoreTv = findViewById<TextView>(R.id.tvFinalScore)
            val suggestionTv = findViewById<TextView>(R.id.tvSuggestion)

            scoreTv.text = "Your Stress Score: $score / 100"
            suggestionTv.text = suggestion
    }
}
