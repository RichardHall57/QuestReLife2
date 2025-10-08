package com.example.questrelife

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val db = FirebaseFirestore.getInstance()
        Log.d("FirestoreCheck", "Connected to project: ${db.app.options.projectId}")


        // Enable App Check in debug mode (safe for development)
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_main)
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // Example: log in the user (replace with real credentials or use Auth UI)
        loginUser("richardhall157@gmail.com", "YOUR_PASSWORD")
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_guild -> HomeFragment()
                R.id.nav_adventure -> QuestLogFragment()
                R.id.nav_quests -> SemesterFragment()
                R.id.nav_archive -> GradeCalculationFragment()
                R.id.nav_hero -> ProfileFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        fetchSemesterDocument(userId)
                        fetchAllSemesters() // Using Query
                    }
                } else {
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun fetchSemesterDocument(documentId: String) {
        val docRef = db.collection("Semester").document(documentId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val semesterName = document.getString("name")
                    val semesterType = document.getString("type")
                    println("Semester Name: $semesterName, Type: $semesterType")
                } else {
                    println("No such Semester document")
                }
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    // ✅ Example of using Query
    private fun fetchAllSemesters() {
        val query: Query = db.collection("Semester").orderBy("startDate")
        query.get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val name = doc.getString("name")
                    val start = doc.getLong("startDate")
                    val end = doc.getLong("endDate")
                    println("Semester: $name, $start - $end")
                }
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }
}
