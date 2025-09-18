package com.example.questrelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class SemesterFragment : Fragment() {

    private var semester: Semester? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            semester = it.getParcelable("semester")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_semester, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        semester?.let { sem ->
            val semesterNameTextView = view.findViewById<TextView>(R.id.textSemesterName)
            val semesterDatesTextView = view.findViewById<TextView>(R.id.textSemesterDates)

            semesterNameTextView.text = sem.name
            semesterDatesTextView.text =
                "${formatDate(sem.startDate)} - ${formatDate(sem.endDate)}"
        }
    }

    private fun formatDate(millis: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        val month = cal.get(java.util.Calendar.MONTH) + 1
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val year = cal.get(java.util.Calendar.YEAR)
        return "$month/$day/$year"
    }

    companion object {
        fun newInstance(semester: Semester) =
            SemesterFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("semester", semester)
                }
            }
    }
}
