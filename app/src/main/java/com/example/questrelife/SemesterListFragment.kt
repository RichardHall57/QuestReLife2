package com.example.questrelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SemesterListFragment : Fragment() {

    private lateinit var adapter: SemesterAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_semester_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.semester_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SemesterAdapter(
            onClick = { semester ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SemesterFragment.newInstance(semester))
                    .addToBackStack(null)
                    .commit()
            },
            onDelete = { semester ->
                deleteSemesterFromFirestore(semester)
            }
        )

        recyclerView.adapter = adapter
        fetchSemesters()
    }

    private fun fetchSemesters() {
        db.collection("Semester")
            .get()
            .addOnSuccessListener { documents ->
                val semesters = documents.map { doc ->
                    Semester(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        startDate = doc.getLong("startDate") ?: 0L,
                        endDate = doc.getLong("endDate") ?: 0L
                    )
                }
                adapter.submitList(semesters)
            }
    }

    private fun deleteSemesterFromFirestore(semester: Semester) {
        db.collection("Semester").document(semester.id)
            .delete()
            .addOnSuccessListener {
                val updatedList = adapter.currentList.toMutableList()
                updatedList.remove(semester)
                adapter.submitList(updatedList)
            }
    }
}
