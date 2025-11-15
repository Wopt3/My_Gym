package com.example.my_gym_2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutProgressLogs(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated primary key
    val workoutName: String, // Regular field, can now have duplicates
    val weight: Double,
    val reps: Int,
    val date: String
)
