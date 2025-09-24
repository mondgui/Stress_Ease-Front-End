package com.example.stressease

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import android.widget.*

class QuizActivity:AppCompatActivity() {

        private var score = 0
        private var currentQ = 0

        private val questions = listOf(
            "I find it hard to relax.",
            "I get upset easily over small things.",
            "I have trouble concentrating on tasks.",
            "I feel nervous or anxious most of the time.",
            "I have difficulty sleeping.",
            "I feel like I cannot control important things in my life.",
            "I feel stressed when I have too much to do.",
            "I often feel sad or low.",
            "I worry about things that may never happen.",
            "I feel tired even after resting."
        )

        private val scoresList = listOf(0, 1, 2, 3) // Never → Always

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_quiz)

            val questionTv = findViewById<TextView>(R.id.tvQuestion)
            val progressTv = findViewById<TextView>(R.id.tvProgress)
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
            val nextBtn = findViewById<Button>(R.id.btnNext)

            updateUI(questionTv, progressTv, radioGroup, progressBar)

            nextBtn.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId == -1) {
                    showCustomToast("⚠ Please select an answer")
                    return@setOnClickListener
                }

                val index = radioGroup.indexOfChild(findViewById(selectedId))
                score += scoresList[index]
                currentQ++

                if (currentQ < questions.size) {
                    updateUI(questionTv, progressTv, radioGroup, progressBar)
                } else {
                    val suggestion = generateSuggestion(score)

                    // Save results for current user
                    val prefs = getSharedPreferences("StressEasePrefs", MODE_PRIVATE)
                    val email = prefs.getString("loggedInUser", "guest") ?: "guest"
                    saveToPrefs(email, score, suggestion)

                    // Go to Result Activity
                    val intent = Intent(this, Suggestions::class.java)
                    intent.putExtra("score", score)
                    intent.putExtra("suggestion", suggestion)
                    startActivity(intent)
                    finish()
                }
            }
        }

        private fun updateUI(
            qTv: TextView,
            pTv: TextView,
            rg: RadioGroup,
            progressBar: ProgressBar
        ) {
            qTv.text = questions[currentQ]
            pTv.text = "Question ${currentQ + 1} of ${questions.size}"

            val progressPercent = ((currentQ + 1).toFloat() / questions.size * 100).toInt()
            progressBar.progress = progressPercent

            rg.clearCheck()
        }

        private fun generateSuggestion(score: Int): String {
            return when {
                score < 10 -> "Low stress. Maintain healthy routines and relaxation."
                score < 20 -> "Moderate stress. Practice mindfulness and journaling."
                else -> "High stress. Seek professional support."
            }
        }
        private fun saveToPrefs(email: String, score: Int, suggestion: String) {
            val prefs = getSharedPreferences("StressEasePrefs", MODE_PRIVATE)
            val quizzes = prefs.getString("quizHistory_$email", "[]")
            val arr = JSONArray(quizzes)

            val newEntry = org.json.JSONObject()
            newEntry.put("score", score)
            newEntry.put("suggestion", suggestion)
            newEntry.put("timestamp", System.currentTimeMillis())
            arr.put(newEntry)

            prefs.edit().putString("quizHistory_$email", arr.toString()).apply()
        }

        private fun showCustomToast(message: String) {
            val inflater: LayoutInflater = layoutInflater
            val layout = inflater.inflate(R.layout.custom_toast, null)
            val tvMessage = layout.findViewById<TextView>(R.id.tvToastMessage)
            tvMessage.text = message

            val toast = Toast(applicationContext)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.show()
        }
}
