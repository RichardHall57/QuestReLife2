package com.example.questrelife

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.questrelife.addClass
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateSemesterFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var createButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Use only Firebase Timestamp
    private var startDateTimestamp: Timestamp? = null
    private var endDateTimestamp: Timestamp? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_create_semester, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.semester_name_edit_text)
        startDateEditText = view.findViewById(R.id.start_date_edit_text)
        endDateEditText = view.findViewById(R.id.end_date_edit_text)
        createButton = view.findViewById(R.id.create_semester_button)

        // Start Date Picker
        startDateEditText.setOnClickListener {
            showDatePicker { dateMillis ->
                startDateTimestamp = Timestamp(Date(dateMillis))
                startDateEditText.setText(formatDate(dateMillis))
            }
        }

        // End Date Picker
        endDateEditText.setOnClickListener {
            showDatePicker { dateMillis ->
                endDateTimestamp = Timestamp(Date(dateMillis))
                endDateEditText.setText(formatDate(dateMillis))
            }
        }

        createButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            if (name.isEmpty() || startDateTimestamp == null || endDateTimestamp == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (endDateTimestamp!!.toDate().before(startDateTimestamp!!.toDate())) {
                Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val semester = Semester(
                name = name,
                startDate = startDateTimestamp!!,
                endDate = endDateTimestamp!!
            )

            addSemesterToFirestore(semester)
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onDateSelected(cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDate(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        return "$month/$day/$year"
    }

    private fun addSemesterToFirestore(semester: Semester) {
        addSemester(
            db,
            semester,
            onSuccess = { id ->
                Toast.makeText(requireContext(), "Semester created with ID $id", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            },
            onFailure = { e ->
                Toast.makeText(requireContext(), "Failed to create semester: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}


