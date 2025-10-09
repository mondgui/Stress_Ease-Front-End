package com.example.stressease

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.ArrayList

class SummaryActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Find the views from the layout
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        barChart = findViewById(R.id.barChart)

        // Set up the back button to close this activity
        btnBack.setOnClickListener {
            finish() // This will close the SummaryActivity and go back to MainActivity
        }

        // Setup the chart with some placeholder data
        setupBarChart()
        loadChartData()
    }

    private fun setupBarChart() {
        // General chart setup
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setFitBars(true)
        barChart.legend.isEnabled = false
        barChart.animateY(1000)

        // Setup X-axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        xAxis.valueFormatter = IndexAxisValueFormatter(days)

        // Setup Y-axis
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
    }

    private fun loadChartData() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 5f))
        entries.add(BarEntry(1f, 8f))
        entries.add(BarEntry(2f, 4f))
        entries.add(BarEntry(3f, 7f))
        entries.add(BarEntry(4f, 6f))
        entries.add(BarEntry(5f, 3f))
        entries.add(BarEntry(6f, 5f))

        val dataSet = BarDataSet(entries, "Weekly Stress Score")
        dataSet.color = ContextCompat.getColor(this, R.color.teal_700)
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.text_black)
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate() // Refresh the chart
    }
}
