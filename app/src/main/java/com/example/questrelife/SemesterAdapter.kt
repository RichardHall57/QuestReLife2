package com.example.questrelife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import android.util.Log


class SemesterAdapter(
    private val onClick: (Semester) -> Unit,
    private val onDelete: (Semester) -> Unit
) : ListAdapter<SemesterRow, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SemesterRow.YearHeader -> 0
            is SemesterRow.SemesterItem -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_year_header, parent, false)
            YearHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_semester, parent, false)
            SemesterItemViewHolder(view, onClick, onDelete)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is SemesterRow.YearHeader -> (holder as YearHeaderViewHolder).bind(row)
            is SemesterRow.SemesterItem -> (holder as SemesterItemViewHolder).bindSafe(row)
        }
    }

    // ----------- Year Header ViewHolder -----------
    class YearHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val yearTextView: TextView = itemView.findViewById(R.id.year_text)
        fun bind(row: SemesterRow.YearHeader) {
            yearTextView.text = row.year
        }
    }

    // ----------- Semester Item ViewHolder -----------
    class SemesterItemViewHolder(
        itemView: View,
        private val onClick: (Semester) -> Unit,
        private val onDelete: (Semester) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView? = itemView.findViewById(R.id.textSemesterName)
        private val datesTextView: TextView? = itemView.findViewById(R.id.textSemesterDates)
        private val deleteButton: Button? = itemView.findViewById(R.id.delete_button)

        // Safe binding wrapper
        fun bindSafe(row: SemesterRow.SemesterItem) {
            try {
                bind(row)
            } catch (e: Exception) {
                Log.e("SemesterAdapter", "Error binding semester item", e)
            }
        }

        private fun bind(row: SemesterRow.SemesterItem) {
            val semester = row.semester ?: return

            // Set semester name safely
            nameTextView?.text = semester.name ?: "Untitled"

            // Set semester dates safely
            if (semester.startDate != null && semester.endDate != null) {
                datesTextView?.text =
                    "${formatDate(semester.startDate)} - ${formatDate(semester.endDate)}"
            } else {
                datesTextView?.text = "Dates not set"
            }

            // Click listeners
            itemView.setOnClickListener { onClick(semester) }
            deleteButton?.setOnClickListener { onDelete(semester) }
        }

        // Format Firebase Timestamp to MM/DD/YYYY
        private fun formatDate(timestamp: Timestamp): String {
            val cal = java.util.Calendar.getInstance()
            cal.time = timestamp.toDate()
            val month = cal.get(java.util.Calendar.MONTH) + 1
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val year = cal.get(java.util.Calendar.YEAR)
            return "$month/$day/$year"
        }
    }

    // ----------- DiffCallback for ListAdapter -----------
    class DiffCallback : DiffUtil.ItemCallback<SemesterRow>() {
        override fun areItemsTheSame(oldItem: SemesterRow, newItem: SemesterRow): Boolean {
            return when {
                oldItem is SemesterRow.YearHeader && newItem is SemesterRow.YearHeader ->
                    oldItem.year == newItem.year
                oldItem is SemesterRow.SemesterItem && newItem is SemesterRow.SemesterItem ->
                    oldItem.semester.id == newItem.semester.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SemesterRow, newItem: SemesterRow): Boolean {
            return oldItem == newItem
        }
    }
}

