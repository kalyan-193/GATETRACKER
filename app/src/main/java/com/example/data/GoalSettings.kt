package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_settings")
data class GoalSettings(
    @PrimaryKey val id: Int = 1, // Singleton row configuration
    val targetAirRank: Int = 100,
    val targetScore: Int = 85,
    val targetDailyHours: Double = 6.0,
    val monthlyGoals: String = "Complete electrical machines and practice PYQs."
)
