package com.example.my_gym_2.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "gym-db"
            )
                .fallbackToDestructiveMigration()   // 👈 add this
                .build().also { INSTANCE = it }
        }
    }
}
