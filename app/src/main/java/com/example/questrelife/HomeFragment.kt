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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Default avatar
        binding.ivProfileImage.setImageResource(R.drawable.ic_default_avatar)
        binding.ivProfileImage.setOnClickListener { replaceProfileImage() }

        binding.tvWelcome.text = "Welcome back, Adventurer!"

        loadStats()

        binding.btnOpenLog.setOnClickListener {
            Snackbar.make(binding.root, "Opening Quest Log…", Snackbar.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun loadStats() {
        // Reset MDPs
        homeworkMDP.reset()
        testMDP.reset()
        projectMDP.reset()

        // Fetch assignments from Firestore
        db.collection("Classes")
            .get()
            .addOnSuccessListener { classesSnapshot ->
                val allAssignments = mutableListOf<AssignmentItem>()

                classesSnapshot.documents.forEach { classDoc ->
                    db.collection("Classes").document(classDoc.id)
                        .collection("Assignments")
                        .get()
                        .addOnSuccessListener { assignmentsSnapshot ->
                            assignmentsSnapshot.documents.forEach { doc ->
                                val gradeValue = doc.get("grade")
                                val gradeFloat = when (gradeValue) {
                                    is Number -> gradeValue.toFloat()
                                    is String -> gradeValue.toFloatOrNull() ?: 0f
                                    else -> 0f
                                }
                                val assignment = AssignmentItem(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "Untitled",
                                    description = doc.getString("description") ?: "",
                                    dueDate = doc.getTimestamp("dueDate") ?: Timestamp.now(),
                                    grade = gradeFloat,
                                    type = doc.getString("type") ?: "Other"
                                )
                                allAssignments.add(assignment)

                                // Update MDPs
                                when (assignment.type) {
                                    "Homework" -> homeworkMDP.stepWithGrade(assignment.grade)
                                    "Test" -> testMDP.stepWithGrade(assignment.grade)
                                    "Project" -> projectMDP.stepWithGrade(assignment.grade)
                                }
                            }

                            // Update UI
                            binding.homeworkProgress.progress =
                                (homeworkMDP.gpa / 4.0 * 100).toInt()
                            binding.testProgress.progress =
                                (testMDP.gpa / 4.0 * 100).toInt()
                            binding.projectProgress.progress =
                                (projectMDP.gpa / 4.0 * 100).toInt()

                            binding.tvHomework.text =
                                "Homework: ${homeworkMDP.state.label} | GPA: ${"%.2f".format(homeworkMDP.gpa)}"
                            binding.tvTests.text =
                                "Tests: ${testMDP.state.label} | GPA: ${"%.2f".format(testMDP.gpa)}"
                            binding.tvProjects.text =
                                "Projects: ${projectMDP.state.label} | GPA: ${"%.2f".format(projectMDP.gpa)}"

                            // Optional: compute level based on GPA average
                            val averageGPA = (homeworkMDP.gpa + testMDP.gpa + projectMDP.gpa) / 3.0
                            val level = (averageGPA / 4.0 * 10).toInt() // example level system
                            binding.tvLevel.text = "Level: $level"
                            binding.levelProgress.progress = (averageGPA / 4.0 * 100).toInt()
                        }
                }
            }
    }

    private fun replaceProfileImage() {
        val prefs = requireContext().getSharedPreferences("MindSpirePrefs", 0)
        val imageIdentifier = prefs.getString("profile_image_identifier", null)
        val bitmap = imageIdentifier?.let { ImageUtils.loadBitmap(requireContext(), it) }

        if (bitmap != null) {
            binding.ivProfileImage.setImageBitmap(bitmap)
            Snackbar.make(binding.root, "Profile image updated!", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(
                binding.root,
                "No AI image found. Generate one in Profile.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
