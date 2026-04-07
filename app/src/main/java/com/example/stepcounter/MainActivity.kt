package com.example.stepcounter

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stepcounter.service.StepCounterService
import com.example.stepcounter.ui.MainViewModel
import com.example.stepcounter.ui.analytics.AnalyticsScreen
import com.example.stepcounter.ui.dashboard.DashboardScreen
import com.example.stepcounter.ui.logging.FoodLoggingDialog
import com.example.stepcounter.ui.logging.WaterLoggingDialog
import com.example.stepcounter.ui.logging.WeightLoggingDialog
import com.example.stepcounter.ui.settings.GoalsScreen
import com.example.stepcounter.ui.theme.StepCounterTheme
import com.example.stepcounter.ui.water.WaterTrackerScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as StepCounterApp
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(app.repository, app.goalPreferences)
            )

            StepCounterTheme {
                MainNavigation(viewModel)
            }
        }
    }

    private fun startStepCounterService() {
        val intent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MainNavigation(viewModel: MainViewModel) {
        val navController = rememberNavController()
        
        var showWaterDialog by remember { mutableStateOf(false) }
        var showFoodDialog by remember { mutableStateOf(false) }
        var showWeightDialog by remember { mutableStateOf(false) }

        val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
        val foodSearchResults by viewModel.foodSearchResults.collectAsStateWithLifecycle()

        val activityRecognitionPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rememberPermissionState(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            null
        }

        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            null
        }

        LaunchedEffect(activityRecognitionPermission?.status, notificationPermission?.status) {
            if (activityRecognitionPermission?.status?.isGranted != false && 
                notificationPermission?.status?.isGranted != false) {
                startStepCounterService()
            } else {
                activityRecognitionPermission?.launchPermissionRequest()
                notificationPermission?.launchPermissionRequest()
            }
        }

        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToWater = { navController.navigate("water") },
                    onNavigateToAnalytics = { navController.navigate("stats") },
                    onSetGoals = { navController.navigate("goals") },
                    onNavigateToProfile = { /* TODO */ },
                    onLogFood = { showFoodDialog = true }
                )
            }
            composable("stats") {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    onNavigateToWater = { navController.navigate("water") },
                    onNavigateToGoals = { navController.navigate("goals") },
                    onNavigateToProfile = { /* TODO */ }
                )
            }
            composable("water") {
                WaterTrackerScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    onNavigateToStats = { navController.navigate("stats") },
                    onNavigateToGoals = { navController.navigate("goals") },
                    onNavigateToProfile = { /* TODO */ },
                    onLogWater = { showWaterDialog = true }
                )
            }
            composable("goals") {
                GoalsScreen(
                    currentGoals = userGoals,
                    onBack = { navController.popBackStack() },
                    onSave = { goals ->
                        viewModel.updateStepGoal(goals.stepGoal)
                        viewModel.updateWaterGoal(goals.waterGoalMl)
                        viewModel.updateWeightGoal(goals.weightGoalKg)
                        navController.popBackStack()
                    }
                )
            }
        }

        if (showWaterDialog) {
            WaterLoggingDialog(
                onDismiss = { showWaterDialog = false },
                onConfirm = { viewModel.addWater(it) }
            )
        }

        if (showFoodDialog) {
            FoodLoggingDialog(
                searchResults = foodSearchResults,
                onSearch = { viewModel.searchFood(it) },
                onLog = { food, amount -> viewModel.logFood(food, amount) },
                onDismiss = { showFoodDialog = false }
            )
        }

        if (showWeightDialog) {
            WeightLoggingDialog(
                onDismiss = { showWeightDialog = false },
                onConfirm = { viewModel.addWeight(it) }
            )
        }
    }
}
