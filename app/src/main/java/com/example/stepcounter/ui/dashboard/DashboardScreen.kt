package com.example.stepcounter.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onLogWater: () -> Unit,
    onLogCalories: () -> Unit,
    onLogWeight: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onSetGoals: () -> Unit
) {
    val steps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val water by viewModel.todayWater.collectAsStateWithLifecycle()
    val caloriesConsumed by viewModel.todayCaloriesConsumed.collectAsStateWithLifecycle()
    val caloriesBurned by viewModel.todayCaloriesBurned.collectAsStateWithLifecycle()
    val weight by viewModel.latestWeight.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("StepCounter", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSetGoals) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Rounded.Analytics, contentDescription = "Analytics")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(8.dp)) }

            // Main Steps Card - Full Width
            item(span = { GridItemSpan(2) }) {
                BentoCard(
                    title = "Steps",
                    value = steps.toString(),
                    unit = "steps",
                    goalValue = userGoals.stepGoal.toString(),
                    progress = if (userGoals.stepGoal > 0) steps.toFloat() / userGoals.stepGoal else 0f,
                    icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Calories Card
            item {
                BentoCard(
                    title = "Calories",
                    value = (caloriesConsumed - caloriesBurned).toString(),
                    unit = "net kcal",
                    icon = Icons.Rounded.LocalFireDepartment,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onLogCalories
                )
            }

            // Water Card
            item {
                BentoCard(
                    title = "Water",
                    value = water.toString(),
                    unit = "ml",
                    goalValue = userGoals.waterGoalMl.toString(),
                    progress = if (userGoals.waterGoalMl > 0) water.toFloat() / userGoals.waterGoalMl else 0f,
                    icon = Icons.Rounded.WaterDrop,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onLogWater
                )
            }

            // Weight Card
            item(span = { GridItemSpan(2) }) {
                BentoCard(
                    title = "Weight",
                    value = if (weight > 0) "%.1f".format(weight) else "--",
                    unit = "kg",
                    goalValue = userGoals.weightGoalKg.toString(),
                    icon = Icons.Rounded.MonitorWeight,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onLogWeight
                )
            }

            // Analytics Shortcut Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    onClick = onNavigateToAnalytics,
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Timeline, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("View Progress Analytics", fontWeight = FontWeight.Bold)
                            Text("Insights and trends based on your data", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            
            item(span = { GridItemSpan(2) }) { 
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoCard(
    title: String,
    value: String,
    unit: String,
    goalValue: String? = null,
    progress: Float? = null,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (goalValue != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Goal: $goalValue",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            if (progress != null) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}
