package com.example.stepcounter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
    var distanceGoal by remember { mutableStateOf(currentGoals.distanceGoalKm.toString()) }
    var calorieGoal by remember { mutableStateOf(currentGoals.calorieGoal.toString()) }

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
                        value = distanceGoal,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) distanceGoal = it },
                        icon = Icons.Rounded.Map,
                        color = AccentBlue
                    )
                }
                item {
                    GoalInputItem(
                        title = "Calorie Goal",
                        subtitle = "kcal/day",
                        value = calorieGoal,
                        onValueChange = { if (it.all { c -> c.isDigit() }) calorieGoal = it },
                        icon = Icons.Rounded.LocalFireDepartment,
                        color = CalorieOrange
                    )
                }
                item {
                    GoalInputItem(
                        title = "Water Goal",
                        subtitle = "ml/day",
                        value = waterGoal,
                        onValueChange = { if (it.all { c -> c.isDigit() }) waterGoal = it },
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
                        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                            Text("Target Weight (kg)", color = TextGray, fontSize = 12.sp)
                            TextField(
                                value = weightGoal,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) weightGoal = it },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    cursorColor = AccentCyan
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(UserGoals(
                        stepGoal = stepGoal.toIntOrNull() ?: currentGoals.stepGoal,
                        waterGoalMl = waterGoal.toIntOrNull() ?: currentGoals.waterGoalMl,
                        weightGoalKg = weightGoal.toDoubleOrNull() ?: currentGoals.weightGoalKg,
                        distanceGoalKm = distanceGoal.toDoubleOrNull() ?: currentGoals.distanceGoalKm,
                        calorieGoal = calorieGoal.toIntOrNull() ?: currentGoals.calorieGoal
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)) 
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
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = color
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}
