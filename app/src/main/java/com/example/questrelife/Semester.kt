package com.example.questrelife

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Semester(
    val id: String = "",
    val name: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
) : Parcelable

data class ClassItem(
    val id: String,
    val name: String,
    val teacher: String
)

data class AssignmentItem(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: Timestamp = Timestamp.now(),
    val grade: Float = 0f
)


