package com.example.stepcounter.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val weightHistory by viewModel.weightHistory.collectAsStateWithLifecycle()
    val stepHistory by viewModel.stepHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val insights by viewModel.insights.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Health Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Smart Insights Section
            item {
                Text(
                    text = "Smart Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    insights.forEach { insight ->
                        InsightCard(insight)
                    }
                    if (insights.isEmpty()) {
                        Text(
                            "No insights yet. Keep logging your data!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Weight Trend Chart
            item {
                AnalyticsChartCard(
                    title = "Weight Trend (Target: ${userGoals.weightGoalKg}kg)",
                    data = weightHistory.map { it.weightKg.toFloat() },
                    goalValue = userGoals.weightGoalKg.toFloat(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Step Progress Chart
            item {
                AnalyticsChartCard(
                    title = "Step History (Goal: ${userGoals.stepGoal})",
                    data = stepHistory.map { it.steps.toFloat() },
                    goalValue = userGoals.stepGoal.toFloat(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            item { 
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
fun InsightCard(insight: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = insight, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AnalyticsChartCard(
    title: String,
    data: List<Float>,
    goalValue: Float? = null,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (data.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Need more data points to show trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SimpleLineChart(
                    data = data,
                    goalValue = goalValue,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<Float>,
    goalValue: Float? = null,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val maxDataVal = data.maxOrNull() ?: 1f
        val minDataVal = data.minOrNull() ?: 0f
        
        // Include goal in range calculation
        val maxVal = if (goalValue != null) maxOf(maxDataVal, goalValue) else maxDataVal
        val minVal = if (goalValue != null) minOf(minDataVal, goalValue) else minDataVal
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * (width / (data.size - 1)),
                y = height - ((value - minVal) / range) * height
            )
        }

        // Draw Goal Line
        if (goalValue != null) {
            val goalY = height - ((goalValue - minVal) / range) * height
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )

        points.forEach { point ->
            drawCircle(
                color = color,
                radius = 5.dp.toPx(),
                center = point
            )
        }
    }
}
