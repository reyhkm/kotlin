package com.example.periodtracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.periodtracker.data.dao.CycleDao
import com.example.periodtracker.data.model.Cycle

@Database(entities = [Cycle::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cycleDao(): CycleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "period_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}