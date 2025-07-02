package com.example.periodtracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.periodtracker.data.model.Cycle
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: Cycle)

    @Update
    suspend fun update(cycle: Cycle)

    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    fun getAllCycles(): Flow<List<Cycle>>

    @Query("SELECT * FROM cycles ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestCycle(): Cycle?
}