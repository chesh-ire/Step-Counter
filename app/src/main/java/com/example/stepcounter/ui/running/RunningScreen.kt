package com.example.stepcounter.ui.running

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.ui.MainViewModel
import com.example.stepcounter.ui.components.BottomNavigationBar
import com.example.stepcounter.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.runDurationSeconds.collectAsStateWithLifecycle()
    val distanceKm by viewModel.runDistanceKm.collectAsStateWithLifecycle()

    val pace = if (distanceKm > 0) {
        val minutes = (durationSeconds / 60.0)
        minutes / distanceKm
    } else 0.0

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Running Stats", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "running",
                onHome = onNavigateToHome,
                onFriends = {},
                onWater = onNavigateToWater,
                onStats = onNavigateToStats,
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
            // Map Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardNavy)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Rounded.Map, contentDescription = null, tint = AccentCyan.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Text("Route tracking active", color = TextGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RunStatItem(
                    label = "Distance",
                    value = String.format(Locale.getDefault(), "%.2f", distanceKm),
                    unit = "km"
                )
                RunStatItem(
                    label = "Duration",
                    value = formatDuration(durationSeconds),
                    unit = ""
                )
                RunStatItem(
                    label = "Pace",
                    value = String.format(Locale.getDefault(), "%.1f", pace),
                    unit = "min/km"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start / End Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isRunning) {
                    Button(
                        onClick = { viewModel.startRun() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = DeepNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Run", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                } else {
                    Button(
                        onClick = { /* TODO: Pause functionality */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardNavy)
                    ) {
                        Icon(Icons.Rounded.Pause, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause", color = TextWhite)
                    }
                    Button(
                        onClick = { viewModel.stopRun() },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("End Run", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RunStatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Black)
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
