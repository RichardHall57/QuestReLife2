package com.example.questrelife

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GradeCalculationAdapter(
    private val assignments: MutableList<AssignmentItem> = mutableListOf(),
    private val onProgressUpdate: ((Int) -> Unit)? = null,
    private val onCompletionUpdate: ((List<Float>) -> Unit)? = null
) : RecyclerView.Adapter<GradeCalculationAdapter.AssignmentViewHolder>() {

    inner class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.assignment_title)
        val descriptionText: TextView = itemView.findViewById(R.id.assignment_description)
        val statusText: TextView = itemView.findViewById(R.id.assignment_grade)
        val dueDateText: TextView = itemView.findViewById(R.id.assignment_due_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = assignments[position]
        holder.titleText.text = assignment.title
        holder.descriptionText.text = assignment.description

        val grade = assignment.grade
        if (grade > 0f) {
            val letter = convertToLetter(grade)
            holder.statusText.text =
                "Completed – Grade: ${"%.1f".format(grade)}% ($letter)"

            // ✅ Color coding
            when {
                grade >= 80 -> holder.statusText.setTextColor(Color.parseColor("#2E7D32")) // green
                grade in 70.0..79.99 -> holder.statusText.setTextColor(Color.parseColor("#F9A825")) // yellow
                else -> holder.statusText.setTextColor(Color.parseColor("#C62828")) // red
            }
        } else {
            holder.statusText.text = "Incomplete – Not graded yet"
            holder.statusText.setTextColor(Color.GRAY)
        }

        val dueDate = assignment.dueDate.toDate().toString().substring(0, 16)
        holder.dueDateText.text = "Due: $dueDate"
    }

    override fun getItemCount(): Int = assignments.size

    fun submitList(newList: List<AssignmentItem>) {
        assignments.clear()
        assignments.addAll(newList)
        notifyDataSetChanged()
        updateProgress()
        onCompletionUpdate?.invoke(assignments.map { it.grade })
    }

    private fun updateProgress() {
        if (assignments.isEmpty()) {
            onProgressUpdate?.invoke(0)
            return
        }
        val completedCount = assignments.count { it.grade > 0f }
        val progress = (completedCount.toFloat() / assignments.size * 100).toInt()
        onProgressUpdate?.invoke(progress)
    }

    private fun convertToLetter(grade: Float): String {
        return when {
            grade >= 90 -> "A"
            grade >= 80 -> "B"
            grade >= 70 -> "C"
            grade >= 60 -> "D"
            else -> "F"
        }
    }
}
