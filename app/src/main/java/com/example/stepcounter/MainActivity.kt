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
import com.example.stepcounter.ui.logging.CalorieLoggingDialog
import com.example.stepcounter.ui.logging.WaterLoggingDialog
import com.example.stepcounter.ui.logging.WeightLoggingDialog
import com.example.stepcounter.ui.settings.GoalSettingDialog
import com.example.stepcounter.ui.theme.StepCounterTheme
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
        var showCalorieDialog by remember { mutableStateOf(false) }
        var showWeightDialog by remember { mutableStateOf(false) }
        var showGoalDialog by remember { mutableStateOf(false) }

        val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()

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

        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onLogWater = { showWaterDialog = true },
                    onLogCalories = { showCalorieDialog = true },
                    onLogWeight = { showWeightDialog = true },
                    onNavigateToAnalytics = { navController.navigate("analytics") },
                    onSetGoals = { showGoalDialog = true }
                )
            }
            composable("analytics") {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showWaterDialog) {
            WaterLoggingDialog(
                onDismiss = { showWaterDialog = false },
                onConfirm = { viewModel.addWater(it) }
            )
        }

        if (showCalorieDialog) {
            CalorieLoggingDialog(
                onDismiss = { showCalorieDialog = false },
                onConfirm = { amount, type -> viewModel.addCalorie(amount, type) }
            )
        }

        if (showWeightDialog) {
            WeightLoggingDialog(
                onDismiss = { showWeightDialog = false },
                onConfirm = { viewModel.addWeight(it) }
            )
        }

        if (showGoalDialog) {
            GoalSettingDialog(
                currentGoals = userGoals,
                onDismiss = { showGoalDialog = false },
                onConfirm = { goals ->
                    viewModel.updateStepGoal(goals.stepGoal)
                    viewModel.updateWaterGoal(goals.waterGoalMl)
                    viewModel.updateWeightGoal(goals.weightGoalKg)
                }
            )
        }
    }
}
