package com.example.stepcounter.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.ui.MainViewModel
import com.example.stepcounter.ui.dashboard.BottomNavigationBar
import com.example.stepcounter.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val weightHistory by viewModel.weightHistory.collectAsStateWithLifecycle()
    val stepHistory by viewModel.stepHistory.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    val latestWeight by viewModel.latestWeight.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Weight Progress", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "stats",
                onHome = onNavigateToHome,
                onStats = {},
                onWater = onNavigateToWater,
                onGoals = onNavigateToGoals,
                onProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Current Weight", color = TextGray, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (latestWeight > 0) String.format(Locale.getDefault(), "%.1f", latestWeight) else "--",
                                color = TextWhite,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " kg",
                                color = TextGray,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    Text(
                        text = "Goal: ${userGoals.weightGoalKg} kg",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Weight Chart Card
            item {
                WeightChartCard(weightHistory.map { it.weightKg.toFloat() }, userGoals.weightGoalKg.toFloat())
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Weight Trend", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Today", color = TextGray, fontSize = 14.sp)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Step Progress Chart
            item {
                StepHistoryChart(stepHistory.map { it.steps.toFloat() }, userGoals.stepGoal.toFloat())
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun WeightChartCard(data: List<Float>, goal: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data.size < 2) {
                    Text("Not enough data to show weight trend", color = TextGray, fontSize = 12.sp)
                } else {
                    AnalyticsLineChart(
                        data = data,
                        goalValue = goal,
                        color = AccentCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Today", color = TextGray, fontSize = 12.sp)
                Text("Week", color = TextGray, fontSize = 12.sp)
                Text("Month", color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StepHistoryChart(data: List<Float>, goal: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Step Progress", color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data.size < 2) {
                    Text("Recording your first steps...", color = TextGray, fontSize = 12.sp)
                } else {
                    AnalyticsLineChart(
                        data = data,
                        goalValue = goal,
                        color = AccentBlue
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsLineChart(
    data: List<Float>,
    goalValue: Float? = null,
    color: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        val maxDataVal = data.maxOrNull() ?: 1f
        val minDataVal = data.minOrNull() ?: 0f
        
        val maxVal = if (goalValue != null) maxOf(maxDataVal, goalValue) else maxDataVal
        val minVal = if (goalValue != null) minOf(minDataVal, goalValue) else minDataVal
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * (width / (data.size - 1)),
                y = height - ((value - minVal) / range) * height
            )
        }

        // Area under path
        val fillPath = Path().apply {
            moveTo(points[0].x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Goal Line
        if (goalValue != null) {
            val goalY = height - ((goalValue - minVal) / range) * height
            drawLine(
                color = TextGray.copy(alpha = 0.3f),
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Main Path
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Points
        points.forEach { point ->
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = DeepNavy,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}
