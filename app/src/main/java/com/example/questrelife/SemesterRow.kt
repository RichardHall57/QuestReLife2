package com.example.questrelife

sealed class SemesterRow {
    data class YearHeader(val year: String) : SemesterRow()
    data class SemesterItem(val semester: Semester) : SemesterRow()
}
