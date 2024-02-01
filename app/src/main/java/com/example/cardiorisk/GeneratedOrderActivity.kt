package com.example.cardiorisk

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class GeneratedOrderActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generated_order_acrivity)

        val bg_input = findViewById<ImageView>(R.id.bg_input)
        bg_input.alpha = 0.7f

        val orderGender = intent.getStringExtra("orderGender")
        val orderAge = intent.getStringExtra("orderAge")
        val orderID = intent.getStringExtra("orderID")
        val orderHDL = intent.getStringExtra("orderHDL")
        val orderTotal = intent.getStringExtra("orderTotal")
        val orderSystolicBP = intent.getStringExtra("orderSystolicBP")
        val orderDiabetes = intent.getStringExtra("orderDiabetes")
        val orderSmoker = intent.getStringExtra("orderSmoker")

        val diabetesAfterGeneration = findViewById<TextView>(R.id.diabetesGenerated)
        val ageAfterGeneration = findViewById<TextView>(R.id.ageGenerated)
        val genderAfterGeneration = findViewById<TextView>(R.id.genderGenerated)
        val hdlAfterGeneration = findViewById<TextView>(R.id.hdlCholesterolGenerated)
        val totalAfterGeneration = findViewById<TextView>(R.id.totalCholesterolGenerated)
        val systolicAfterGeneration = findViewById<TextView>(R.id.BPLevelGenerated)
        val smokerAfterGeneration = findViewById<TextView>(R.id.smokerGenerated)
        val idAfterGeneration = findViewById<TextView>(R.id.orderIDGenerated)

        diabetesAfterGeneration.text = orderDiabetes
        ageAfterGeneration.text = "Age: $orderAge years"
        idAfterGeneration.text = orderID
        genderAfterGeneration.text = "Gender: $orderGender"
        hdlAfterGeneration.text = "HDL Cholesterol: $orderHDL mmol/L"
        totalAfterGeneration.text = "Total Cholesterol: $orderTotal mmol/L"
        systolicAfterGeneration.text = "Systolic Blood Pressure: $orderSystolicBP mmHg"
        smokerAfterGeneration.text = orderSmoker

        val homeButton = findViewById<ImageView>(R.id.homeButton)
        homeButton.setOnClickListener {
            finish()
        }
        val age = orderAge?.toIntOrNull()
        val totalCholesterolMmol = orderTotal?.toDoubleOrNull()
        val hdlCholesterolMmol = orderHDL?.toDoubleOrNull()
        val systolicBP = orderSystolicBP?.toDoubleOrNull()
        val isSmoker = if (orderSmoker == "smoker") 1 else 0
        val isDiabetic = if (orderDiabetes == "has diabetes") 1 else 0
        val isMale: Boolean = if (orderGender == "male") true else false

        var frs: Double? = null
        if (age != null && totalCholesterolMmol != null && hdlCholesterolMmol != null && systolicBP != null) {
           // val frs = calculateCVD(age, totalCholesterolMmol, hdlCholesterolMmol, systolicBP, isSmoker, isDiabetic, isMale)
            frs = calculateCVD(age, totalCholesterolMmol, hdlCholesterolMmol, systolicBP, isSmoker, isDiabetic, isMale)
            val analysis = analyzeFRS(frs)
            val frsTextView = findViewById<TextView>(R.id.frsGenerated)
            frsTextView.text = "$analysis \n\nYour FRS is $frs %"
        } else {
            println("Error: Invalid input")
        }

        val graphButton = findViewById<ImageView>(R.id.generatedGraphButton)
        graphButton.setOnClickListener {
            val graphDialog = Dialog(this)
            graphDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            graphDialog.setCancelable(true)
            graphDialog.setContentView(R.layout.graph_generated)

            val barChart = graphDialog.findViewById<BarChart>(R.id.barChart)
            barChart.setExtraOffsets(0f, 20f, 0f, 10f)
            barChart.description.isEnabled = false
            frs?.let {
                val data = generateBarData(it)
                barChart.data = data

                barChart.xAxis.textSize = 14f
                barChart.axisLeft.textSize = 14f
                barChart.axisRight.textSize = 14f

                barChart.axisLeft.axisMinimum = 0f
                barChart.axisLeft.axisMaximum = 32f
                barChart.xAxis.axisMinimum = -1f
                barChart.xAxis.axisMaximum = 1f

                data.barWidth = 0.4f

                val typeface = ResourcesCompat.getFont(this, R.font.inter_semibold)
                barChart.xAxis.typeface = typeface

                barChart.axisLeft.typeface = typeface
                barChart.axisRight.typeface = typeface

                data.setValueTextSize(14f)
                data.setValueTypeface(typeface)

                val legend = barChart.legend
                legend.isEnabled = true
                legend.form = Legend.LegendForm.CIRCLE
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.setDrawInside(false)

                legend.textSize = 14f
                legend.typeface = typeface

                val legendEntries = arrayOf(
                    LegendEntry("Low Risk", Legend.LegendForm.CIRCLE, 10f, 2f, null, ContextCompat.getColor(applicationContext, R.color.lowRisk)),
                    LegendEntry("Moderate Risk", Legend.LegendForm.CIRCLE, 10f, 2f, null, ContextCompat.getColor(applicationContext, R.color.moderateRisk)),
                    LegendEntry("High Risk", Legend.LegendForm.CIRCLE, 10f, 2f, null, ContextCompat.getColor(applicationContext, R.color.highRisk))
                )
                legend.setCustom(legendEntries)

                barChart.invalidate()
            }

            graphDialog.show()
            graphDialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.99).toInt(),
                (resources.displayMetrics.heightPixels * 0.70).toInt()
            )
        }

    }

    fun calculateCVD(age: Int, totalCholesterolMmol: Double, hdlCholesterolMmol: Double, systolicBP: Double, isSmoker: Int, isDiabetic: Int, isMale: Boolean): Double {
        val agePoints = when {
            age <= 34 -> 0
            age in 35..39 -> if (isMale) 2 else 2
            age in 40..44 -> if (isMale) 5 else 4
            age in 45..49 -> if (isMale) 7 else 5
            age in 50..54 -> if (isMale) 8 else 7
            age in 55..59 -> if (isMale) 10 else 8
            age in 60..64 -> if (isMale) 11 else 9
            age in 65..69 -> if (isMale) 12 else 10
            age in 70..74 -> if (isMale) 14 else 11
            else -> if (isMale) 15 else 12
        }
        val hdlPoints = when {
            hdlCholesterolMmol > 1.6 -> if (isMale) -2 else -2
            hdlCholesterolMmol in 1.3..1.6 -> if (isMale) -1 else -1
            hdlCholesterolMmol in 1.2..1.29 -> if (isMale) 0 else 0
            hdlCholesterolMmol in 0.9..1.19 -> if (isMale) 1 else 1
            hdlCholesterolMmol < 0.9 -> if (isMale) 2 else 2
            else -> 0
        }
        val totalChlPoints = when {
            totalCholesterolMmol < 4.1 -> 0
            totalCholesterolMmol in 4.1..5.19 -> if (isMale) 1 else 1
            totalCholesterolMmol in 5.2..6.19 -> if (isMale) 2 else 3
            totalCholesterolMmol in 6.2..7.2 -> if (isMale) 3 else 4
            totalCholesterolMmol > 7.2 -> if (isMale) 4 else 5
            else -> 0
        }
        val systolicBPPoints = when {
            systolicBP < 120 -> if (isMale) -2 else -3
            systolicBP in 120.0..129.0 -> if (isMale) 0 else 0
            systolicBP in 130.0..139.0 -> if (isMale) 1 else 1
            systolicBP in 140.0..149.0 -> if (isMale) 2 else 2
            systolicBP in 150.0..159.0 -> if (isMale) 2 else 4
            systolicBP >= 160.0 -> if (isMale) 3 else 5
            else -> 0
        }
        val smokerPoints = when  {
            (isSmoker == 1) -> if (isMale) 4 else 3
            else -> 0
        }
        val diabetesPoints = when {
            (isDiabetic == 1) -> if (isMale) 3 else 4
            else -> 0
        }
        val totalCDVriskPoints = agePoints + hdlPoints + totalChlPoints + systolicBPPoints + smokerPoints + diabetesPoints
        val totalCDVrisk = when {
            totalCDVriskPoints < -3 -> if (isMale) 0.0 else 0.0
            totalCDVriskPoints == -2 -> if (isMale) 1.1 else 0.0
            totalCDVriskPoints == -1 -> if (isMale) 1.4 else 1.0
            totalCDVriskPoints == 0 -> if (isMale) 1.6 else 1.2
            totalCDVriskPoints == 1 -> if (isMale) 1.9 else 1.5
            totalCDVriskPoints == 2 -> if (isMale) 2.3 else 1.7
            totalCDVriskPoints == 3 -> if (isMale) 2.8 else 2.0
            totalCDVriskPoints == 4 -> if (isMale) 3.3 else 2.4
            totalCDVriskPoints == 5 -> if (isMale) 3.9 else 2.8
            totalCDVriskPoints == 6 -> if (isMale) 4.7 else 3.3
            totalCDVriskPoints == 7 -> if (isMale) 5.6 else 3.9
            totalCDVriskPoints == 8 -> if (isMale) 6.7 else 4.5
            totalCDVriskPoints == 9 -> if (isMale) 7.9 else 5.3
            totalCDVriskPoints == 10 -> if (isMale) 9.4 else 6.3
            totalCDVriskPoints == 11 -> if (isMale) 11.2 else 7.3
            totalCDVriskPoints == 12 -> if (isMale) 13.3 else 8.6
            totalCDVriskPoints == 13 -> if (isMale) 15.6 else 10.0
            totalCDVriskPoints == 14 -> if (isMale) 18.4 else 11.7
            totalCDVriskPoints == 15 -> if (isMale) 21.6 else 13.7
            totalCDVriskPoints == 16 -> if (isMale) 25.3 else 15.9
            totalCDVriskPoints == 17 -> if (isMale) 29.4 else 18.5
            totalCDVriskPoints == 18 -> if (isMale) 30.0 else 21.5
            totalCDVriskPoints == 19 -> if (isMale) 30.0 else 24.8
            totalCDVriskPoints == 20 -> if (isMale) 30.0 else 27.5
            totalCDVriskPoints > 21 -> if (isMale) 30.0 else 30.0
            else -> 0.0
        }
        return totalCDVrisk

    }
    private fun generateBarData(frs: Double): BarData {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, frs.toFloat()))

        val set = BarDataSet(entries, "FRS")

        if (frs < 10.0) {
            set.setColor(ContextCompat.getColor(applicationContext, R.color.lowRisk))
        } else if (frs in 10.0..19.0) {
            set.setColor(ContextCompat.getColor(applicationContext, R.color.moderateRisk))
        } else {
            set.setColor(ContextCompat.getColor(applicationContext, R.color.highRisk))
        }




        return BarData(set)
    }

    fun analyzeFRS(totalCDVrisk: Double): String {
        return when {
            totalCDVrisk < 10.0 -> "Your risk of developing cardiovascular disease in the next 10 years is low. However, it's important to maintain a healthy lifestyle and regular check-ups."
            totalCDVrisk in 10.0..19.0 -> "Your risk of developing cardiovascular disease in the next 10 years is moderate. It's recommended to consult with your doctor about ways to lower your risk."
            totalCDVrisk > 19.0 -> "Your risk of developing cardiovascular disease in the next 10 years is high. Please consult with your doctor immediately to discuss your risk factors and treatment options."
            else -> "Error: Invalid input"
        }
    }
}