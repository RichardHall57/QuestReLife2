package com.example.questrelife

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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AssignmentsFragment : Fragment() {

    companion object {
        private const val ARG_CLASS_ID = "class_id"
        private const val ARG_CLASS_NAME = "class_name"

        fun newInstance(classId: String, className: String): AssignmentsFragment {
            val fragment = AssignmentsFragment()
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
    private lateinit var adapter: AssignmentAdapter  // Your adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString(ARG_CLASS_ID)
        className = arguments?.getString(ARG_CLASS_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assignments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerTextView = view.findViewById<TextView>(R.id.header_text)
        headerTextView.text = className ?: "Assignments"

        val backButton = view.findViewById<Button>(R.id.button_back)
        backButton.setOnClickListener { parentFragmentManager.popBackStack() }

        val addAssignmentButton = view.findViewById<Button>(R.id.add_assignment_button)
        addAssignmentButton.setOnClickListener {
            val id = classId
            val name = className
            if (id != null && name != null) {
                val createFragment = CreateAssignmentFragment.newInstance(id, name)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, createFragment)
                    .addToBackStack(null)
                    .commit()
            } else Log.e("AssignmentsFragment", "Class ID or Class Name is null")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.assignments_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Pass delete lambda to adapter
        adapter = AssignmentAdapter { assignment ->
            classId?.let { cid ->
                db.collection("Classes")
                    .document(cid)
                    .collection("Assignments")
                    .document(assignment.id)
                    .delete()
                    .addOnSuccessListener { Log.d("AssignmentsFragment", "Deleted ${assignment.title}") }
                    .addOnFailureListener { e -> Log.e("AssignmentsFragment", "Delete failed", e) }
            }
        }

        recyclerView.adapter = adapter

        classId?.let { fetchAssignmentsForClass(it) }
            ?: Log.e("AssignmentsFragment", "No class ID found in arguments!")
    }

    private fun fetchAssignmentsForClass(classId: String) {
        db.collection("Classes")
            .document(classId)
            .collection("Assignments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AssignmentsFragment", "Listen failed", error)
                    return@addSnapshotListener
                }

                val assignments = snapshot?.documents?.map { doc ->
                    AssignmentItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getTimestamp("dueDate") ?: Timestamp.now(),
                        grade = doc.getDouble("grade")?.toFloat() ?: 0f // <--- ADD THIS
                    )
                } ?: emptyList()

                adapter.submitList(assignments)

            }
    }
}