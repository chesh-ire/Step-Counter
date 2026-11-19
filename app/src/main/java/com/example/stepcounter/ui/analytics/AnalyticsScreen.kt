package com.example.stepcounter.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.ui.MainViewModel
import com.example.stepcounter.ui.components.BottomNavigationBar
import com.example.stepcounter.ui.theme.*
import java.util.*
import kotlin.math.roundToInt

enum class TimeRange(val label: String, val days: Int) {
    WEEK("Week", 7),
    MONTH("Month", 30),
    YEAR("Year", 365)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRunning: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogWeight: () -> Unit
) {
    val weightHistory by viewModel.weightHistory.collectAsStateWithLifecycle()
    val stepHistory by viewModel.stepHistory.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    val latestWeight by viewModel.latestWeight.collectAsStateWithLifecycle()

    var selectedRange by remember { mutableStateOf(TimeRange.WEEK) }
    val cutoff = remember(selectedRange) {
        System.currentTimeMillis() - selectedRange.days * 24L * 60 * 60 * 1000
    }
    val filteredWeight = remember(weightHistory, cutoff) {
        weightHistory.filter { it.timestamp >= cutoff }
    }
    val filteredSteps = remember(stepHistory, cutoff) {
        stepHistory.filter { it.date >= cutoff }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Health Analytics", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onLogWeight) {
                        Icon(Icons.Rounded.Add, contentDescription = "Log Weight", tint = AccentCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "stats",
                onHome = onNavigateToHome,
                onRunning = onNavigateToRunning,
                onWater = onNavigateToWater,
                onStats = {},
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
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Goal: ${userGoals.weightGoalKg} kg",
                            color = AccentCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onLogWeight, contentPadding = PaddingValues(0.dp)) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update", color = AccentCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Time range filter (applies to both charts)
            item {
                TimeRangeSelector(
                    selected = selectedRange,
                    onSelected = { selectedRange = it }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Weight Chart Card
            item {
                WeightChartCard(filteredWeight.map { it.weightKg.toFloat() }, userGoals.weightGoalKg.toFloat())
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Activity History", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Step Progress Chart
            item {
                StepHistoryChart(filteredSteps.map { it.steps.toFloat() }, userGoals.stepGoal.toFloat())
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardNavy)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeRange.values().forEach { range ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) AccentCyan else Color.Transparent)
                    .clickable { onSelected(range) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    color = if (isSelected) DeepNavy else TextGray,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
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
            Text("Weight Trend", color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data.size < 2) {
                    Text("Add more weight entries to see trend", color = TextGray, fontSize = 12.sp)
                } else {
                    AnalyticsLineChart(
                        data = data,
                        goalValue = goal,
                        color = AccentCyan,
                        unit = "kg"
                    )
                }
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
            Text("Daily Steps", color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data.size < 2) {
                    Text("Counting your journey...", color = TextGray, fontSize = 12.sp)
                } else {
                    AnalyticsLineChart(
                        data = data,
                        goalValue = goal,
                        color = AccentBlue,
                        unit = "steps"
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
    color: Color,
    unit: String = ""
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column {
        // Selection display
        Row(
            modifier = Modifier.fillMaxWidth().height(30.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedIndex != null && selectedIndex!! < data.size) {
                val value = data[selectedIndex!!]
                val text = if (unit == "kg") {
                    String.format(Locale.getDefault(), "%.1f kg", value)
                } else {
                    String.format(Locale.getDefault(), "%,d steps", value.toInt())
                }
                Text(text = text, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(text = "Tap nodes to see details", color = TextGray, fontSize = 12.sp)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        if (data.isNotEmpty()) {
                            val width = size.width
                            val stepX = if (data.size > 1) width / (data.size - 1) else width
                            val index = if (data.size > 1) {
                                (offset.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                            } else 0
                            selectedIndex = if (selectedIndex == index) null else index
                        }
                    }
                }
            ) {
                val width = size.width
                val height = size.height
                
                val maxDataVal = data.maxOrNull() ?: 1f
                val minDataVal = data.minOrNull() ?: 0f
                
                val maxVal = if (goalValue != null) maxOf(maxDataVal, goalValue) else maxDataVal
                val minVal = if (goalValue != null) minOf(minDataVal, goalValue) else minDataVal
                val range = (maxVal - minVal).coerceAtLeast(1f)

                val points = data.mapIndexed { index, value ->
                    Offset(
                        x = if (data.size > 1) index * (width / (data.size - 1)) else width / 2,
                        y = height - ((value - minVal) / range) * height
                    )
                }

                // Area under path
                if (points.size > 1) {
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
                }

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
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Selection Helper Line
                selectedIndex?.let { index ->
                    if (index < points.size) {
                        val point = points[index]
                        drawLine(
                            color = Color.White.copy(alpha = 0.3f),
                            start = Offset(point.x, 0f),
                            end = Offset(point.x, height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Points
                points.forEachIndexed { index, point ->
                    val isSelected = selectedIndex == index
                    drawCircle(
                        color = if (isSelected) Color.White else color,
                        radius = (if (isSelected) 6.dp else 4.dp).toPx(),
                        center = point
                    )
                    drawCircle(
                        color = DeepNavy,
                        radius = (if (isSelected) 3.dp else 2.dp).toPx(),
                        center = point
                    )
                }
            }
        }
    }
}
