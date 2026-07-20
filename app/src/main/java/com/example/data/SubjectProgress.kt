package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_progress")
data class SubjectProgress(
    @PrimaryKey val subjectName: String,
    val completionPercentage: Int, // 0 to 100
    val notesCompleted: Boolean = false,
    val pyqsDone: Int = 0,
    val mockTestsDone: Int = 0
)
