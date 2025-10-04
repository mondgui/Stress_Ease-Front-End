package com.example.stressease

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MoodActivity: AppCompatActivity() {
    private lateinit var mood: Spinner
    private lateinit var analyzeBtn: Button
    private lateinit var resultView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        mood = findViewById(R.id.spinnerMood)
        analyzeBtn = findViewById(R.id.analyzeBtn)
        resultView = findViewById(R.id.resultView)

        val back: Button = findViewById(R.id.btnBack)
        val next: Button = findViewById(R.id.btnNext)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.mood_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mood.adapter = adapter


        loadLastMood()

        back.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
            finish()
        }

        next.setOnClickListener {
            val selectedMood = mood.selectedItem?.toString()
            if (selectedMood.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
            } else {
                // Save before navigating
                saveMood(selectedMood)
                val oldMoods = SharedPreference.loadStringList(this, "mood_history").toMutableList()
                oldMoods.add(selectedMood)
                SharedPreference.saveStringList(this, "mood_history", oldMoods)

                startActivity(Intent(this, ChatActivity::class.java))
                finish()
            }
        }

        analyzeBtn.setOnClickListener {
            val selectedMood = mood.selectedItem?.toString()
            if (selectedMood.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveMood(selectedMood)

            val oldMoods = SharedPreference.loadStringList(this, "mood_history").toMutableList()
            oldMoods.add(selectedMood)
            SharedPreference.saveStringList(this, "mood_history", oldMoods)

            Toast.makeText(this, "Mood saved: $selectedMood", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadLastMood() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("moods")

            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val lastMood = documents.documents[0].getString("mood")
                    val adapter = mood.adapter
                    if (lastMood != null && adapter != null) {
                        for (i in 0 until adapter.count) {
                            if (adapter.getItem(i).toString() == lastMood) {
                                mood.setSelection(i)
                                break
                            }
                        }
                    }
                }
            }
    }

    private fun saveMood(selectedMood: String) {
        val userId = auth.currentUser?.uid ?: return

        val moodData = hashMapOf(
            "mood" to selectedMood,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users")
            .document(userId)
            .collection("moods")
            .add(moodData)
            .addOnSuccessListener {
                Toast.makeText(this, "Mood saved: $selectedMood", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save mood", Toast.LENGTH_SHORT).show()
            }
    }
}
