package com.example.questrelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var calendar_recycler_view: RecyclerView
    private lateinit var header_text: TextView
    private lateinit var prev_month: Button
    private lateinit var next_month: Button
    private lateinit var month_text: TextView
    private lateinit var empty_state: View

    private var currentCalendar = Calendar.getInstance()
    private var assignments: List<AssignmentItem> = listOf()

    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        calendar_recycler_view = view.findViewById(R.id.calendar_recycler_view)
        header_text = view.findViewById(R.id.header_text)
        prev_month = view.findViewById(R.id.prev_month)
        next_month = view.findViewById(R.id.next_month)
        month_text = view.findViewById(R.id.month_text)
        empty_state = view.findViewById(R.id.empty_state)

        calendar_recycler_view.layoutManager = GridLayoutManager(requireContext(), 7)

        // Pass assignments list to adapter
        calendarAdapter = CalendarAdapter(assignments) { dayAssignments ->
            // Open AssignmentsFragment and pass the assignments for the selected day
            if (dayAssignments.isNotEmpty()) {
                val fragment = AssignmentsFragment()
                val bundle = Bundle()
                // FIX: Use Serializable instead of Parcelable
                bundle.putSerializable("assignments_for_day", ArrayList(dayAssignments))
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        calendar_recycler_view.adapter = calendarAdapter

        prev_month.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        next_month.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        fetchAssignments()

        return view
    }

    private fun fetchAssignments() {
        db.collection("Classes")
            .get()
            .addOnSuccessListener { classDocs ->
                val allAssignments = mutableListOf<AssignmentItem>()
                val totalClasses = classDocs.size()

                if (totalClasses == 0) {
                    assignments = listOf()
                    updateCalendar()
                    return@addOnSuccessListener
                }

                var processedClasses = 0

                for (classDoc in classDocs) {
                    db.collection("Classes").document(classDoc.id)
                        .collection("Assignments")
                        .get()
                        .addOnSuccessListener { assignmentDocs ->
                            for (doc in assignmentDocs) {
                                val dueTimestamp = doc.getTimestamp("dueDate") ?: Timestamp.now()
                                val assignment = AssignmentItem(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "",
                                    description = doc.getString("description") ?: "",
                                    dueDate = dueTimestamp,
                                    grade = (doc.getDouble("grade") ?: 0.0).toFloat(),
                                    type = doc.getString("type") ?: "Other"
                                )
                                allAssignments.add(assignment)
                            }
                            processedClasses++
                            if (processedClasses == totalClasses) {
                                assignments = allAssignments
                                updateCalendar()
                            }
                        }
                        .addOnFailureListener {
                            processedClasses++
                            if (processedClasses == totalClasses) {
                                assignments = allAssignments
                                updateCalendar()
                            }
                        }
                }
            }
            .addOnFailureListener {
                assignments = listOf()
                updateCalendar()
            }
    }

    private fun updateCalendar() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        month_text.text = sdf.format(currentCalendar.time)

        val month = currentCalendar.get(Calendar.MONTH)
        val year = currentCalendar.get(Calendar.YEAR)

        val calendarDays = mutableListOf<CalendarAdapter.CalendarItem>()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Empty days before month starts
        for (i in 0 until firstDayOfWeek) {
            calendarDays.add(CalendarAdapter.CalendarItem(0, listOf()))
        }

        // Fill days with assignments
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayAssignments = assignments.filter {
                val tsDate = it.dueDate.toDate()
                tsDate.year == cal.get(Calendar.YEAR) - 1900 &&
                        tsDate.month == cal.get(Calendar.MONTH) &&
                        tsDate.date == cal.get(Calendar.DAY_OF_MONTH)
            }
            calendarDays.add(CalendarAdapter.CalendarItem(day, dayAssignments))
        }

        calendarAdapter.updateDays(calendarDays)

        empty_state.visibility = if (assignments.isEmpty()) View.VISIBLE else View.GONE
    }
}

