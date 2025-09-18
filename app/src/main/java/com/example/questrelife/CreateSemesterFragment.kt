package com.example.questrelife

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateSemesterFragment : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var createButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var startDateMillis: Long = 0L
    private var endDateMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_semester_form)

        nameEditText = findViewById(R.id.semester_name_edit_text)
        startDateEditText = findViewById(R.id.start_date_edit_text)
        endDateEditText = findViewById(R.id.end_date_edit_text)
        createButton = findViewById(R.id.create_semester_button)

        // Show DatePicker for start date
        startDateEditText.setOnClickListener {
            showDatePicker { date ->
                startDateMillis = date
                startDateEditText.setText(formatDate(date))
            }
        }

        // Show DatePicker for end date
        endDateEditText.setOnClickListener {
            showDatePicker { date ->
                endDateMillis = date
                endDateEditText.setText(formatDate(date))
            }
        }

        // Create button click
        createButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty() || startDateMillis == 0L || endDateMillis == 0L) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (endDateMillis < startDateMillis) {
                Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Semester object
            val semester = Semester(
                name = name,
                startDate = startDateMillis,
                endDate = endDateMillis
            )

            val semesterMap = mapOf(
                "name" to name,
                "startDate" to startDateMillis,
                "endDate" to endDateMillis
            )

            db.collection("semesters")
                .add(semesterMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Semester created", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to create semester: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
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
}
