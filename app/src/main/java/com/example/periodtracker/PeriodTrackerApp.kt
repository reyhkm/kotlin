package com.example.periodtracker

import android.app.Application
import com.example.periodtracker.data.database.AppDatabase

class PeriodTrackerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}