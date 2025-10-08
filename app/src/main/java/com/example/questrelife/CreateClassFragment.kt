package com.example.questrelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CreateClassFragment : Fragment() {

    companion object {
        private const val ARG_SEMESTER_ID = "semester_id"
        private const val ARG_SEMESTER_NAME = "semester_name"

        fun newInstance(semesterId: String, semesterName: String): CreateClassFragment {
            val fragment = CreateClassFragment()
            val args = Bundle()
            args.putString(ARG_SEMESTER_ID, semesterId)
            args.putString(ARG_SEMESTER_NAME, semesterName)
            fragment.arguments = args
            return fragment
        }
    }

    private var semesterId: String? = null
    private var semesterName: String? = null
    private val db = FirebaseFirestore.getInstance()

    private lateinit var headerTextView: TextView
    private lateinit var classNameEditText: EditText
    private lateinit var teacherEditText: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        semesterId = arguments?.getString(ARG_SEMESTER_ID)
        semesterName = arguments?.getString(ARG_SEMESTER_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_fragment_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerTextView = view.findViewById(R.id.header_text)
        classNameEditText = view.findViewById(R.id.class_name_edit_text)
        teacherEditText = view.findViewById(R.id.teacher_name_edit_text)
        createButton = view.findViewById(R.id.create_class_button)

        headerTextView.text = "Add Class to: ${semesterName ?: "Semester"}"

        createButton.setOnClickListener {
            val className = classNameEditText.text.toString().trim()
            val teacher = teacherEditText.text.toString().trim()

            if (className.isEmpty() || teacher.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val semesterIdValue = semesterId
            if (semesterIdValue == null) {
                Toast.makeText(requireContext(), "Semester not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classItem = ClassItem(
                id = "",  // You can leave empty or generate if needed, Firestore will generate ID
                name = className,
                teacher = teacher
            )

            addClassToFirestore(classItem, semesterIdValue)
        }
    }

    private fun addClassToFirestore(classItem: ClassItem, semesterId: String) {
        addClass(
            db,
            classItem,
            semesterId,
            onSuccess = {
                Toast.makeText(requireContext(), "Class created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to create class: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

