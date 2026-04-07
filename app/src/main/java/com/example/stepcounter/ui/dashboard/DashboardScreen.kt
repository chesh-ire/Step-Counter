package com.example.stepcounter.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.ui.MainViewModel
import com.example.stepcounter.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToWater: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onSetGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogFood: () -> Unit
) {
    val steps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val distanceKm by viewModel.todayDistanceKm.collectAsStateWithLifecycle()
    val caloriesConsumed by viewModel.todayCaloriesConsumed.collectAsStateWithLifecycle()
    val caloriesBurned by viewModel.todayCaloriesBurned.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()

    val netCalories = (caloriesConsumed - caloriesBurned)

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Steps Tracker", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onSetGoals) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onHome = {},
                onStats = onNavigateToAnalytics,
                onWater = onNavigateToWater,
                onGoals = onSetGoals,
                onProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Steps Gauge
            item {
                StepsGauge(steps = steps, goal = userGoals.stepGoal)
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Triple Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStatItem(String.format(Locale.getDefault(), "%.1f", distanceKm), "km", "Distance")
                    MiniStatItem(caloriesBurned.toString(), "kcal", "Burned")
                    MiniStatItem("45", "min", "Active Time")
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Calorie Intake Card
            item {
                CalorieIntakeCard(
                    consumed = caloriesConsumed,
                    net = netCalories,
                    onClick = onLogFood
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun StepsGauge(steps: Int, goal: Int) {
    val progress = if (goal > 0) steps.toFloat() / goal else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1500), label = ""
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(240.dp)) {
            drawArc(
                color = CardNavy,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    0f to AccentCyan,
                    0.5f to AccentBlue,
                    1f to AccentCyan
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.getDefault(), "%,d", steps),
                style = MaterialTheme.typography.displayMedium,
                color = TextWhite,
                fontWeight = FontWeight.Black
            )
            Text(text = "Steps", color = TextWhite, fontSize = 18.sp)
            Text(
                text = "Goal: ${String.format(Locale.getDefault(), "%,d", goal)}",
                color = AccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MiniStatItem(value: String, unit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = unit, color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}

@Composable
fun CalorieIntakeCard(consumed: Int, net: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Food Intake", color = TextGray, fontSize = 14.sp)
                Text(
                    text = "$consumed kcal",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Net: $net kcal",
                    color = if (net > 0) CalorieOrange else AccentCyan,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CalorieOrange.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = CalorieOrange)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onHome: () -> Unit,
    onStats: () -> Unit,
    onWater: () -> Unit,
    onGoals: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(
        containerColor = DeepNavy,
        contentColor = TextWhite,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onHome,
            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "stats",
            onClick = onStats,
            icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
            label = { Text("Stats", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "water",
            onClick = onWater,
            icon = { Icon(Icons.Rounded.WaterDrop, contentDescription = null) },
            label = { Text("Water", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "goals",
            onClick = onGoals,
            icon = { Icon(Icons.Rounded.EmojiEvents, contentDescription = null) },
            label = { Text("Goals", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfile,
            icon = { Icon(Icons.Rounded.Person, contentDescription = null) },
            label = { Text("Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
    }
}
