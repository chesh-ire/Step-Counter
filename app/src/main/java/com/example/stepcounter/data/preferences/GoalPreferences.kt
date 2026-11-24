package com.example.stepcounter.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "goals")

data class UserGoals(
    val stepGoal: Int,
    val waterGoalMl: Int,
    val weightGoalKg: Double,
    val distanceGoalKm: Double = 8.0,
    val calorieGoal: Int = 500,
    val isDarkMode: Boolean = true
)

/** Stable identity used to compete with friends on the live leaderboard. */
data class FriendIdentity(
    val userId: String,
    val displayName: String,
    val friendCode: String
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
        val USER_ID = stringPreferencesKey("friend_user_id")
        val DISPLAY_NAME = stringPreferencesKey("friend_display_name")
        val FRIEND_CODE = stringPreferencesKey("friend_code")
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

    val friendIdentityFlow: Flow<FriendIdentity?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID] ?: return@map null
        FriendIdentity(
            userId = id,
            displayName = preferences[DISPLAY_NAME] ?: "Me",
            friendCode = preferences[FRIEND_CODE] ?: ""
        )
    }

    /** Returns the stable identity, generating (and persisting) one on first use. */
    suspend fun ensureFriendIdentity(): FriendIdentity {
        val existing = friendIdentityFlow.first()
        if (existing != null && existing.friendCode.isNotEmpty()) return existing

        val newId = UUID.randomUUID().toString()
        val newCode = generateFriendCode()
        context.dataStore.edit { preferences ->
            if (preferences[USER_ID] == null) preferences[USER_ID] = newId
            if (preferences[FRIEND_CODE] == null) preferences[FRIEND_CODE] = newCode
            if (preferences[DISPLAY_NAME] == null) preferences[DISPLAY_NAME] = "Me"
        }
        return friendIdentityFlow.first() ?: FriendIdentity(newId, "Me", newCode)
    }

    suspend fun updateDisplayName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[DISPLAY_NAME] = name.trim().ifEmpty { "Me" }
        }
    }

    private fun generateFriendCode(): String {
        // Avoid ambiguous chars (0/O, 1/I) for easy sharing.
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return (0 until 6).joinToString("") { i ->
            val chunk = uuid.substring(i * 2, i * 2 + 2)
            alphabet[chunk.toInt(16) % alphabet.length].toString()
        }
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
