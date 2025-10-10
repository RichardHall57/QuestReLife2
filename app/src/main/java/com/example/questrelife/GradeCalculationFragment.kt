package com.example.questrelife

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class GradeCalculationFragment : Fragment() {

    private var classId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: GradeCalculationAdapter
    private lateinit var barChart: BarChart
    private lateinit var gpaText: TextView
    private lateinit var levelProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString("class_id")
        Log.d("GradeCalculation", "Fragment created with classId=$classId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grade_calculation, container, false)

        gpaText = view.findViewById(R.id.assignment_gpa_text)
        barChart = view.findViewById(R.id.gpa_bar_chart)
        levelProgressBar = view.findViewById(R.id.level_progress)

        val recyclerView = view.findViewById<RecyclerView>(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = GradeCalculationAdapter(
            onProgressUpdate = { progress ->
                Log.d("GradeCalculation", "Progress update: $progress")
                levelProgressBar.progress = progress
            },
            onGradesUpdate = { grades ->
                Log.d("GradeCalculation", "Adapter callback received grades: $grades")
                updateGPAChart(grades)
            }
        )
        recyclerView.adapter = adapter

        classId?.let { fetchAssignments(it) } ?: Log.e("GradeCalculation", "classId is null!")
        return view
    }

    private fun fetchAssignments(classId: String) {
        Log.d("GradeCalculation", "Fetching assignments for classId=$classId")
        db.collection("Classes")
            .document(classId)
            .collection("Assignments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GradeCalculation", "Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e("GradeCalculation", "Snapshot is null!")
                    return@addSnapshotListener
                }

                Log.d("GradeCalculation", "Snapshot size: ${snapshot.size()}")

                val assignments = snapshot.documents.map { doc ->
                    val gradeValue = doc.get("grade")
                    val grade = when (gradeValue) {
                        is Number -> gradeValue.toFloat()
                        is String -> gradeValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }

                    Log.d(
                        "GradeCalculation",
                        "Doc ${doc.id}: raw grade=$gradeValue -> parsed grade=$grade"
                    )

                    AssignmentItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getTimestamp("dueDate") ?: Timestamp.now(),
                        grade = grade
                    )
                }

                Log.d("GradeCalculation", "Parsed assignments: ${assignments.map { it.grade }}")

                // Submit list to adapter (this triggers the chart callback)
                adapter.submitList(assignments)

                // Also force a chart update here as a backup
                val grades = assignments.map { it.grade }
                if (grades.isEmpty()) {
                    Log.w("GradeCalculation", "No grades found!")
                }
                updateGPAChart(grades)
            }
    }

    private fun updateGPAChart(grades: List<Float>) {
        Log.d("GradeCalculation", "Updating chart with grades: $grades")

        val validGrades = grades.filter { it >= 0f }
        if (validGrades.isEmpty()) {
            Log.w("GradeCalculation", "No valid grades to display!")
            gpaText.text = "Average GPA: 0.00"
            barChart.clear()
            barChart.invalidate()
            return
        }

        val avgGrade = validGrades.average().toFloat()
        val gpa = avgGrade / 25f
        gpaText.text = "Average GPA: %.2f".format(gpa)

        val entries = validGrades.mapIndexed { index, grade ->
            BarEntry(index.toFloat(), grade / 25f)
        }

        val dataSet = BarDataSet(entries, "GPA per Assignment").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description.isEnabled = false

        // Set axis scaling to make bars visible
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 4f
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.axisMaximum = 4f

        barChart.animateY(800)
        barChart.invalidate()
        Log.d("GradeCalculation", "Chart updated successfully")
    }
}
