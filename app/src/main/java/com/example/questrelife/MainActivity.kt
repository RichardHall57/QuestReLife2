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

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Enable Firebase App Check in debug mode
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

        // Example login (replace with real credentials)
        loginUser("richardhall157@gmail.com", "REAL_PASSWORD_HERE")
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_guild -> HomeFragment()
                R.id.nav_adventure -> CalendarFragment()
                R.id.nav_quests -> SemesterFragment()
                R.id.nav_archive -> {
                    val classId = "2HFcXxT1jN3oIgbknRF2"
                    val className = "Senior Project"
                    GradeCalculationFragment.newInstance(classId, className)
                }
                R.id.nav_hero -> ProfileFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            // Log which fragment is being shown
            Log.d("QuestReLife", "Current Fragment: ${fragment::class.java.simpleName}")

            true
        }
    }

    /** Login user and log success or failure */
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    Log.d("MainActivity", "Logged in user: $userId")
                } else {
                    val errorMsg = task.exception?.localizedMessage
                    Log.e("MainActivity", "Authentication failed: $errorMsg")
                    task.exception?.printStackTrace()
                }
            }
    }
}
