package com.example.stepcounter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stepcounter.data.local.entities.*
import com.example.stepcounter.data.preferences.FriendIdentity
import com.example.stepcounter.data.preferences.GoalPreferences
import com.example.stepcounter.data.preferences.UserGoals
import com.example.stepcounter.data.repository.AddFriendResult
import com.example.stepcounter.data.repository.FriendProfile
import com.example.stepcounter.data.repository.FriendsRepository
import com.example.stepcounter.data.repository.HealthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: HealthRepository,
    private val goalPreferences: GoalPreferences,
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    val userGoals = goalPreferences.userGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoals(10000, 2000, 70.0, 8.0, 500))

    val todaySteps = repository.getStepsForToday()
        .map { it?.steps ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayDistanceKm = todaySteps
        .map { steps -> (steps * 0.000762) } 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    val todayActiveMinutes = combine(
        repository.getAllWorkouts(),
        todaySteps
    ) { workouts, steps ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        val workoutMinutes = workouts.filter { it.startTime >= startOfDay }.sumOf { it.durationSeconds } / 60
        val walkingMinutes = (steps / 100).toLong() // 100 steps ~ 1 minute of activity
        
        workoutMinutes + walkingMinutes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Food Logging
    private val _foodSearchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodSearchResults: StateFlow<List<FoodItem>> = _foodSearchResults.asStateFlow()

    val todayFoodConsumption = repository.getFoodConsumptionToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Running / Workout State
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _runDurationSeconds = MutableStateFlow(0L)
    val runDurationSeconds: StateFlow<Long> = _runDurationSeconds.asStateFlow()

    private val _runDistanceKm = MutableStateFlow(0.0)
    val runDistanceKm: StateFlow<Double> = _runDistanceKm.asStateFlow()

    private val _runSteps = MutableStateFlow(0)
    val runSteps: StateFlow<Int> = _runSteps.asStateFlow()

    private val _lastWorkoutSummary = MutableStateFlow<Workout?>(null)
    val lastWorkoutSummary: StateFlow<Workout?> = _lastWorkoutSummary.asStateFlow()

    private var runTimerJob: Job? = null
    private var runBaselineSteps = -1

    // ---------------- Friends / Live Leaderboard ----------------

    val friendsAvailable: Boolean get() = friendsRepository.isAvailable

    private val _friendIdentity = MutableStateFlow<FriendIdentity?>(null)
    val friendIdentity: StateFlow<FriendIdentity?> = _friendIdentity.asStateFlow()

    private val _addFriendResult = MutableStateFlow<AddFriendResult?>(null)
    val addFriendResult: StateFlow<AddFriendResult?> = _addFriendResult.asStateFlow()

    /** Raw leaderboard streamed from Firebase for me + my friends. */
    private val remoteLeaderboard: StateFlow<List<FriendProfile>> = _friendIdentity
        .flatMapLatest { identity ->
            if (identity == null) flowOf(emptyList())
            else friendsRepository.observeLeaderboard(identity.userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Leaderboard shown in the UI. My own row is patched with my local live step count
     * so it moves instantly, without waiting for the Firebase round-trip.
     */
    val leaderboard: StateFlow<List<FriendProfile>> = combine(
        remoteLeaderboard, todaySteps, userGoals, _friendIdentity
    ) { remote, mySteps, goals, identity ->
        if (identity == null) return@combine remote
        val hasMe = remote.any { it.isMe }
        val patched = remote.map { profile ->
            if (profile.isMe) profile.copy(steps = maxOf(profile.steps, mySteps), name = identity.displayName)
            else profile
        }.toMutableList()
        if (!hasMe) {
            // Firebase hasn't echoed me back yet — show my row locally in the meantime.
            patched.add(
                FriendProfile(
                    id = identity.userId,
                    name = identity.displayName,
                    code = identity.friendCode,
                    steps = mySteps,
                    goal = goals.stepGoal,
                    lastUpdated = System.currentTimeMillis(),
                    isMe = true
                )
            )
        }
        patched.sortedByDescending { it.steps }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.preseedFoodDatabase()
        }

        // Establish identity, then keep my live steps + goal published to Firebase.
        viewModelScope.launch {
            _friendIdentity.value = goalPreferences.ensureFriendIdentity()
        }
        viewModelScope.launch {
            combine(
                _friendIdentity.filterNotNull(),
                todaySteps,
                userGoals.map { it.stepGoal }.distinctUntilChanged()
            ) { identity, steps, goal -> Triple(identity, steps, goal) }
                .collect { (identity, steps, goal) ->
                    friendsRepository.publishProfile(
                        id = identity.userId,
                        name = identity.displayName,
                        code = identity.friendCode,
                        steps = steps,
                        goal = goal
                    )
                }
        }
    }

    fun addFriend(code: String) {
        val identity = _friendIdentity.value ?: return
        viewModelScope.launch {
            _addFriendResult.value = friendsRepository.addFriendByCode(identity.userId, code)
        }
    }

    fun clearAddFriendResult() {
        _addFriendResult.value = null
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            goalPreferences.updateDisplayName(name)
            _friendIdentity.value = goalPreferences.ensureFriendIdentity()
        }
    }

    fun startRun() {
        if (_isRunning.value) return
        _isRunning.value = true
        _runDurationSeconds.value = 0
        _runDistanceKm.value = 0.0
        _runSteps.value = 0
        _lastWorkoutSummary.value = null
        runBaselineSteps = todaySteps.value
        
        runTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _runDurationSeconds.value += 1
                
                val currentSteps = todaySteps.value
                val stepsDuringRun = (currentSteps - runBaselineSteps).coerceAtLeast(0)
                _runSteps.value = stepsDuringRun
                _runDistanceKm.value = stepsDuringRun * 0.000762
            }
        }
    }

    fun stopRun() {
        if (!_isRunning.value) return
        _isRunning.value = false
        runTimerJob?.cancel()
        
        val workout = Workout(
            type = "Running",
            startTime = System.currentTimeMillis() - (_runDurationSeconds.value * 1000),
            durationSeconds = _runDurationSeconds.value,
            distanceKm = _runDistanceKm.value,
            steps = _runSteps.value,
            calories = (_runSteps.value * 0.05).toInt()
        )
        
        _lastWorkoutSummary.value = workout
        
        viewModelScope.launch {
            repository.saveWorkout(workout)
        }
    }

    fun dismissWorkoutSummary() {
        _lastWorkoutSummary.value = null
    }

    fun searchFood(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _foodSearchResults.value = repository.searchFood(query)
            } else {
                _foodSearchResults.value = emptyList()
            }
        }
    }

    fun logFood(foodItem: FoodItem, amountGrams: Int) {
        viewModelScope.launch {
            repository.logFoodConsumption(foodItem.name, amountGrams, foodItem.caloriesPer100g)
        }
    }

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

    fun updateDistanceGoal(goalKm: Double) {
        viewModelScope.launch { goalPreferences.updateDistanceGoal(goalKm) }
    }

    fun updateCalorieGoal(goal: Int) {
        viewModelScope.launch { goalPreferences.updateCalorieGoal(goal) }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch { goalPreferences.toggleDarkMode(isDark) }
    }

    class Factory(
        private val repository: HealthRepository,
        private val goalPreferences: GoalPreferences,
        private val friendsRepository: FriendsRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository, goalPreferences, friendsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
