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
import com.example.questrelife.mdp.AcademicMDP
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class GradeCalculationFragment : Fragment() {

    private var classId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: GradeCalculationAdapter
    private lateinit var barChart: BarChart

    // Multiple Progress Bars & TextViews
    private lateinit var homeworkProgress: ProgressBar
    private lateinit var testProgress: ProgressBar
    private lateinit var projectProgress: ProgressBar
    private lateinit var homeworkText: TextView
    private lateinit var testText: TextView
    private lateinit var projectText: TextView

    // Academic MDP per type
    private val homeworkMDP = AcademicMDP()
    private val testMDP = AcademicMDP()
    private val projectMDP = AcademicMDP()

    companion object {
        private const val ARG_CLASS_ID = "class_id"
        private const val ARG_CLASS_NAME = "class_name"
        fun newInstance(classId: String, className: String): GradeCalculationFragment {
            val fragment = GradeCalculationFragment()
            val args = Bundle()
            args.putString(ARG_CLASS_ID, classId)
            args.putString(ARG_CLASS_NAME, className)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString(ARG_CLASS_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grade_calculation, container, false)

        barChart = view.findViewById(R.id.gpa_bar_chart)

        // Progress bars
        homeworkProgress = view.findViewById(R.id.homework_progress)
        testProgress = view.findViewById(R.id.test_progress)
        projectProgress = view.findViewById(R.id.project_progress)

        // TextViews
        homeworkText = view.findViewById(R.id.homework_text)
        testText = view.findViewById(R.id.test_text)
        projectText = view.findViewById(R.id.project_text)

        // RecyclerView for assignments
        val recyclerView = view.findViewById<RecyclerView>(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GradeCalculationAdapter()
        recyclerView.adapter = adapter

        setupBarChartAppearance()
        classId?.let { fetchAssignments(it) }

        return view
    }

    private fun fetchAssignments(classId: String) {
        db.collection("Classes").document(classId)
            .collection("Assignments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GradeCalculation", "Listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val assignments = snapshot.documents.map { doc ->
                    val gradeValue = doc.get("grade")
                    val gradeFloat = when (gradeValue) {
                        is Number -> gradeValue.toFloat()
                        is String -> gradeValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }
                    AssignmentItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getTimestamp("dueDate") ?: Timestamp.now(),
                        grade = gradeFloat,
                        type = doc.getString("type") ?: "Other"
                    )
                }

                adapter.submitList(assignments)
                updateLevels(assignments)
                updateBarChart(assignments)
            }
    }

    /** Update multiple level tracks */
    private fun updateLevels(assignments: List<AssignmentItem>) {
        // Reset MDPs
        homeworkMDP.reset()
        testMDP.reset()
        projectMDP.reset()

        val homeworkGrades = mutableListOf<Float>()
        val testGrades = mutableListOf<Float>()
        val projectGrades = mutableListOf<Float>()

        assignments.forEach { a ->
            when (a.type) {
                "Homework" -> {
                    homeworkMDP.stepWithGrade(a.grade)
                    homeworkGrades.add(a.grade)
                }
                "Test" -> {
                    testMDP.stepWithGrade(a.grade)
                    testGrades.add(a.grade)
                }
                "Project" -> {
                    projectMDP.stepWithGrade(a.grade)
                    projectGrades.add(a.grade)
                }
            }
        }

        // Update Progress Bars & Text
        homeworkProgress.progress = (homeworkMDP.gpa / 4.0 * 100).toInt()
        testProgress.progress = (testMDP.gpa / 4.0 * 100).toInt()
        projectProgress.progress = (projectMDP.gpa / 4.0 * 100).toInt()

        homeworkText.text = "Homework: ${homeworkMDP.state.label} | GPA: ${"%.2f".format(homeworkMDP.gpa)}"
        testText.text = "Tests: ${testMDP.state.label} | GPA: ${"%.2f".format(testMDP.gpa)}"
        projectText.text = "Projects: ${projectMDP.state.label} | GPA: ${"%.2f".format(projectMDP.gpa)}"
    }

    /** Chart appearance */
    private fun setupBarChartAppearance() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 100f
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            animateY(800)
            legend.isEnabled = true
        }
    }

    /** Update bar chart */
    private fun updateBarChart(assignments: List<AssignmentItem>) {
        if (assignments.isEmpty()) return

        val entries = assignments.mapIndexed { i, a -> BarEntry(i.toFloat(), a.grade) }
        val colors = assignments.map { getColorForGrade(it.grade) }
        val titles = assignments.map { it.title }

        val dataSet = BarDataSet(entries, "Grades").apply {
            setColors(colors)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getBarLabel(barEntry: BarEntry?): String {
                    val g = barEntry?.y ?: 0f
                    return convertToLetter(g)
                }
            }
        }

        barChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(titles)
        barChart.setFitBars(true)
        barChart.invalidate()
    }

    private fun convertToLetter(grade: Float): String = when {
        grade >= 90 -> "A"
        grade >= 80 -> "B"
        grade >= 70 -> "C"
        grade >= 60 -> "D"
        else -> "F"
    }

    private fun getColorForGrade(grade: Float): Int = when {
        grade >= 80 -> Color.parseColor("#2E7D32")
        grade >= 70 -> Color.parseColor("#F9A825")
        else -> Color.parseColor("#C62828")
    }
}


