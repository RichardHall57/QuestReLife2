package com.example.questrelife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GradeCalculationAdapter(
    private val assignments: MutableList<AssignmentItem> = mutableListOf(),
    private val onProgressUpdate: ((Int) -> Unit)? = null,
    private val onGradesUpdate: ((List<Float>) -> Unit)? = null
) : RecyclerView.Adapter<GradeCalculationAdapter.AssignmentViewHolder>() {

    inner class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.assignment_title)
        val descriptionText: TextView = itemView.findViewById(R.id.assignment_description)
        val gradeText: TextView = itemView.findViewById(R.id.assignment_grade)
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
        holder.gradeText.text = "Grade: ${assignment.grade} (${gradeFunction(assignment.grade)})"
        holder.dueDateText.text = "Due: ${assignment.dueDate.toDate()}"
    }

    override fun getItemCount(): Int = assignments.size

    fun submitList(newList: List<AssignmentItem>) {
        assignments.clear()
        assignments.addAll(newList)
        notifyDataSetChanged()
        updateProgress()
        onGradesUpdate?.invoke(assignments.map { it.grade })
    }

    private fun updateProgress() {
        if (assignments.isEmpty()) {
            onProgressUpdate?.invoke(0)
            return
        }
        val completedCount = assignments.count { it.grade > 0 }
        val progress = (completedCount.toFloat() / assignments.size * 100).toInt()
        onProgressUpdate?.invoke(progress)
    }

    private fun gradeFunction(avg: Float): Char = when {
        avg >= 90 -> 'A'
        avg >= 80 -> 'B'
        avg >= 70 -> 'C'
        avg >= 60 -> 'D'
        else -> 'F'
    }
}
