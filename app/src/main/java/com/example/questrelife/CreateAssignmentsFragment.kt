package com.example.questrelife

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateAssignmentFragment : Fragment() {

    companion object {
        private const val ARG_CLASS_ID = "class_id"
        private const val ARG_CLASS_NAME = "class_name"

        fun newInstance(classId: String, className: String): CreateAssignmentFragment {
            val fragment = CreateAssignmentFragment()
            val args = Bundle()
            args.putString(ARG_CLASS_ID, classId)
            args.putString(ARG_CLASS_NAME, className)
            fragment.arguments = args
            return fragment
        }
    }

    private var classId: String? = null
    private var className: String? = null
    private val db = FirebaseFirestore.getInstance()

    private lateinit var headerTextView: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dueDateButton: Button
    private lateinit var createButton: Button
    private lateinit var selectedDueDate: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString(ARG_CLASS_ID)
        className = arguments?.getString(ARG_CLASS_NAME)
        selectedDueDate = Calendar.getInstance()

        Log.d("CreateAssignment", "classId=$classId, className=$className")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_fragment_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerTextView = view.findViewById(R.id.header_text)
        titleEditText = view.findViewById(R.id.assignment_title_edit_text)
        descriptionEditText = view.findViewById(R.id.assignment_description_edit_text)
        val gradeEditText = view.findViewById<EditText>(R.id.assignment_grade_edit_text) // NEW
        dueDateButton = view.findViewById(R.id.due_date_button)
        createButton = view.findViewById(R.id.create_assignment_button)

        headerTextView.text = "Add Assignment to: ${className ?: "Class"}"
        updateDueDateButtonText()

        dueDateButton.setOnClickListener { showDatePicker() }

        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val grade = gradeEditText.text.toString().toFloatOrNull() ?: 0f // READ GRADE

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classIdValue = classId
            if (classIdValue == null) {
                Toast.makeText(requireContext(), "Class not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dueTimestamp = Timestamp(selectedDueDate.time)

            // Build AssignmentItem
            val assignmentItem = AssignmentItem(
                id = "", // will be filled after Firestore generates ID
                title = title,
                description = description,
                dueDate = dueTimestamp,
                grade = grade
            )

            addAssignmentToFirestore(assignmentItem, classIdValue)
        }
    }

    private fun updateDueDateButtonText() {
        val format = android.text.format.DateFormat.getDateFormat(requireContext())
        dueDateButton.text = "Due Date: ${format.format(selectedDueDate.time)}"
    }

    private fun showDatePicker() {
        val year = selectedDueDate.get(Calendar.YEAR)
        val month = selectedDueDate.get(Calendar.MONTH)
        val day = selectedDueDate.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDueDate.set(y, m, d)
            updateDueDateButtonText()
        }, year, month, day).show()
    }

    private fun addAssignmentToFirestore(assignment: AssignmentItem, classId: String) {
        val assignmentsRef = db.collection("Classes")
            .document(classId)
            .collection("Assignments")

        assignmentsRef.add(assignment)
            .addOnSuccessListener { docRef ->
                // Update the Firestore-generated ID into the assignment document
                docRef.update("id", docRef.id)

                Toast.makeText(requireContext(), "Assignment created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to create assignment", e)
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
