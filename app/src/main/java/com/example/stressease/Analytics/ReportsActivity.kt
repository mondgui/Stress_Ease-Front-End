package com.example.stressease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.LocalStorageOffline.SharedPreference
import com.example.stressease.LoginMain.MainActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ReportsActivity : AppCompatActivity() {

    private lateinit var tvReportTitle: TextView
    private lateinit var tvReportStats: TextView
    private lateinit var tvReportStatus: TextView
    private lateinit var emotionBarChart: BarChart
    private lateinit var moodPieChart: PieChart
    private lateinit var moodTrendChart: LineChart
    private lateinit var btnBack: Button
    private lateinit var btnViewSummary: Button
    private lateinit var btnNext: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reports)

        auth=FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()


        // Bind views
        tvReportTitle = findViewById(R.id.tvReportTitle)
        tvReportStats = findViewById(R.id.tvReportStats)
        tvReportStatus = findViewById(R.id.tvReportStatus)
        emotionBarChart = findViewById(R.id.emotionBarChart)
        moodPieChart = findViewById(R.id.moodPieChart)
        moodTrendChart = findViewById(R.id.moodTrendChart)
        btnBack = findViewById(R.id.btnBack)
        btnViewSummary = findViewById(R.id.btnViewSummary)
        btnNext = findViewById(R.id.btnNext)

        val moodList = SharedPreference.loadStringList(this, "moodLogs") // existing helper
        val total = moodList.size
        val positive = moodList.count { it == "Happy" || it == "Excited" }
        val negative = moodList.count { it == "Sad" || it == "Angry" }
        val neutral = total - positive - negative

        saveAggregatedReport(total, positive, negative, neutral, moodList)

        // Load and process data
        loadReports()

        // Navigation
        btnBack.setOnClickListener { finish() }
        btnViewSummary.setOnClickListener {
            startActivity(Intent(this, Summary::class.java))
        }
        btnNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun loadReports() {


        val moodHistory = SharedPreference.loadStringList(this, "mood_history")
        val chatHistory = SharedPreference.loadChatList(this, "chat_history")

        val chatEmotionCounts = chatHistory.groupingBy { chatMsg ->
            val msg = chatMsg.message ?: ""   // default empty string if null
            when {
                msg.contains("good", true) || msg.contains("happy", true) || msg.contains("great", true) -> "Positive"
                msg.contains("bad", true) || msg.contains("sad", true) || msg.contains("angry", true) -> "Negative"
                else -> "Neutral"
            }
        }.eachCount()

        val moodCounts = moodHistory.groupingBy { mood ->
            when (mood) {
                "Happy", "Excited", "Calm" -> "Positive"
                "Sad", "Angry", "Stressed" -> "Negative"
                else -> "Neutral"
            }
        }.eachCount()

        val total = (chatEmotionCounts.values.sum() + moodCounts.values.sum())
        val positive = (chatEmotionCounts["Positive"] ?: 0) + (moodCounts["Positive"] ?: 0)
        val negative = (chatEmotionCounts["Negative"] ?: 0) + (moodCounts["Negative"] ?: 0)
        val neutral = (chatEmotionCounts["Neutral"] ?: 0) + (moodCounts["Neutral"] ?: 0)

        tvReportStats.text =
            "Total Entries: $total\nPositive: $positive\nNegative: $negative\nNeutral: $neutral"

        tvReportStatus.text = when {
            positive > negative && positive > neutral -> "Status: You are mostly Positive 🎉"
            negative > positive && negative > neutral -> "Status: You are mostly Negative 😟"
            else -> "Status: Mixed Mood ⚖️"
        }

        val mergedCounts = mapOf("Positive" to positive, "Negative" to negative, "Neutral" to neutral)
        if (total > 0) {
            showEmotionBarChart(mergedCounts)
            showMoodPieChart(mergedCounts)
            showMoodTrendChart(moodHistory)
        }
    }

    private fun showEmotionBarChart(emotionCounts: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        var index = 0
        for ((_, count) in emotionCounts) {
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
            index++
        }
        val dataSet = BarDataSet(entries, "Emotions")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 18f

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        emotionBarChart.data = data
        emotionBarChart.setFitBars(true)
        emotionBarChart.setDrawGridBackground(false)
        emotionBarChart.description = Description().apply { text = "" }
        emotionBarChart.legend.apply {
            textSize = 16f
            formSize = 16f
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        }
        emotionBarChart.animateY(1200)
        emotionBarChart.invalidate()
    }

    private fun showMoodPieChart(emotionCounts: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((emotion, count) in emotionCounts) {
            entries.add(PieEntry(count.toFloat(), emotion))
        }
        val dataSet = PieDataSet(entries, "Moods")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 20f
        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)
        data.setValueTextSize(18f)

        moodPieChart.data = data
        moodPieChart.setUsePercentValues(true)
        moodPieChart.setDrawEntryLabels(true)
        moodPieChart.setEntryLabelTextSize(16f)
        moodPieChart.description = Description().apply { text = "" }
        moodPieChart.legend.apply {
            textSize = 20f
            formSize = 20f
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        }
        moodPieChart.animateY(1200)
        moodPieChart.invalidate()
    }

    private fun showMoodTrendChart(moodHistory: List<String>) {
        val entries = ArrayList<Entry>()
        for ((index, mood) in moodHistory.withIndex()) {
            val value = when (mood) {
                "Happy" -> 3f
                "Neutral" -> 2f
                "Sad" -> 1f
                "Angry" -> 0f
                else -> 2f
            }
            entries.add(Entry(index.toFloat(), value))
        }

        val dataSet = LineDataSet(entries, "Mood Trend")
        dataSet.colors = listOf(ColorTemplate.getHoloBlue())
        dataSet.circleColors = listOf(ColorTemplate.getHoloBlue())
        dataSet.valueTextSize = 20f
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f

        val data = LineData(dataSet)

        moodTrendChart.data = data
        moodTrendChart.description = Description().apply { text = "" }
        moodTrendChart.legend.apply {
            textSize = 20f
            formSize = 16f
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        }
        moodTrendChart.axisLeft.textSize = 14f
        moodTrendChart.axisRight.textSize = 14f
        moodTrendChart.xAxis.textSize = 14f

        moodTrendChart.animateX(1200)
        moodTrendChart.invalidate()
    }
    private fun saveAggregatedReport(
        total: Int,
        positive: Int,
        negative: Int,
        neutral: Int,
        moodHistory: List<String>
    ) {
        val userId = auth.currentUser?.uid ?: return
        val reportData = hashMapOf(
            "totalChats" to total,
            "positiveCount" to positive,
            "negativeCount" to negative,
            "neutralCount" to neutral,
            "last7DaysMood" to moodHistory.takeLast(7),
            "lastUpdated" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("reports")
            .document("summary")
            .set(reportData, SetOptions.merge())
            .addOnSuccessListener {
                tvReportStats.append("\n\n[Synced to Cloud ✅]")
            }
            .addOnFailureListener {
                tvReportStats.append("\n\n[Cloud Sync Failed ❌]")
            }
    }

}