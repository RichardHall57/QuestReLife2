package com.example.questrelife

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.lang.Exception

/**
 * Adds a new Semester to Firestore.
 * Returns the Firestore-generated ID via onSuccess, or error via onFailure.
 */
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
        .addOnSuccessListener { docRef ->
            onSuccess(docRef.id)
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

/**
 * Adds a Class linked to a Semester.
 */
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
        "semesterId" to semesterId
    )

    db.collection("Classes")
        .add(classMap)
        .addOnSuccessListener { docRef ->
            onSuccess(docRef.id)
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun fetchClassesForSemester(
    db: FirebaseFirestore,
    semesterId: String,
    limit: Long = 50,
    onSuccess: (List<ClassItem>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    db.collection("Classes")
        .whereEqualTo("semesterId", semesterId)
        .limit(limit)
        .get()
        .addOnSuccessListener { snapshot ->
            val classes = snapshot.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val teacher = doc.getString("teacher") ?: ""
                ClassItem(
                    id = doc.id,
                    name = name,
                    teacher = teacher
                )
            }
            onSuccess(classes)
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

suspend fun fetchClassesForSemesterSuspend(
    db: FirebaseFirestore,
    semesterId: String,
    limit: Long = 50
): List<ClassItem> {
    return try {
        val snapshot: QuerySnapshot = db.collection("Classes")
            .whereEqualTo("semesterId", semesterId)
            .limit(limit)
            .get()
            .await()

        snapshot.mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null
            val teacher = doc.getString("teacher") ?: ""
            ClassItem(
                id = doc.id,
                name = name,
                teacher = teacher
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}
