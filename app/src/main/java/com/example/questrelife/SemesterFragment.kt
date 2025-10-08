package com.example.questrelife

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import java.util.Calendar
import com.google.firebase.Timestamp
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date


class SemesterFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: SemesterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_semester, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView setup
        val recyclerView = view.findViewById<RecyclerView>(R.id.semester_recycler_view)
        if (recyclerView == null) {
            Log.e("SemesterFragment", "RecyclerView with id semester_recycler_view NOT found in fragment_semester.xml!")
            return
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SemesterAdapter(
            onClick = { semester ->
                Log.d("SemesterFragment", "Clicked on semester: ${semester.name}")
                // Optional: navigate to semester details
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ClassFragment.newInstance(semester.id, semester.name))
                    .addToBackStack(null)
                    .commit()


            },
            onDelete = { semester ->
                Log.d("SemesterFragment", "Deleting semester: ${semester.name}")
                deleteSemesterFromFirestore(semester)
            }
        )

        recyclerView.adapter = adapter

        // Add Semester button setup (safe lookup)
        val addSemesterButton = view.findViewById<Button>(R.id.add_semester_button)
        if (addSemesterButton != null) {
            addSemesterButton.setOnClickListener {
                Log.d("SemesterFragment", "Navigating to CreateSemesterFragment")
                try {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CreateSemesterFragment())
                        .addToBackStack(null)
                        .commit()
                } catch (e: IllegalArgumentException) {
                    Log.e("SemesterFragment", "No fragment_container found in activity layout!", e)
                }
            }
        } else {
            Log.w("SemesterFragment", "No add_semester_button found in layout, skipping setup.")
        }

        fetchSemesters()
    }

    private fun fetchSemesters() {
        db.collection("Semester")
            .get()
            .addOnSuccessListener { documents ->
                val semesters = documents.map { doc ->
                    val startTimestamp: Timestamp = when (val value = doc["startDate"]) {
                        is Timestamp -> value
                        is Date -> Timestamp(value)
                        else -> Timestamp.now()
                    }

                    val endTimestamp: Timestamp = when (val value = doc["endDate"]) {
                        is Timestamp -> value
                        is Date -> Timestamp(value)
                        else -> Timestamp.now()
                    }

                    Semester(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        startDate = startTimestamp,
                        endDate = endTimestamp
                    )
                }.sortedBy { it.startDate.seconds } // sort by timestamp

                val rows = mutableListOf<SemesterRow>()

                val semestersByYear = semesters.groupBy { sem ->
                    val cal = Calendar.getInstance()
                    cal.time = sem.startDate.toDate()
                    cal.get(Calendar.YEAR).toString()
                }

                semestersByYear.forEach { (year, semestersInYear) ->
                    rows.add(SemesterRow.YearHeader(year))
                    semestersInYear.forEach { sem ->
                        rows.add(SemesterRow.SemesterItem(sem))
                    }
                }

                adapter.submitList(rows.toList())
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


    private fun deleteSemesterFromFirestore(semester: Semester) {
        db.collection("Semester").document(semester.id)
            .delete()
            .addOnSuccessListener {
                Log.d("SemesterFragment", "Successfully deleted semester ${semester.name}")
                val updatedRows = adapter.currentList
                    .filterNot { it is SemesterRow.SemesterItem && it.semester.id == semester.id }
                    .toList()
                adapter.submitList(updatedRows)
            }
            .addOnFailureListener { e ->
                Log.e("SemesterFragment", "Failed to delete semester ${semester.name}", e)
            }
    }
}

