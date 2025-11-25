package com.example.questrelife

import ClassAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class ClassFragment : Fragment() {

    companion object {
        private const val ARG_SEMESTER_ID = "semester_id"
        private const val ARG_SEMESTER_NAME = "semester_name"

        fun newInstance(semesterId: String, semesterName: String): ClassFragment {
            val fragment = ClassFragment()
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
    private lateinit var adapter: ClassAdapter
    private var selectedClassId: String? = null
    private var selectedClassName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        semesterId = arguments?.getString(ARG_SEMESTER_ID)
        semesterName = arguments?.getString(ARG_SEMESTER_NAME)
        Log.d("ClassFragment", "SemesterId=$semesterId, SemesterName=$semesterName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_class, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerTextView = view.findViewById<TextView>(R.id.header_text)
        headerTextView.text = semesterName ?: "Classes"

        view.findViewById<Button>(R.id.button_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.add_class_button).setOnClickListener {
            semesterId?.let { id ->
                val createClassFragment = CreateClassFragment.newInstance(id, semesterName ?: "")
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, createClassFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        view.findViewById<Button>(R.id.view_grades_button).setOnClickListener {
            if (!selectedClassId.isNullOrEmpty() && !selectedClassName.isNullOrEmpty()) {
                val gradeFragment = GradeCalculationFragment.newInstance(
                    selectedClassId!!,
                    selectedClassName!!
                )
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, gradeFragment) // Use ADD to overlay
                    .addToBackStack(null)
                    .commit()
            } else {
                Log.e("ClassFragment", "No class selected for viewing grades!")
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.class_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ClassAdapter { classItem ->
            selectedClassId = classItem.id
            selectedClassName = classItem.name

            val assignmentsFragment = AssignmentsFragment.newInstance(
                classItem.id,
                classItem.name
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, assignmentsFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        semesterId?.let { fetchClassesForSemester(it) }
            ?: Log.e("ClassFragment", "No semester ID found in arguments!")
    }

    private fun fetchClassesForSemester(semesterId: String) {
        db.collection("Classes")
            .whereEqualTo("semesterId", semesterId)
            .get()
            .addOnSuccessListener { documents ->
                val classes = documents.map { doc ->
                    ClassItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        teacher = doc.getString("teacher") ?: ""
                    )
                }
                adapter.submitList(classes)
            }
            .addOnFailureListener { e ->
                Log.e("ClassFragment", "Failed to fetch classes for semester $semesterId", e)
            }
    }
}