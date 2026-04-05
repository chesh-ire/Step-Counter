package com.example.stepcounter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stepcounter.data.local.entities.CalorieType
import com.example.stepcounter.data.local.entities.StepEntry
import com.example.stepcounter.data.local.entities.WeightEntry
import com.example.stepcounter.data.preferences.GoalPreferences
import com.example.stepcounter.data.preferences.UserGoals
import com.example.stepcounter.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: HealthRepository,
    private val goalPreferences: GoalPreferences
) : ViewModel() {

    val userGoals = goalPreferences.userGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoals(10000, 2000, 70.0))

    val todaySteps = repository.getStepsForToday()
        .map { it?.steps ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayWater = repository.getWaterForToday()
        .map { list -> list.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayCaloriesConsumed = repository.getCaloriesForToday()
        .map { list -> list.filter { it.type == CalorieType.CONSUMED }.sumOf { it.calories } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayCaloriesBurned = repository.getCaloriesForToday()
        .map { list -> list.filter { it.type == CalorieType.BURNED }.sumOf { it.calories } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val latestWeight = repository.getLatestWeight()
        .map { it?.weightKg ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Historical Data for Analytics
    val weightHistory = repository.getAllWeightEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepHistory = repository.getAllSteps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val insights = combine(todaySteps, todayWater, latestWeight, userGoals) { steps, water, weight, goals ->
        val list = mutableListOf<String>()
        val stepProgress = if (goals.stepGoal > 0) (steps.toFloat() / goals.stepGoal) * 100 else 0f
        
        if (stepProgress < 50) {
            list.add("You're less than halfway to your step goal! Let's get moving.")
        } else if (stepProgress < 100) {
            val remaining = (goals.stepGoal - steps).coerceAtLeast(0)
            list.add("Only $remaining steps left to reach your goal! You can do it.")
        } else {
            list.add("Goal smashed! You've exceeded your daily step target.")
        }
        
        if (water < goals.waterGoalMl) {
            val remainingMl = goals.waterGoalMl - water
            list.add("Drink $remainingMl ml more water to stay hydrated.")
        } else {
            list.add("Great job! You've met your hydration goal.")
        }
        
        if (weight > 0 && goals.weightGoalKg > 0) {
            val diff = weight - goals.weightGoalKg
            if (diff > 0) {
                list.add("You are ${"%.1f".format(diff)} kg away from your target weight.")
            } else if (diff < 0) {
                list.add("Target weight achieved! Maintaining your health is key.")
            }
        }
        
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWater(amountMl: Int) {
        viewModelScope.launch { repository.addWater(amountMl) }
    }

    fun addCalorie(calories: Int, type: CalorieType) {
        viewModelScope.launch { repository.addCalorie(calories, type) }
    }

    fun addWeight(weightKg: Double) {
        viewModelScope.launch { repository.addWeight(weightKg) }
    }

    fun updateStepGoal(goal: Int) {
        viewModelScope.launch { goalPreferences.updateStepGoal(goal) }
    }

    fun updateWaterGoal(goalMl: Int) {
        viewModelScope.launch { goalPreferences.updateWaterGoal(goalMl) }
    }

    fun updateWeightGoal(goalKg: Double) {
        viewModelScope.launch { goalPreferences.updateWeightGoal(goalKg) }
    }

    class Factory(
        private val repository: HealthRepository,
        private val goalPreferences: GoalPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository, goalPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
