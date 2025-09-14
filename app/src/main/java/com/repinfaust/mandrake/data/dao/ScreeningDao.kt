package com.repinfaust.mandrake.data.dao

import androidx.room.*
import com.repinfaust.mandrake.data.entity.ScreeningResult
import com.repinfaust.mandrake.data.entity.ScreeningType

@Dao
interface ScreeningDao {
    @Query("SELECT * FROM screening_results ORDER BY timestamp DESC")
    suspend fun getAllScreenings(): List<ScreeningResult>
    
    @Query("SELECT * FROM screening_results WHERE category = :category ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestScreeningForCategory(category: String): ScreeningResult?
    
    @Query("SELECT * FROM screening_results WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestScreeningByType(type: ScreeningType): ScreeningResult?
    
    @Query("SELECT * FROM screening_results WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getScreeningsSince(startTime: Long): List<ScreeningResult>
    
    @Insert
    suspend fun insertScreening(screening: ScreeningResult): Long
    
    @Update
    suspend fun updateScreening(screening: ScreeningResult)
    
    @Delete
    suspend fun deleteScreening(screening: ScreeningResult)
    
    @Query("DELETE FROM screening_results WHERE category = :category")
    suspend fun deleteScreeningsForCategory(category: String)
}