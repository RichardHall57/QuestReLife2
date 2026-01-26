package com.example.questrelife

import android.graphics.Color
import android.os.Bundle
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

    companion object {
        fun newInstance(classId: String, className: String): GradeCalculationFragment {
            val fragment = GradeCalculationFragment()
            val args = Bundle()
            args.putString("class_id", classId)
            args.putString("class_name", className)
            fragment.arguments = args
            return fragment
        }
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var assignmentsRecyclerView: RecyclerView
    private lateinit var assignmentsAdapter: GradeCalculationAdapter

    private lateinit var gpaBarChart: BarChart
    private lateinit var assignmentGpaText: TextView
    private lateinit var homeworkProgress: ProgressBar
    private lateinit var testProgress: ProgressBar
    private lateinit var projectProgress: ProgressBar

    private var assignments: MutableList<AssignmentItem> = mutableListOf()
    private var classId: String? = null
    private var className: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString("class_id")
        className = arguments?.getString("class_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_grade_calculation, container, false)

        // Bind views
        assignmentsRecyclerView = view.findViewById(R.id.posts_recycler_view)
        gpaBarChart = view.findViewById(R.id.gpa_bar_chart)
        assignmentGpaText = view.findViewById(R.id.assignment_gpa_text)
        homeworkProgress = view.findViewById(R.id.homework_progress)
        testProgress = view.findViewById(R.id.test_progress)
        projectProgress = view.findViewById(R.id.project_progress)

        assignmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        assignmentsAdapter = GradeCalculationAdapter(assignments)
        assignmentsRecyclerView.adapter = assignmentsAdapter

        // TEMP dummy data (safe, predictable, visible)
        assignments = mutableListOf(
            AssignmentItem("1", "Math HW", "Chapter 3", Timestamp.now(), 95f, "Homework"),
            AssignmentItem("2", "Science Test", "Lab quiz", Timestamp.now(), 88f, "Test"),
            AssignmentItem("3", "History Project", "Presentation", Timestamp.now(), 92f, "Project"),
            AssignmentItem("4", "Extra Homework", "Extra exercises", Timestamp.now(), 0f, "Homework")
        )

        assignmentsAdapter.submitList(assignments)

        updateProgressBars()
        setupOverallGradeChart()

        return view
    }

    private fun updateProgressBars() {
        val homework = assignments.filter { it.type == "Homework" }
        val test = assignments.filter { it.type == "Test" }
        val project = assignments.filter { it.type == "Project" }

        homeworkProgress.progress =
            if (homework.isNotEmpty())
                homework.count { it.grade > 0f } * 100 / homework.size
            else 0

        testProgress.progress =
            if (test.isNotEmpty())
                test.count { it.grade > 0f } * 100 / test.size
            else 0

        projectProgress.progress =
            if (project.isNotEmpty())
                project.count { it.grade > 0f } * 100 / project.size
            else 0
    }

    private fun setupOverallGradeChart() {
        val completedAssignments = assignments.filter { it.grade > 0f }
        val overallGrade =
            if (completedAssignments.isNotEmpty())
                completedAssignments.map { it.grade }.average().toFloat()
            else 0f

        assignmentGpaText.text = "Class Grade: ${"%.1f".format(overallGrade)}%"

        val entries = listOf(
            BarEntry(0f, overallGrade)
        )

        val dataSet = BarDataSet(entries, "Overall Grade").apply {
            color = requireContext().getColor(R.color.teal_700)
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        gpaBarChart.data = barData
        gpaBarChart.description.isEnabled = false
        gpaBarChart.legend.isEnabled = false

        gpaBarChart.axisLeft.axisMinimum = 0f
        gpaBarChart.axisLeft.axisMaximum = 100f
        gpaBarChart.axisRight.isEnabled = false
        gpaBarChart.xAxis.isEnabled = false

        gpaBarChart.setFitBars(true)
        gpaBarChart.invalidate()
    }
}
