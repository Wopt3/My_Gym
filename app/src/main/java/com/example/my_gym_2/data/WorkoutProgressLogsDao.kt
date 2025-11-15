package com.example.my_gym_2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutLogDao {

    @Insert
    suspend fun insertLog(log: WorkoutProgressLogs)

    @Query("SELECT * FROM workout_logs WHERE workoutName = :exercise AND date = :date LIMIT 1")
    suspend fun getLogByExerciseAndDate(exercise: String, date: String): WorkoutProgressLogs?
    @Query("DELETE FROM workout_logs WHERE workoutName = :exercise AND date = :date")
    suspend fun deleteLogByExerciseAndDate(exercise: String, date: String)
    @Query("SELECT * FROM workout_logs WHERE workoutName = :name AND date = :date AND weight = :weight AND reps = :reps LIMIT 1")
    suspend fun getLogByDetails(name: String, date: String, weight: Double, reps: Int): WorkoutProgressLogs?
    //@Query("SELECT * FROM workout_logs WHERE workoutName = :name AND date = :date AND weight = :weight AND reps = :reps")
    //suspend fun deleteLogByDetails(name: String, date: String, weight: Double, reps: Int)

    @Query("SELECT DISTINCT workoutName FROM workout_logs ORDER BY workoutName ASC")
    suspend fun getAllExerciseNames(): List<String>


    @Query("SELECT * FROM workout_logs WHERE workoutName = :name ORDER BY date DESC")
    suspend fun getLogsForExercise(name: String): List<WorkoutProgressLogs>

    @Query("SELECT MAX(weight) FROM workout_logs WHERE workoutName = :name")
    suspend fun getMaxWeight(name: String): Double?

    // Estimated 1RM = weight * (1 + reps/30.0)
    @Query("""
        SELECT MAX(weight / (1.0278 - 0.0278 * reps))
        FROM workout_logs 
        WHERE workoutName = :name
    """)
    suspend fun getMaxOneRepMax(name: String): Double?

    @Query("SELECT * FROM workout_logs ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 20): List<WorkoutProgressLogs>

    @Query("""
        SELECT * FROM workout_logs 
        WHERE workoutName = :name 
        ORDER BY date DESC LIMIT 1
    """)
    suspend fun getLastLogForExercise(name: String): WorkoutProgressLogs?

}

