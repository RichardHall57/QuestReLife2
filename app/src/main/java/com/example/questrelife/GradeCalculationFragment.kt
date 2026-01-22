package com.example.questrelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class GradeCalculationFragment : Fragment() {

    companion object {
        private const val ARG_CLASS_ID = "class_id"
        private const val ARG_CLASS_NAME = "class_name"

        fun newInstance(classId: String, className: String): GradeCalculationFragment {
            val fragment = GradeCalculationFragment()
            val args = Bundle()
            args.putString(ARG_CLASS_ID, classId)
            args.putString(ARG_CLASS_NAME, className)
            fragment.arguments = args
            return fragment
        }
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var assignmentsRecyclerView: RecyclerView
    private lateinit var assignmentsAdapter: GradeCalculationAdapter

    private var assignments: MutableList<AssignmentItem> = mutableListOf()

    private var classId: String? = null
    private var className: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classId = arguments?.getString(ARG_CLASS_ID)
        className = arguments?.getString(ARG_CLASS_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_grade_calculation, container, false)

        assignmentsRecyclerView = view.findViewById(R.id.assignments_recycler_view)
        assignmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Step 1: Dummy assignments (fallback / preview)
        assignments = mutableListOf(
            AssignmentItem(
                id = "1",
                title = "Math Homework",
                description = "Chapter 3 exercises",
                dueDate = Timestamp.now(),
                grade = 95f,
                type = "Homework"
            ),
            AssignmentItem(
                id = "2",
                title = "Science Test",
                description = "Lab quiz",
                dueDate = Timestamp.now(),
                grade = 88f,
                type = "Test"
            ),
            AssignmentItem(
                id = "3",
                title = "History Project",
                description = "Presentation",
                dueDate = Timestamp.now(),
                grade = 92f,
                type = "Project"
            )
        )

        assignmentsAdapter = GradeCalculationAdapter(assignments)
        assignmentsRecyclerView.adapter = assignmentsAdapter

        // Step 2: Fetch Firestore assignments (if classId exists)
        classId?.let { id ->
            db.collection("Classes")
                .document(id)
                .collection("Assignments")
                .get()
                .addOnSuccessListener { assignmentDocs ->
                    val fetchedAssignments = mutableListOf<AssignmentItem>()
                    for (doc in assignmentDocs) {
                        val due = doc.getTimestamp("dueDate") ?: Timestamp.now()
                        fetchedAssignments.add(
                            AssignmentItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                dueDate = due,
                                grade = (doc.getDouble("grade") ?: 0.0).toFloat(),
                                type = doc.getString("type") ?: "Other"
                            )
                        )
                    }
                    if (fetchedAssignments.isNotEmpty()) {
                        assignmentsAdapter.submitList(fetchedAssignments)
                    }
                }
        }

        return view
    }
}
