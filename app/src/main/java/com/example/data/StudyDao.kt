package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    // Daily Entries
    @Query("SELECT * FROM daily_study_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DailyStudyEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DailyStudyEntry)

    @Delete
    suspend fun deleteEntry(entry: DailyStudyEntry)

    @Query("SELECT * FROM daily_study_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): DailyStudyEntry?

    @Query("DELETE FROM daily_study_entries")
    suspend fun deleteAllEntries()

    // Subject Progress
    @Query("SELECT * FROM subject_progress")
    fun getSubjectProgress(): Flow<List<SubjectProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectProgress(progress: List<SubjectProgress>)

    @Update
    suspend fun updateSubjectProgress(progress: SubjectProgress)

    // Goal Settings
    @Query("SELECT * FROM goal_settings WHERE id = 1 LIMIT 1")
    fun getGoalSettings(): Flow<GoalSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoalSettings(settings: GoalSettings)
}
