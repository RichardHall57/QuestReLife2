package com.example.questrelife.mdp

/** Define student levels */
enum class State(val label: String) {
    S0("No progress"),
    S1("Bronze Learner (GPA < 2.0)"),
    S2("Silver Student (2.0–3.0)"),
    S3("Gold Achiever (3.0–3.7)"),
    S4("Diamond Scholar (≥3.7)"),
    S5("Master Level (≥3.9)")
}

/** Action enum still kept if needed for future simulations */
enum class Action {
    SubmitAssignment,
    ImproveStudy,
    Retry,
    Idle
}

/** Academic MDP for calculating GPA and level */
class AcademicMDP {

    var state: State = State.S0
        private set
    var gpa: Double = 0.0
        private set
    private var assignmentsCompleted = 0
    private var totalGradeSum: Double = 0.0

    /** Step through a new grade (deterministic, no randomness) */
    fun stepWithGrade(grade: Float): State {
        val gpaValue = convertToGPA(grade)
        totalGradeSum += gpaValue
        assignmentsCompleted++
        gpa = totalGradeSum / assignmentsCompleted
        updateState()
        return state
    }

    /** Convert numeric grade to GPA */
    private fun convertToGPA(grade: Float): Double = when {
        grade >= 90 -> 4.0
        grade >= 80 -> 3.0
        grade >= 70 -> 2.0
        grade >= 60 -> 1.0
        else -> 0.0
    }

    /** Update level based on GPA */
    private fun updateState() {
        state = when {
            gpa >= 3.9 -> State.S5
            gpa >= 3.7 -> State.S4
            gpa >= 3.0 -> State.S3
            gpa >= 2.0 -> State.S2
            gpa > 0.0 -> State.S1
            else -> State.S0
        }
    }

    /** Reset all progress */
    fun reset() {
        state = State.S0
        gpa = 0.0
        assignmentsCompleted = 0
        totalGradeSum = 0.0
    }

    /** Check if top level reached */
    fun isTerminal(): Boolean = (state == State.S5)
}
