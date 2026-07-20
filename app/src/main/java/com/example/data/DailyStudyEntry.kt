package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_study_entries")
data class DailyStudyEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format: "yyyy-MM-dd"
    val studyHours: Double,
    val subjectsStudied: String, // Comma-separated list of subjects
    val topicsCompleted: String,
    val questionsSolved: Int,
    val pyqsSolved: Int,
    val mockTestsAttempted: Int,
    val revisionHours: Double,
    val videoLectureHours: Double,
    val notesPrepared: Boolean,
    val confidence: Int, // 1-10
    val productivity: Int, // 1-10
    val remarks: String,
    val gpsScore: Int, // Calculated and saved
    val sessionType: String = "Preparation"
)
