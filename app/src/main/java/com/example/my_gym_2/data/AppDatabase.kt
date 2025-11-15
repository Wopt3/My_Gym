package com.example.my_gym_2.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutProgressLogs::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutLogDao(): WorkoutLogDao
}
