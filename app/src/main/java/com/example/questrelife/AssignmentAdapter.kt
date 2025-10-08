package com.example.questrelife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AssignmentAdapter(
    private val onDelete: (AssignmentItem) -> Unit // Callback for delete
) : RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    private val assignmentList = mutableListOf<AssignmentItem>()

    fun submitList(list: List<AssignmentItem>) {
        assignmentList.clear()
        assignmentList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = assignmentList[position]
        holder.titleView.text = assignment.title
        holder.descriptionView.text = assignment.description

        val date = assignment.dueDate.toDate()
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.dueDateView.text = "Due: ${formatter.format(date)}"


        // Call gradeFunction to get the letter grade
        val numericGrade = assignment.grade
        val letterGrade = gradeFunction(numericGrade)

        holder.gradeView.text = "Grade: $numericGrade ($letterGrade)"


        // Each delete button works for its own assignment
        holder.deleteButton.setOnClickListener {
            onDelete(assignment)
        }
    }

    override fun getItemCount(): Int = assignmentList.size

    // Function to calculate letter grade
    private fun gradeFunction(avg: Float): Char {
        return when {
            avg >= 90 -> 'A'
            avg >= 80 -> 'B'
            avg >= 70 -> 'C'
            avg >= 60 -> 'D'
            else -> 'F'
        }
    }

    class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.assignment_title)
        val descriptionView: TextView = itemView.findViewById(R.id.assignment_description)
        val dueDateView: TextView = itemView.findViewById(R.id.assignment_due_date)
        val gradeView: TextView = itemView.findViewById(R.id.assignment_grade)
        val deleteButton: Button = itemView.findViewById(R.id.button_delete_assignment) // Individual delete
    }
}
