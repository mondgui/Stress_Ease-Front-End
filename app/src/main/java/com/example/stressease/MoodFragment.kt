package com.example.stressease

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import org.w3c.dom.Text

class MoodFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)

    }
    private lateinit var switchMoodQuiz : SwitchMaterial
    private lateinit var spinnerMood: Spinner
    private lateinit var analyzeBtn:Button
    private lateinit var result: TextView

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        spinnerMood = view.findViewById<Spinner>(R.id.spinnerMood)
        super.onViewCreated(view, savedInstanceState)
        // Spinner setup
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.mood_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMood.adapter = adapter
        switchMoodQuiz = view.findViewById<SwitchMaterial>(R.id.switchMoodQuiz)

        analyzeBtn = view.findViewById<Button>(R.id.analyzeBtn)
        result = view.findViewById(R.id.resultView)


                // Default message
        result.text = "Your mood analysis will appear here"

        // 🔘 Switch logic → open QuizActivity instead of replacing fragment
        switchMoodQuiz.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchMoodQuiz.text = "Switch to Mood"
                // Start Quiz Activity
                val intent = Intent(requireContext(), QuizFragment::class.java)
                startActivity(intent)
                // Optional smooth transition
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                // Reset the switch after launching Quiz
                switchMoodQuiz.isChecked = false
            } else {
                switchMoodQuiz.text = "Switch to Quiz"
            }
        }

        // Analyze button logic
        analyzeBtn.setOnClickListener {
            val mood = spinnerMood.selectedItem?.toString()
            if (!mood.isNullOrEmpty()) {
                result.text = "Your current mood: $mood"
            } else {
                Toast.makeText(requireContext(), "Please select a mood", Toast.LENGTH_SHORT).show()
            }
            val moodMessage = when (mood) {
                "Happy" -> "😊 Great! Keep spreading positivity."
                "Sad" -> "😔 Take some rest and listen to calming music."
                "Angry" -> "😤 Try deep breathing to relax your mind."
                "Stressed" -> "😩 A short walk or meditation might help."
                "Neutral" -> "🙂 Stay calm and balanced throughout your day."
                else -> "😐 Your mood seems neutral. Stay mindful!"
            }

            result.text = moodMessage

            saveMoodToFirestore(mood.toString())

        }
    }

        private fun saveMoodToFirestore(selectedMood: String) {
            val userId = auth.currentUser?.uid ?: return
            val moodData = hashMapOf(
                "mood" to selectedMood,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(userId)
                .collection("moodLogs")
                .add(moodData)
                .addOnSuccessListener {
                    generateInstantWeeklyReport(userId)
                }
                .addOnFailureListener {
                    result.text = "Error saving mood: ${it.message}"
                }
        }
       private fun generateInstantWeeklyReport(userId: String) {

           db.collection("users")
            .document(userId)
            .collection("moodLogs")
            .get()
            .addOnSuccessListener { snap ->
                val moodCounts = mutableMapOf<String, Int>()
                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000

                for (doc in snap) {
                    val mood = doc.getString("mood") ?: continue
                    val timestamp = doc.getLong("timestamp") ?: continue
                    if (timestamp >= sevenDaysAgo) {
                        moodCounts[mood] = (moodCounts[mood] ?: 0) + 1
                    }
                }

                if (moodCounts.isNotEmpty()) {
                    val total = moodCounts.values.sum()
                    val topMood = moodCounts.maxByOrNull { it.value }?.key ?: "Neutral"
                    val topMoodPercent = (moodCounts[topMood]!! * 100 / total)

                    val reportText = buildString {
                        append("\n📊 Weekly Mood Report:\n")
                        append("• Dominant Mood: $topMood ($topMoodPercent%)\n")
                        append("• Total logs this week: $total\n\n")
                        append("🗓 Mood Breakdown:\n")

                        moodCounts.forEach { (mood, count) ->
                            val bar = "█".repeat((count * 5 / total).coerceAtLeast(1))
                            append("$mood — $bar ($count)\n")
                        }

                        append("\n💡 Insight: ${getMoodInsight(topMood)}")
                    }

                    result.text=reportText
                } else {
                    result.text = "No mood logs for this week yet."
                }
            }
    }
    private fun getMoodInsight(mood: String): String {
        return when (mood) {
            "Happy" -> "You’ve been in a positive state overall this week!"
            "Sad" -> "Try journaling or light activity to lift your spirits."
            "Stressed" -> "Take short breaks and focus on calm breathing."
            "Angry" -> "Redirect your energy with exercise or art."
            else -> "You seem balanced this week — great consistency!"
        }
    }

}
