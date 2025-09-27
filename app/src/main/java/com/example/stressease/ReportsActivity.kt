package com.example.stressease

import android.content.Intent
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
import java.io.FileOutputStream

class ReportsActivity : AppCompatActivity() {

    private lateinit var tvReportTitle: TextView
    private lateinit var tvReportStats: TextView
    private lateinit var tvReportStatus: TextView
    private lateinit var moodChart: BarChart
    private lateinit var moodPieChart: PieChart
    private lateinit var btnDownloadPdf: Button

    private var positive = 3
    private var negative = 2
    private var neutral = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reports)

        // Initialize views
        tvReportTitle = findViewById(R.id.tvReportTitle)
        tvReportStats = findViewById(R.id.tvReportStats)
        tvReportStatus = findViewById(R.id.tvReportStatus)
        moodChart = findViewById(R.id.moodChart)
        moodPieChart = findViewById(R.id.moodPieChart)
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf)

        // Update report text
        val total = positive + negative + neutral
        tvReportStats.text = "Total Entries: $total\nPositive: $positive\nNegative: $negative\nNeutral: $neutral"

        // Set status with color
        setStatusMessage()

        // Setup charts
        resizeCharts()
        setupBarChart(positive, negative, neutral)
        setupPieChart(positive, negative, neutral)

        // Download PDF
        btnDownloadPdf.setOnClickListener {
            generateReportPdf()
        }
    }

    private fun setStatusMessage() {
        var statusMsg: String
        var statusColor: Int

        when {
            positive >= negative && positive >= neutral -> {
                statusMsg = "Status: You are mostly Positive 🎉"
                statusColor = Color.parseColor("#4CAF50") // Green
            }
            negative >= positive && negative >= neutral -> {
                statusMsg = "Status: You are mostly Negative 😞"
                statusColor = Color.parseColor("#F44336") // Red
            }
            else -> {
                statusMsg = "Status: You are Neutral 😐"
                statusColor = Color.parseColor("#FFC107") // Yellow
            }
        }
        tvReportStatus.text = statusMsg
        tvReportStatus.setTextColor(statusColor)
    }

    private fun setupBarChart(positive: Int, negative: Int, neutral: Int) {
        val entries = listOf(
            BarEntry(0f, positive.toFloat()),
            BarEntry(1f, negative.toFloat()),
            BarEntry(2f, neutral.toFloat())
        )
        val dataSet = BarDataSet(entries, "Emotions").apply {
            colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"), Color.parseColor("#FFC107"))
            valueTextSize = 18f
            valueTextColor = getTextColor()
        }
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        moodChart.apply {
            data = barData
            description.isEnabled = false

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(listOf("Positive", "Negative", "Neutral"))
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 16f
                textColor = getTextColor()
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.textSize = 16f
            axisLeft.textColor = getTextColor()
            axisRight.isEnabled = false

            legend.textSize = 16f
            legend.textColor = getTextColor()

            setFitBars(true)
            invalidate()
        }
    }

    private fun setupPieChart(positive: Int, negative: Int, neutral: Int) {
        val entries = listOf(
            PieEntry(positive.toFloat(), "Positive"),
            PieEntry(negative.toFloat(), "Negative"),
            PieEntry(neutral.toFloat(), "Neutral")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"), Color.parseColor("#FFC107"))
            valueTextSize = 18f
            valueTextColor = getTextColor()
            sliceSpace = 4f
        }
        val pieData = PieData(dataSet)

        moodPieChart.apply {
            data = pieData
            description.isEnabled = false
            centerText = "Mood Share"
            setCenterTextSize(20f)
            setCenterTextColor(getTextColor())
            setEntryLabelTextSize(16f)
            setEntryLabelColor(getTextColor())
            legend.textSize = 16f
            legend.textColor = getTextColor()
            invalidate()
        }
    }

    private fun resizeCharts() {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val chartHeight = (screenHeight * 0.35).toInt()

        moodChart.layoutParams.height = chartHeight
        moodPieChart.layoutParams.height = chartHeight
    }

    private fun getTextColor(): Int {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) Color.WHITE else Color.BLACK
    }

    private fun generateReportPdf() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint()
        paint.textSize = 18f
        paint.color = getTextColor()

        var y = 50

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Condition Report", 200f, y.toFloat(), paint)

        y += 60
        paint.textSize = 18f
        paint.isFakeBoldText = false

        // Stats
        canvas.drawText("Total Entries: ${positive + negative + neutral}", 50f, y.toFloat(), paint)
        y += 30
        canvas.drawText("Positive: $positive", 50f, y.toFloat(), paint)
        y += 30
        canvas.drawText("Negative: $negative", 50f, y.toFloat(), paint)
        y += 30
        canvas.drawText("Neutral: $neutral", 50f, y.toFloat(), paint)
        y += 50

        // Status
        val statusMsg = tvReportStatus.text.toString()
        paint.color = tvReportStatus.currentTextColor
        canvas.drawText(statusMsg, 50f, y.toFloat(), paint)

        pdfDocument.finishPage(page)

        val file = File(getExternalFilesDir(null), "MoodReport.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // Open PDF Viewer Activity
            val intent = Intent(this, PdfViewer::class.java)
            intent.putExtra("pdfPath", file.absolutePath)
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
