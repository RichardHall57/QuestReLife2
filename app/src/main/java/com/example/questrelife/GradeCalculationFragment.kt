package com.example.questrelife

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        private const val ARG_CLASS_ID = "class_id"
        private const val ARG_CLASS_NAME = "class_name"
    }

    private var classId: String? = null
    private var className: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: GradeCalculationAdapter
    private lateinit var barChart: BarChart
    private lateinit var gpaText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString(ARG_CLASS_ID)
        className = arguments?.getString(ARG_CLASS_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grade_calculation, container, false)

        // Connect BarChart and GPA TextView
        barChart = view.findViewById(R.id.gpa_bar_chart)
        gpaText = view.findViewById(R.id.assignment_gpa_text)

        // RecyclerView setup
        val recyclerView = view.findViewById<RecyclerView>(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GradeCalculationAdapter() // <-- external adapter
        recyclerView.adapter = adapter

        // Fetch assignments from Firestore
        classId?.let { fetchAssignments(it) }

        return view
    }

    private fun fetchAssignments(classId: String) {
        db.collection("Classes")
            .document(classId)
            .collection("Assignments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GradeCalculation", "Listen failed", error)
                    return@addSnapshotListener
                }

                val assignments = snapshot?.documents?.map { doc ->
                    AssignmentItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getTimestamp("dueDate") ?: Timestamp.now(),
                        grade = doc.getDouble("grade")?.toFloat() ?: 0f
                    )
                } ?: emptyList()

                // Submit to adapter and update chart
                adapter.submitList(assignments)
                updateGPAChart(assignments)
            }
    }

    private fun updateGPAChart(assignments: List<AssignmentItem>) {
        if (assignments.isEmpty()) return

        val avgGrade = assignments.map { it.grade }.average().toFloat() // 0–100
        val gpa = avgGrade / 25f // Convert 0–100 to 0–4 GPA

        gpaText.text = "Average GPA: %.2f".format(gpa)

        val entries = arrayListOf(BarEntry(0f, gpa))
        val dataSet = BarDataSet(entries, "GPA").apply {
            color = when {
                gpa >= 3.5f -> Color.GREEN
                gpa >= 2.0f -> Color.YELLOW
                else -> Color.RED
            }
        }

        barChart.data = BarData(dataSet)
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }
}
