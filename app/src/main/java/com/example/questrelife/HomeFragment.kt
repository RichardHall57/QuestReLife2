package com.example.questrelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.questrelife.databinding.FragmentHomeBinding
import com.example.questrelife.mdp.AcademicMDP
import com.example.questrelife.utils.ImageUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    // Academic MDP per type
    private val homeworkMDP = AcademicMDP()
    private val testMDP = AcademicMDP()
    private val projectMDP = AcademicMDP()

    private var assignments: MutableList<AssignmentItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Default avatar
        binding.ivProfileImage.setImageResource(R.drawable.ic_default_avatar)
        binding.ivProfileImage.setOnClickListener { replaceProfileImage() }

        binding.tvWelcome.text = "Welcome back, Adventurer!"

        // Load assignments and update UI
        loadAssignments()

        binding.btnOpenLog.setOnClickListener {
            Snackbar.make(binding.root, "Opening Quest Log…", Snackbar.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun loadAssignments() {
        // Step 1: Add dummy data first
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
                description = "Group presentation",
                dueDate = Timestamp.now(),
                grade = 92f,
                type = "Project"
            )
        )

        updateUIWithAssignments()

        // Step 2: Fetch from Firestore and replace if available
        db.collection("Classes").get().addOnSuccessListener { classDocs ->
            val fetchedAssignments = mutableListOf<AssignmentItem>()
            val totalClasses = classDocs.size()
            if (totalClasses == 0) return@addOnSuccessListener

            var processed = 0
            for (classDoc in classDocs) {
                db.collection("Classes").document(classDoc.id)
                    .collection("Assignments")
                    .get()
                    .addOnSuccessListener { assignmentDocs ->
                        for (doc in assignmentDocs) {
                            val due = doc.getTimestamp("dueDate") ?: Timestamp.now()
                            val assignment = AssignmentItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                dueDate = due,
                                grade = (doc.getDouble("grade") ?: 0.0).toFloat(),
                                type = doc.getString("type") ?: "Other"
                            )
                            fetchedAssignments.add(assignment)
                        }
                        processed++
                        if (processed == totalClasses) {
                            assignments = fetchedAssignments
                            updateUIWithAssignments()
                        }
                    }
                    .addOnFailureListener {
                        processed++
                        if (processed == totalClasses) {
                            assignments = fetchedAssignments
                            updateUIWithAssignments()
                        }
                    }
            }
        }
    }

    private fun updateUIWithAssignments() {
        // Reset MDPs
        homeworkMDP.reset()
        testMDP.reset()
        projectMDP.reset()

        // Step 1: Compute MDPs for all assignments
        for (assignment in assignments) {
            when (assignment.type) {
                "Homework" -> homeworkMDP.stepWithGrade(assignment.grade)
                "Test" -> testMDP.stepWithGrade(assignment.grade)
                "Project" -> projectMDP.stepWithGrade(assignment.grade)
            }
        }

        // Step 2: Update progress bars
        binding.homeworkProgress.progress = (homeworkMDP.gpa / 4.0 * 100).toInt()
        binding.testProgress.progress = (testMDP.gpa / 4.0 * 100).toInt()
        binding.projectProgress.progress = (projectMDP.gpa / 4.0 * 100).toInt()

        // Step 3: Update labels
        binding.tvHomework.text =
            "Homework: ${homeworkMDP.state.label} | GPA: ${"%.2f".format(homeworkMDP.gpa)}"
        binding.tvTests.text =
            "Tests: ${testMDP.state.label} | GPA: ${"%.2f".format(testMDP.gpa)}"
        binding.tvProjects.text =
            "Projects: ${projectMDP.state.label} | GPA: ${"%.2f".format(projectMDP.gpa)}"

        // Step 4: Update level
        val avgGPA = (homeworkMDP.gpa + testMDP.gpa + projectMDP.gpa) / 3.0
        binding.tvLevel.text = "Level: ${(avgGPA / 4.0 * 10).toInt()}"
        binding.levelProgress.progress = (avgGPA / 4.0 * 100).toInt()
    }

    private fun replaceProfileImage() {
        val prefs = requireContext().getSharedPreferences("MindSpirePrefs", 0)
        val imageIdentifier = prefs.getString("profile_image_identifier", null)
        val bitmap = imageIdentifier?.let { ImageUtils.loadBitmap(requireContext(), it) }

        if (bitmap != null) {
            binding.ivProfileImage.setImageBitmap(bitmap)
            Snackbar.make(binding.root, "Profile image updated!", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "No AI image found. Generate one in Profile.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
