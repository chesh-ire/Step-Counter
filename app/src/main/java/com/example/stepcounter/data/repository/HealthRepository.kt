package com.example.stepcounter.data.repository

import com.example.stepcounter.data.local.dao.*
import com.example.stepcounter.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HealthRepository(
    private val stepDao: StepDao,
    private val waterDao: WaterDao,
    private val calorieDao: CalorieDao,
    private val weightDao: WeightDao,
    private val foodDao: FoodDao
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

    private fun getStartOfHour(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Steps
    fun getStepsForToday(): Flow<StepEntry?> = stepDao.getStepsForDate(getStartOfDay())

    suspend fun updateSteps(steps: Int) {
        stepDao.insertOrUpdateStep(StepEntry(getStartOfDay(), steps))
    }

    fun getAllSteps(): Flow<List<StepEntry>> = stepDao.getAllSteps()

    // Hourly Steps
    fun getHourlyStepsForToday(): Flow<List<HourlyStep>> = 
        stepDao.getHourlyStepsForDay(getStartOfDay(), getEndOfDay())

    suspend fun updateHourlySteps(stepsInHour: Int) {
        stepDao.insertHourlyStep(HourlyStep(hourTimestamp = getStartOfHour(), steps = stepsInHour))
    }

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

    // Food
    suspend fun searchFood(query: String): List<FoodItem> = foodDao.searchFood(query)

    suspend fun logFoodConsumption(foodName: String, amountGrams: Int, caloriesPer100g: Int) {
        val totalCalories = (amountGrams.toDouble() / 100.0 * caloriesPer100g).toInt()
        val consumption = FoodConsumption(
            foodName = foodName,
            calories = totalCalories,
            amountGrams = amountGrams,
            timestamp = System.currentTimeMillis()
        )
        foodDao.insertFoodConsumption(consumption)
        // Also add to daily calories consumed
        addCalorie(totalCalories, CalorieType.CONSUMED)
    }

    fun getFoodConsumptionToday(): Flow<List<FoodConsumption>> = 
        foodDao.getFoodConsumptionForDay(getStartOfDay(), getEndOfDay())

    suspend fun preseedFoodDatabase() {
        val items = listOf(
            FoodItem(name = "Apple", caloriesPer100g = 52),
            FoodItem(name = "Banana", caloriesPer100g = 89),
            FoodItem(name = "Chicken Breast", caloriesPer100g = 165),
            FoodItem(name = "Rice", caloriesPer100g = 130),
            FoodItem(name = "Egg", caloriesPer100g = 155),
            FoodItem(name = "Milk", caloriesPer100g = 42),
            FoodItem(name = "Bread", caloriesPer100g = 265),
            FoodItem(name = "Pizza", caloriesPer100g = 266),
            FoodItem(name = "Salad", caloriesPer100g = 15),
            FoodItem(name = "Potato", caloriesPer100g = 77)
        )
        items.forEach { foodDao.insertFoodItem(it) }
    }
}
