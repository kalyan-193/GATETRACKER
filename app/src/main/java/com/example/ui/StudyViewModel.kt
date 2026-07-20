package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import com.example.util.ExcelCsvHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = StudyRepository(db.studyDao())

    // UI Tab / Navigation State
    private val _activeTab = MutableStateFlow("Dashboard")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    // Flows from database
    val allEntries: StateFlow<List<DailyStudyEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectProgress: StateFlow<List<SubjectProgress>> = repository.subjectProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalSettings: StateFlow<GoalSettings> = repository.goalSettings
        .map { it ?: GoalSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalSettings())

    // Calculated / UI Derived states
    private val _currentGps = MutableStateFlow(50)
    val currentGps: StateFlow<Int> = _currentGps.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _insights = MutableStateFlow<List<String>>(emptyList())
    val insights: StateFlow<List<String>> = _insights.asStateFlow()

    private val _isInsightsLoading = MutableStateFlow(false)
    val isInsightsLoading: StateFlow<Boolean> = _isInsightsLoading.asStateFlow()

    // Pomodoro Timer State
    private val _pomodoroSecondsRemaining = MutableStateFlow(25 * 60)
    val pomodoroSecondsRemaining: StateFlow<Int> = _pomodoroSecondsRemaining.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _pomodoroMode = MutableStateFlow("Work") // "Work" or "Break"
    val pomodoroMode: StateFlow<String> = _pomodoroMode.asStateFlow()

    private var pomodoroJob: Job? = null

    // Mock Exam Timer State
    private val _mockExamSecondsRemaining = MutableStateFlow(3 * 3600) // 3 hours
    val mockExamSecondsRemaining: StateFlow<Int> = _mockExamSecondsRemaining.asStateFlow()

    private val _isMockExamRunning = MutableStateFlow(false)
    val isMockExamRunning: StateFlow<Boolean> = _isMockExamRunning.asStateFlow()

    private val _mockExamStatus = MutableStateFlow("NotStarted") // "NotStarted", "Running", "Finished"
    val mockExamStatus: StateFlow<String> = _mockExamStatus.asStateFlow()

    private var mockExamJob: Job? = null

    // Mock feedback variables
    var mockQuestionsAttempted = MutableStateFlow("0")
    var mockCorrectAnswers = MutableStateFlow("0")
    var mockScore = MutableStateFlow("0.0")
    var mockRemarks = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Check and populate subjects if database is fresh
            repository.checkAndPrepopulateSubjects()
            // Recalculate streak and GPS when entries change
            allEntries.collect { entries ->
                recalculateStreakAndGps(entries)
                generateInsights()
            }
        }
    }

    // Streak & GPS Calculation
    private fun recalculateStreakAndGps(entries: List<DailyStudyEntry>) {
        if (entries.isEmpty()) {
            _streak.value = 0
            _currentGps.value = 50
            return
        }

        // Sort by date descending
        val sortedEntries = entries.sortedByDescending { it.date }
        val todayStr = getTodayDateStr()

        // 1. Calculate Streak
        var currentStreak = 0
        var expectedDateMillis = parseDate(todayStr)
        var hasActiveStreak = false

        // Check if latest entry was today or yesterday to continue streak
        val latestEntryDateMillis = parseDate(sortedEntries[0].date)
        val diffToToday = expectedDateMillis - latestEntryDateMillis
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (diffToToday <= oneDayMillis) {
            hasActiveStreak = true
            expectedDateMillis = latestEntryDateMillis
        }

        if (hasActiveStreak) {
            for (entry in sortedEntries) {
                val entryMillis = parseDate(entry.date)
                val diff = expectedDateMillis - entryMillis
                // Allow a tiny margin of 2 hours for DST differences
                if (diff < 0L) continue // Skip entries on same day (should be unique anyway)
                if (diff <= oneDayMillis + 2 * 60 * 60 * 1000L) {
                    if (entry.studyHours > 0.0) {
                        currentStreak++
                        expectedDateMillis = entryMillis
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
        }
        _streak.value = currentStreak

        // 2. Calculate GPS Score (0-100, Base 50)
        var score = 50

        // Latest entry contributions
        val latest = sortedEntries[0]
        
        // Study Hours
        val hours = latest.studyHours
        score += when {
            hours < 2.0 -> -10
            hours in 2.0..4.0 -> -5
            hours in 4.0..6.0 -> 0
            hours in 6.0..8.0 -> 5
            hours in 8.0..10.0 -> 8
            else -> 10
        }

        // Revision Bonus
        if (latest.revisionHours >= 1.0) {
            score += 2
        }

        // PYQ Bonus
        if (latest.pyqsSolved > 20) {
            score += 3
        }

        // Mock Test Bonus
        if (latest.mockTestsAttempted > 0) {
            score += 5
        }

        // Notes Bonus
        if (latest.notesPrepared) {
            score += 2
        }

        // Productivity Bonus
        if (latest.productivity >= 8) {
            score += 3
        }

        // Streak Bonus
        score += when {
            currentStreak >= 100 -> 50
            currentStreak >= 30 -> 20
            currentStreak >= 7 -> 5
            else -> 0
        }

        // Penalties
        // A. No study entry for yesterday if today is not logged
        if (diffToToday > oneDayMillis) {
            score -= 8
        }

        // B. Two consecutive low-effort days (<4 hours)
        if (sortedEntries.size >= 2) {
            val e1 = sortedEntries[0]
            val e2 = sortedEntries[1]
            if (e1.studyHours < 4.0 && e2.studyHours < 4.0) {
                score -= 10
            }
        }

        // C. Missing revision for 7 consecutive days
        if (sortedEntries.size >= 7) {
            var hasRevisionIn7Days = false
            for (i in 0 until minOf(sortedEntries.size, 7)) {
                if (sortedEntries[i].revisionHours > 0.0) {
                    hasRevisionIn7Days = true
                    break
                }
            }
            if (!hasRevisionIn7Days) {
                score -= 5
            }
        }

        // Constrain [0, 100]
        _currentGps.value = score.coerceIn(0, 100)
    }

    // AI suggestion handler
    fun generateInsights() {
        viewModelScope.launch {
            _isInsightsLoading.value = true
            val generated = GeminiClient.getInsights(allEntries.value, subjectProgress.value)
            _insights.value = generated
            _isInsightsLoading.value = false
        }
    }

    // Daily Entry Interactions
    fun addDailyEntry(
        date: String,
        studyHours: Double,
        subjects: List<String>,
        topics: String,
        questions: Int,
        pyqs: Int,
        mocks: Int,
        revision: Double,
        videoHours: Double,
        notesPrepared: Boolean,
        confidence: Int,
        productivity: Int,
        remarks: String,
        sessionType: String = "Preparation"
    ) {
        viewModelScope.launch {
            val gps = calculateSingleEntryGps(studyHours, revision, pyqs, mocks, notesPrepared, productivity)
            val entry = DailyStudyEntry(
                date = date,
                studyHours = studyHours,
                subjectsStudied = subjects.joinToString(", "),
                topicsCompleted = topics,
                questionsSolved = questions,
                pyqsSolved = pyqs,
                mockTestsAttempted = mocks,
                revisionHours = revision,
                videoLectureHours = videoHours,
                notesPrepared = notesPrepared,
                confidence = confidence,
                productivity = productivity,
                remarks = remarks,
                gpsScore = gps,
                sessionType = sessionType
            )
            repository.insertEntry(entry)

            // Auto-update subject progress database counters dynamically if relevant
            if (subjects.isNotEmpty()) {
                val currentSubjects = subjectProgress.value
                for (subName in subjects) {
                    val match = currentSubjects.find { it.subjectName == subName }
                    if (match != null) {
                        val newPyqs = match.pyqsDone + pyqs
                        val newMocks = match.mockTestsDone + mocks
                        // Incrementally add 2% progress for completing a log with topics or hours studied
                        val increment = if (studyHours > 2.0) 5 else 2
                        val newProgress = (match.completionPercentage + increment).coerceIn(0, 100)
                        repository.updateSubjectProgress(
                            match.copy(
                                completionPercentage = newProgress,
                                pyqsDone = newPyqs,
                                mockTestsDone = newMocks,
                                notesCompleted = match.notesCompleted || notesPrepared
                            )
                        )
                    }
                }
            }
        }
    }

    private fun calculateSingleEntryGps(
        hours: Double,
        revision: Double,
        pyqs: Int,
        mocks: Int,
        notes: Boolean,
        productivity: Int
    ): Int {
        var base = 50
        base += when {
            hours < 2.0 -> -10
            hours in 2.0..4.0 -> -5
            hours in 4.0..6.0 -> 0
            hours in 6.0..8.0 -> 5
            hours in 8.0..10.0 -> 8
            else -> 10
        }
        if (revision >= 1.0) base += 2
        if (pyqs > 20) base += 3
        if (mocks > 0) base += 5
        if (notes) base += 2
        if (productivity >= 8) base += 3
        return base.coerceIn(0, 100)
    }

    fun deleteEntry(entry: DailyStudyEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    // Subject progress interactions
    fun updateSubjectManualProgress(subjectName: String, percentage: Int) {
        viewModelScope.launch {
            val current = subjectProgress.value.find { it.subjectName == subjectName }
            if (current != null) {
                repository.updateSubjectProgress(current.copy(completionPercentage = percentage))
            }
        }
    }

    // Goal settings interactions
    fun updateGoalSettings(rank: Int, score: Int, hours: Double, monthly: String) {
        viewModelScope.launch {
            repository.saveGoalSettings(GoalSettings(id = 1, targetAirRank = rank, targetScore = score, targetDailyHours = hours, monthlyGoals = monthly))
        }
    }

    // Backup & Excel Sync Helpers
    fun exportToCsvContent(): String {
        return ExcelCsvHelper.exportToCsv(allEntries.value)
    }

    fun importFromCsvContent(content: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val list = ExcelCsvHelper.importFromCsv(content)
                if (list.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        list.forEach { entry ->
                            // Check if date entry exists, insert or replace
                            repository.insertEntry(entry)
                        }
                    }
                    onSuccess()
                } else {
                    onFailure("No valid rows parsed. Ensure matching columns are present.")
                }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Import failed.")
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllEntries()
        }
    }

    // Pomodoro logic
    fun togglePomodoro() {
        if (_isPomodoroRunning.value) {
            pomodoroJob?.cancel()
            _isPomodoroRunning.value = false
        } else {
            _isPomodoroRunning.value = true
            pomodoroJob = viewModelScope.launch {
                while (_pomodoroSecondsRemaining.value > 0) {
                    delay(1000)
                    _pomodoroSecondsRemaining.value--
                }
                // Toggle mode when timer hits zero
                if (_pomodoroMode.value == "Work") {
                    _pomodoroMode.value = "Break"
                    _pomodoroSecondsRemaining.value = 5 * 60
                } else {
                    _pomodoroMode.value = "Work"
                    _pomodoroSecondsRemaining.value = 25 * 60
                }
                _isPomodoroRunning.value = false
            }
        }
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        _isPomodoroRunning.value = false
        _pomodoroMode.value = "Work"
        _pomodoroSecondsRemaining.value = 25 * 60
    }

    // Mock Exam timer logic
    fun startMockExam() {
        _mockExamStatus.value = "Running"
        _isMockExamRunning.value = true
        _mockExamSecondsRemaining.value = 3 * 3600
        mockExamJob = viewModelScope.launch {
            while (_mockExamSecondsRemaining.value > 0 && _mockExamStatus.value == "Running") {
                delay(1000)
                _mockExamSecondsRemaining.value--
            }
            if (_mockExamSecondsRemaining.value == 0) {
                finishMockExam()
            }
        }
    }

    fun finishMockExam() {
        mockExamJob?.cancel()
        _isMockExamRunning.value = false
        _mockExamStatus.value = "Finished"
    }

    fun resetMockExam() {
        mockExamJob?.cancel()
        _isMockExamRunning.value = false
        _mockExamStatus.value = "NotStarted"
        _mockExamSecondsRemaining.value = 3 * 3600
        mockQuestionsAttempted.value = "0"
        mockCorrectAnswers.value = "0"
        mockScore.value = "0.0"
        mockRemarks.value = ""
    }

    fun submitMockFeedback(questions: Int, correct: Int, score: Double, remarks: String) {
        viewModelScope.launch {
            // Also log a mock exam in current stats!
            val todayStr = getTodayDateStr()
            addDailyEntry(
                date = todayStr,
                studyHours = 3.0,
                subjects = listOf("Power Systems"), // Default representation subject
                topics = "Full Length Mock Exam",
                questions = questions,
                pyqs = 0,
                mocks = 1,
                revision = 1.0,
                videoHours = 0.0,
                notesPrepared = false,
                confidence = 8,
                productivity = 9,
                remarks = "Mock Exam Result: $score/100. $remarks"
            )
            resetMockExam()
        }
    }

    // Utils
    fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
