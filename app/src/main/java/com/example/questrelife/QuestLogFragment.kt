package com.example.questrelife

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuestLogFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var levelText: TextView
    private lateinit var progressText: TextView

    private var currentLevel = 1
    private var currentProgress = 0 // 0–100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quest_log, container, false)

        levelText = view.findViewById(R.id.level_text)
        progressText = view.findViewById(R.id.progress_text)
        progressBar = view.findViewById(R.id.level_progress)

        updateUI()

        // Demo: Simulate leveling up automatically for now
        simulateAssignmentCompletion()

        return view
    }

    private fun simulateAssignmentCompletion() {
        // For now, simulate earning XP every 2 seconds
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                gainXP(25) // Each assignment = +25%
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun gainXP(amount: Int) {
        currentProgress += amount
        if (currentProgress >= 100) {
            currentProgress -= 100
            currentLevel++
        }
        updateUI()
    }

    private fun updateUI() {
        levelText.text = "Level $currentLevel"
        progressText.text = "Progress: $currentProgress%"
        progressBar.progress = currentProgress
    }
}
