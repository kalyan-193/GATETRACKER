package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class StudyRepository(private val studyDao: StudyDao) {

    val allEntries: Flow<List<DailyStudyEntry>> = studyDao.getAllEntries()
    val goalSettings: Flow<GoalSettings?> = studyDao.getGoalSettings()
    val subjectProgress: Flow<List<SubjectProgress>> = studyDao.getSubjectProgress()

    suspend fun checkAndPrepopulateSubjects() = withContext(Dispatchers.IO) {
        val subjects = listOf(
            "Engineering Mathematics",
            "Network Theory",
            "Signals and Systems",
            "Control Systems",
            "Electrical Machines",
            "Power Systems",
            "Power Electronics",
            "Analog Electronics",
            "Digital Electronics",
            "Measurements",
            "Electromagnetic Fields"
        )
        // Fetch current subject progress lists (non-blocking firstOrNull is safe as we call within Dispatchers.IO)
        val existing = studyDao.getSubjectProgress().firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            val defaultList = subjects.map { SubjectProgress(it, 0) }
            studyDao.insertSubjectProgress(defaultList)
        }

        val existingGoal = studyDao.getGoalSettings().firstOrNull()
        if (existingGoal == null) {
            studyDao.saveGoalSettings(GoalSettings())
        }
    }

    suspend fun insertEntry(entry: DailyStudyEntry) = withContext(Dispatchers.IO) {
        studyDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: DailyStudyEntry) = withContext(Dispatchers.IO) {
        studyDao.deleteEntry(entry)
    }

    suspend fun updateSubjectProgress(progress: SubjectProgress) = withContext(Dispatchers.IO) {
        studyDao.updateSubjectProgress(progress)
    }

    suspend fun saveGoalSettings(settings: GoalSettings) = withContext(Dispatchers.IO) {
        studyDao.saveGoalSettings(settings)
    }

    suspend fun clearAllEntries() = withContext(Dispatchers.IO) {
        studyDao.deleteAllEntries()
    }
}
