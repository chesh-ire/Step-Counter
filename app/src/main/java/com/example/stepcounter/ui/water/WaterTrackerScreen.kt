package com.example.stepcounter.ui.water

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
fun WaterTrackerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogWater: () -> Unit
) {
    val waterIntake by viewModel.todayWater.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    
    val goalLiters = userGoals.waterGoalMl.toFloat() / 1000f
    val currentLiters = waterIntake.toFloat() / 1000f
    val progress = if (userGoals.waterGoalMl > 0) waterIntake.toFloat() / userGoals.waterGoalMl else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Water Tracker", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = AccentCyan, modifier = Modifier.padding(end = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "water",
                onHome = onNavigateToHome,
                onStats = onNavigateToStats,
                onWater = {},
                onGoals = onNavigateToGoals,
                onProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daily Hydration", color = TextGray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Bottle Graphic Placeholder
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(180.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(CardNavy)
            ) {
                // Water Level
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(AccentCyan.copy(alpha = 0.8f), AccentBlue)
                            )
                        )
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1fL", currentLiters),
                        color = TextWhite,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Goal: ${String.format(Locale.getDefault(), "%.1fL", goalLiters)}",
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.LocalDrink,
                    text = "${(waterIntake / 250)} Glasses",
                    color = AccentCyan
                )
                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.ShowChart,
                    text = "${(progress * 100).toInt()}% of Goal",
                    color = AccentBlue
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onLogWater,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Water", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = { /* TODO: Reminders */ },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reminders")
                }
            }
        }
    }
}

@Composable
fun InfoTile(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
