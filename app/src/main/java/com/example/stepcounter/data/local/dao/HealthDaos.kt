package com.example.stepcounter.data.local.dao

import androidx.room.*
import com.example.stepcounter.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_entries WHERE date = :date")
    fun getStepsForDate(date: Long): Flow<StepEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStep(stepEntry: StepEntry)

    @Query("SELECT * FROM step_entries ORDER BY date DESC")
    fun getAllSteps(): Flow<List<StepEntry>>
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getWaterForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterEntry>>

    @Insert
    suspend fun insertWater(waterEntry: WaterEntry)

    @Delete
    suspend fun deleteWater(waterEntry: WaterEntry)
}

@Dao
interface CalorieDao {
    @Query("SELECT * FROM calorie_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getCaloriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<CalorieEntry>>

    @Insert
    suspend fun insertCalorie(calorieEntry: CalorieEntry)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Insert
    suspend fun insertWeight(weightEntry: WeightEntry)

    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightEntry?>
}
