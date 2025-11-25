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
        private const val ARG_ASSIGNMENT_ID = "assignmentId"

        fun newInstance(classId: String, className: String, assignmentId: String? = null): CreateAssignmentFragment {
            val fragment = CreateAssignmentFragment()
            val args = Bundle()
            args.putString(ARG_CLASS_ID, classId)
            args.putString(ARG_CLASS_NAME, className)
            assignmentId?.let { args.putString(ARG_ASSIGNMENT_ID, it) }
            fragment.arguments = args
            return fragment
        }
    }

    private var classId: String? = null
    private var className: String? = null
    private var assignmentId: String? = null
    private val db = FirebaseFirestore.getInstance()

    private lateinit var headerTextView: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var gradeEditText: EditText
    private lateinit var dueDateButton: Button
    private lateinit var createButton: Button
    private lateinit var selectedDueDate: Calendar
    private lateinit var categorySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString(ARG_CLASS_ID)
        className = arguments?.getString(ARG_CLASS_NAME)
        assignmentId = arguments?.getString(ARG_ASSIGNMENT_ID)
        selectedDueDate = Calendar.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.create_fragment_assignment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerTextView = view.findViewById(R.id.header_text)
        titleEditText = view.findViewById(R.id.assignment_title_edit_text)
        descriptionEditText = view.findViewById(R.id.assignment_description_edit_text)
        gradeEditText = view.findViewById(R.id.assignment_grade_edit_text)
        dueDateButton = view.findViewById(R.id.due_date_button)
        createButton = view.findViewById(R.id.create_assignment_button)
        categorySpinner = view.findViewById(R.id.assignment_type_spinner)

        setupCategorySpinner()
        updateDueDateButtonText()
        dueDateButton.setOnClickListener { showDatePicker() }

        // Check if editing existing assignment
        if (assignmentId != null) {
            headerTextView.text = "Edit Assignment"
            loadAssignmentData()
        } else {
            headerTextView.text = "Add Assignment to: ${className ?: "Class"}"
        }

        createButton.setOnClickListener {
            saveAssignment()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "Classwork",
            "Homework",
            "Quiz",
            "Test",
            "Project",
            "Participation",
            "Other"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
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

    private fun loadAssignmentData() {
        val cid = classId ?: return
        val aid = assignmentId ?: return

        db.collection("Classes")
            .document(cid)
            .collection("Assignments")
            .document(aid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    titleEditText.setText(doc.getString("title"))
                    descriptionEditText.setText(doc.getString("description"))
                    gradeEditText.setText(doc.getDouble("grade")?.toFloat()?.toString() ?: "0")
                    val type = doc.getString("type") ?: "Other"
                    val typeIndex = (categorySpinner.adapter as ArrayAdapter<String>).getPosition(type)
                    categorySpinner.setSelection(if (typeIndex >= 0) typeIndex else 0)

                    val ts = doc.getTimestamp("dueDate") ?: Timestamp.now()
                    selectedDueDate.time = ts.toDate()
                    updateDueDateButtonText()
                }
            }
            .addOnFailureListener { e ->
                Log.e("CreateAssignment", "Failed to load assignment", e)
                Toast.makeText(requireContext(), "Failed to load assignment", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAssignment() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val grade = gradeEditText.text.toString().toFloatOrNull() ?: 0f
        val selectedType = categorySpinner.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val cid = classId ?: run {
            Toast.makeText(requireContext(), "Class not found", Toast.LENGTH_SHORT).show()
            return
        }

        val assignmentData = hashMapOf(
            "title" to title,
            "description" to description,
            "dueDate" to Timestamp(selectedDueDate.time),
            "grade" to grade,
            "type" to selectedType
        )

        if (assignmentId != null) {
            // Update existing assignment
            db.collection("Classes")
                .document(cid)
                .collection("Assignments")
                .document(assignmentId!!)
                .update(assignmentData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Assignment updated", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.e("CreateAssignment", "Failed to update assignment", e)
                    Toast.makeText(requireContext(), "Failed to update assignment", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new assignment
            db.collection("Classes")
                .document(cid)
                .collection("Assignments")
                .add(assignmentData)
                .addOnSuccessListener { docRef ->
                    docRef.update("id", docRef.id)
                    Toast.makeText(requireContext(), "Assignment created", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.e("CreateAssignment", "Failed to create assignment", e)
                    Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

