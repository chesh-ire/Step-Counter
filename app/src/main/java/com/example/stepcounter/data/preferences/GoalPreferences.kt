package com.example.stepcounter.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "goals")

data class UserGoals(
    val stepGoal: Int,
    val waterGoalMl: Int,
    val weightGoalKg: Double,
    val distanceGoalKm: Double = 8.0,
    val calorieGoal: Int = 500,
    val isDarkMode: Boolean = true
)

class GoalPreferences(private val context: Context) {

    companion object {
        val STEP_GOAL = intPreferencesKey("step_goal")
        val WATER_GOAL = intPreferencesKey("water_goal")
        val WEIGHT_GOAL = doublePreferencesKey("weight_goal")
        val DISTANCE_GOAL = doublePreferencesKey("distance_goal")
        val CALORIE_GOAL = intPreferencesKey("calorie_goal")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LAST_ACTIVITY_TIMESTAMP = longPreferencesKey("last_activity_timestamp")
    }

    val userGoalsFlow: Flow<UserGoals> = context.dataStore.data.map { preferences ->
        UserGoals(
            stepGoal = preferences[STEP_GOAL] ?: 10000,
            waterGoalMl = preferences[WATER_GOAL] ?: 2000,
            weightGoalKg = preferences[WEIGHT_GOAL] ?: 70.0,
            distanceGoalKm = preferences[DISTANCE_GOAL] ?: 8.0,
            calorieGoal = preferences[CALORIE_GOAL] ?: 500,
            isDarkMode = preferences[IS_DARK_MODE] ?: true
        )
    }

    val lastActivityTimestampFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_ACTIVITY_TIMESTAMP] ?: System.currentTimeMillis()
    }

    suspend fun updateLastActivityTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ACTIVITY_TIMESTAMP] = timestamp
        }
    }

    suspend fun updateStepGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[STEP_GOAL] = goal
        }
    }

    suspend fun updateWaterGoal(goalMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[WATER_GOAL] = goalMl
        }
    }

    suspend fun updateWeightGoal(goalKg: Double) {
        context.dataStore.edit { preferences ->
            preferences[WEIGHT_GOAL] = goalKg
        }
    }

    suspend fun updateDistanceGoal(goalKm: Double) {
        context.dataStore.edit { preferences ->
            preferences[DISTANCE_GOAL] = goalKm
        }
    }

    suspend fun updateCalorieGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[CALORIE_GOAL] = goal
        }
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
}
