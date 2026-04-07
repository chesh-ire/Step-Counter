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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyStep(hourlyStep: HourlyStep)

    @Query("SELECT * FROM hourly_steps WHERE hourTimestamp >= :startOfDay AND hourTimestamp <= :endOfDay ORDER BY hourTimestamp ASC")
    fun getHourlyStepsForDay(startOfDay: Long, endOfDay: Long): Flow<List<HourlyStep>>
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

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%'")
    suspend fun searchFood(query: String): List<FoodItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem)

    @Insert
    suspend fun insertFoodConsumption(consumption: FoodConsumption)

    @Query("SELECT * FROM food_consumption WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getFoodConsumptionForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodConsumption>>
}
