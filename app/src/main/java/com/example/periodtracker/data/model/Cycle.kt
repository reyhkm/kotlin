package com.example.periodtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
data class Cycle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Long,
    val endDate: Long? = null
)