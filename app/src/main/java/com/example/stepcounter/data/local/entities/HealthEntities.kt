package com.example.stepcounter.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_entries")
data class StepEntry(
    @PrimaryKey val date: Long, // Start of day in milliseconds
    val steps: Int
)

@Entity(tableName = "hourly_steps")
data class HourlyStep(
    @PrimaryKey val hourTimestamp: Long, // Start of the hour
    val steps: Int
)

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val amountMl: Int
)

@Entity(tableName = "calorie_entries")
data class CalorieEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val calories: Int,
    val type: CalorieType
)

enum class CalorieType {
    CONSUMED, BURNED
}

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val weightKg: Double
)

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey val name: String,
    val caloriesPer100g: Int
)

@Entity(tableName = "food_consumption")
data class FoodConsumption(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodName: String,
    val calories: Int,
    val amountGrams: Int,
    val timestamp: Long
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // e.g., "Running"
    val startTime: Long,
    val durationSeconds: Long,
    val distanceKm: Double,
    val steps: Int,
    val calories: Int
)
