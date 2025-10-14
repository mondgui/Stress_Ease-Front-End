package com.example.stressease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Suggestions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestion)
         val back:Button=findViewById<Button>(R.id.btnBack)
         val front:Button=findViewById<Button>(R.id.btnNext)
         back.setOnClickListener {
             startActivity(Intent(this, QuizFragment::class.java))
             finish()
         }
         front.setOnClickListener {
             startActivity(Intent(this, MoodFragment::class.java))
             finish()
         }
            val score = intent.getIntExtra("score", 0)
            val suggestion = intent.getStringExtra("suggestion") ?: "No suggestion available"

            val scoreTv = findViewById<TextView>(R.id.tvFinalScore)
            val suggestionTv = findViewById<TextView>(R.id.tvSuggestion)

            scoreTv.text = "Your Stress Score: $score / 100"
            suggestionTv.text = suggestion
    }
}
