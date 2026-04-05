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
    val weightGoalKg: Double
)

class GoalPreferences(private val context: Context) {

    companion object {
        val STEP_GOAL = intPreferencesKey("step_goal")
        val WATER_GOAL = intPreferencesKey("water_goal")
        val WEIGHT_GOAL = doublePreferencesKey("weight_goal")
        val LAST_ACTIVITY_TIMESTAMP = longPreferencesKey("last_activity_timestamp")
    }

    val userGoalsFlow: Flow<UserGoals> = context.dataStore.data.map { preferences ->
        UserGoals(
            stepGoal = preferences[STEP_GOAL] ?: 10000,
            waterGoalMl = preferences[WATER_GOAL] ?: 2000,
            weightGoalKg = preferences[WEIGHT_GOAL] ?: 70.0
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
}
