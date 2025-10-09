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
import com.example.stressease.LoginMain.MainActivity
import org.json.JSONArray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuizActivity : AppCompatActivity() {

        private var score = 0
        private var currentQ = 0

        private lateinit var auth: FirebaseAuth
        private lateinit var db: FirebaseFirestore

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
            val back = findViewById<Button>(R.id.btnback)

            auth= FirebaseAuth.getInstance()
            db= FirebaseFirestore.getInstance()


            val prefs = getSharedPreferences("StressEasePrefs", MODE_PRIVATE)
            val email = prefs.getString("loggedInUser", "guest") ?: "guest"


            showQuestion(currentQ, questionTv, progressTv, radioGroup, progressBar)


            nextBtn.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId == -1) {
                    showCustomToast("⚠ Please select an answer")
                    return@setOnClickListener
                }

                val selectedIndex = radioGroup.indexOfChild(findViewById(selectedId))
                score += scoresList[selectedIndex]
                currentQ++

                if (currentQ < questions.size) {
                    showQuestion(currentQ, questionTv, progressTv, radioGroup, progressBar)
                } else {
                    saveScore(score)
                    saveScoreToLeaderboard(score)
                    val suggestion = generateSuggestion(score)
                    saveToPrefs(email, score, suggestion)

                    val intent = Intent(this, Suggestions::class.java)
                    intent.putExtra("score", score)
                    intent.putExtra("suggestion", suggestion)
                    startActivity(intent)
                    finish()
                }
            }
            back.setOnClickListener {
                if (currentQ > 0) {
                    currentQ--
                    showQuestion(currentQ, questionTv, progressTv, radioGroup, progressBar)
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
        private fun showQuestion(
            index: Int,
            questionTv: TextView,
            progressTv: TextView,
            radioGroup: RadioGroup,
            progressBar: ProgressBar
        ) {
            questionTv.text = questions[index]
            progressTv.text = "Question ${index + 1}/${questions.size}"
            progressBar.max = questions.size
            progressBar.progress = index + 1
            radioGroup.clearCheck()
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
            prefs.edit().putInt("last_quiz_score_$email", score).apply()

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
        private fun saveScore(score: Int) {
            val userId = auth.currentUser?.uid ?: return

            val scoreData = hashMapOf(
                "score" to score,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("moods")
                .add(scoreData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Score saved: $score", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save score", Toast.LENGTH_SHORT).show()
                }
        }
    private fun saveScoreToLeaderboard(score: Int) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val leaderboardEntry = hashMapOf(
                "username" to (user.email ?: "Unknown"),
                "score" to score
            )

            // Save or update user’s score
            db.collection("leaderboard").document(user.uid)
                .set(leaderboardEntry) // overwrites old score
                .addOnSuccessListener {
                    Toast.makeText(this, "Score updated on leaderboard!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update score: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

