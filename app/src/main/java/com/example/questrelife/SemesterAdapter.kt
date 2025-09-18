package com.example.questrelife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SemesterAdapter(
    private val onClick: (Semester) -> Unit,
    private val onDelete: (Semester) -> Unit
) : ListAdapter<Semester, SemesterAdapter.SemesterViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_semester, parent, false)
        return SemesterViewHolder(view, onClick, onDelete)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SemesterViewHolder(
        itemView: View,
        private val onClick: (Semester) -> Unit,
        private val onDelete: (Semester) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.textSemesterName)
        private val datesTextView: TextView = itemView.findViewById(R.id.textSemesterDates)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        private var currentSemester: Semester? = null

        fun bind(semester: Semester) {
            currentSemester = semester
            nameTextView.text = semester.name
            datesTextView.text = "${formatDate(semester.startDate)} - ${formatDate(semester.endDate)}"

            itemView.setOnClickListener { currentSemester?.let(onClick) }
            deleteButton.setOnClickListener { currentSemester?.let(onDelete) }
        }

        private fun formatDate(millis: Long): String {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = millis
            val month = cal.get(java.util.Calendar.MONTH) + 1
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val year = cal.get(java.util.Calendar.YEAR)
            return "$month/$day/$year"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Semester>() {
        override fun areItemsTheSame(oldItem: Semester, newItem: Semester) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Semester, newItem: Semester) = oldItem == newItem
    }
}
