package com.example.questrelife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GradeCalculationAdapter(
    private val assignments: MutableList<AssignmentItem> = mutableListOf()
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

        // Use the gradeFunction to show both numeric and letter grade
        val numericGrade = assignment.grade
        val letterGrade = gradeFunction(numericGrade)

        holder.gradeText.text = "Grade: $numericGrade ($letterGrade)"
        holder.dueDateText.text = "Due: ${assignment.dueDate.toDate()}"
    }

    override fun getItemCount(): Int = assignments.size

    fun submitList(newList: List<AssignmentItem>) {
        assignments.clear()
        assignments.addAll(newList)
        notifyDataSetChanged()
    }

    // This function returns a letter grade based on the average
    private fun gradeFunction(avg: Float): Char {
        return when {
            avg >= 90 -> 'A'
            avg >= 80 -> 'B'
            avg >= 70 -> 'C'
            avg >= 60 -> 'D'
            else -> 'F'
        }
    }
}
