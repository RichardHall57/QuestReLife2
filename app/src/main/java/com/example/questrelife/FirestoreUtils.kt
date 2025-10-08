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

    db.collection("Semester")
        .add(semesterMap)
        .addOnSuccessListener { documentReference ->
            onSuccess(documentReference.id) // return Firestore-generated ID
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}  // <-- missing closing brace fixed here

fun addClass(
    db: FirebaseFirestore,
    classItem: ClassItem,
    semesterId: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val classMap = hashMapOf(
        "name" to classItem.name,
        "teacher" to classItem.teacher,
        "semesterId" to semesterId  // Link class to semester
    )

    db.collection("Classes")  // Your collection for classes
        .add(classMap)
        .addOnSuccessListener { documentReference ->
            onSuccess(documentReference.id)  // Firestore document ID of new class
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}
