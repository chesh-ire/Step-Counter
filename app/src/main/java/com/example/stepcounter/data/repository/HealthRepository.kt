package com.example.stepcounter.data.repository

import com.example.stepcounter.data.local.dao.*
import com.example.stepcounter.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HealthRepository(
    private val stepDao: StepDao,
    private val waterDao: WaterDao,
    private val calorieDao: CalorieDao,
    private val weightDao: WeightDao
) {
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // Steps
    fun getStepsForToday(): Flow<StepEntry?> = stepDao.getStepsForDate(getStartOfDay())

    suspend fun updateSteps(steps: Int) {
        stepDao.insertOrUpdateStep(StepEntry(getStartOfDay(), steps))
    }

    fun getAllSteps(): Flow<List<StepEntry>> = stepDao.getAllSteps()

    // Water
    fun getWaterForToday(): Flow<List<WaterEntry>> = waterDao.getWaterForDay(getStartOfDay(), getEndOfDay())

    suspend fun addWater(amountMl: Int) {
        waterDao.insertWater(WaterEntry(timestamp = System.currentTimeMillis(), amountMl = amountMl))
    }

    // Calories
    fun getCaloriesForToday(): Flow<List<CalorieEntry>> = calorieDao.getCaloriesForDay(getStartOfDay(), getEndOfDay())

    suspend fun addCalorie(calories: Int, type: CalorieType) {
        calorieDao.insertCalorie(CalorieEntry(timestamp = System.currentTimeMillis(), calories = calories, type = type))
    }

    // Weight
    fun getLatestWeight(): Flow<WeightEntry?> = weightDao.getLatestWeight()

    fun getAllWeightEntries(): Flow<List<WeightEntry>> = weightDao.getAllWeightEntries()

    suspend fun addWeight(weightKg: Double) {
        weightDao.insertWeight(WeightEntry(timestamp = System.currentTimeMillis(), weightKg = weightKg))
    }
}
