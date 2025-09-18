package com.example.questrelife

import com.google.firebase.firestore.FirebaseFirestore

fun addSemester(
    db: FirebaseFirestore,
    semester: Semester,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val semesterMap = hashMapOf(
        "name" to semester.name,
        "startDate" to semester.startDate,
        "endDate" to semester.endDate
    )
    db.collection("semesters")
        .add(semesterMap)
        .addOnSuccessListener { documentReference ->
            onSuccess(documentReference.id)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}


