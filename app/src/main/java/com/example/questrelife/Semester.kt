package com.example.questrelife

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Semester(
    val id: String = "",
    val name: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L
) : Parcelable

