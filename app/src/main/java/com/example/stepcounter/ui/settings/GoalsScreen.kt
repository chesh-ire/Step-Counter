package com.example.stepcounter.ui.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepcounter.data.preferences.UserGoals
import com.example.stepcounter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    currentGoals: UserGoals,
    onBack: () -> Unit,
    onSave: (UserGoals) -> Unit
) {
    var stepGoal by remember { mutableStateOf(currentGoals.stepGoal.toString()) }
    var waterGoal by remember { mutableStateOf(currentGoals.waterGoalMl.toString()) }
    var weightGoal by remember { mutableStateOf(currentGoals.weightGoalKg.toString()) }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Set Your Goals", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GoalInputItem(
                        title = "Step Goal",
                        subtitle = "steps/day",
                        value = stepGoal,
                        onValueChange = { if (it.all { c -> c.isDigit() }) stepGoal = it },
                        icon = Icons.Rounded.DirectionsRun,
                        color = AccentCyan
                    )
                }
                item {
                    GoalInputItem(
                        title = "Distance Goal",
                        subtitle = "km/day",
                        value = "8", // For UI demo
                        onValueChange = {},
                        icon = Icons.Rounded.Map,
                        color = AccentBlue
                    )
                }
                item {
                    GoalInputItem(
                        title = "Calorie Goal",
                        subtitle = "kcal/day",
                        value = "500", // For UI demo
                        onValueChange = {},
                        icon = Icons.Rounded.LocalFireDepartment,
                        color = CalorieOrange
                    )
                }
                item {
                    GoalInputItem(
                        title = "Water Goal",
                        subtitle = "L/day",
                        value = (waterGoal.toDoubleOrNull() ?: 0.0).div(1000).toString(),
                        onValueChange = { 
                            waterGoal = (it.toDoubleOrNull()?.times(1000)?.toInt() ?: 0).toString()
                        },
                        icon = Icons.Rounded.WaterDrop,
                        color = WaterBlue
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Text("Weight Goal", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Target Weight", color = TextGray, fontSize = 12.sp)
                                Text("$weightGoal kg", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextGray)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(UserGoals(
                        stepGoal = stepGoal.toIntOrNull() ?: currentGoals.stepGoal,
                        waterGoalMl = waterGoal.toIntOrNull() ?: currentGoals.waterGoalMl,
                        weightGoalKg = weightGoal.toDoubleOrNull() ?: currentGoals.weightGoalKg
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)) // Bright Green like image
            ) {
                Text("Save Goals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun GoalInputItem(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            Text(value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
