package com.example.stepcounter.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stepcounter.data.preferences.UserGoals

@Composable
fun GoalSettingDialog(
    currentGoals: UserGoals,
    onDismiss: () -> Unit,
    onConfirm: (UserGoals) -> Unit
) {
    var stepGoal by remember { mutableStateOf(currentGoals.stepGoal.toString()) }
    var waterGoal by remember { mutableStateOf(currentGoals.waterGoalMl.toString()) }
    var weightGoal by remember { mutableStateOf(currentGoals.weightGoalKg.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Goals") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stepGoal,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stepGoal = it },
                    label = { Text("Daily Step Goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = waterGoal,
                    onValueChange = { if (it.all { c -> c.isDigit() }) waterGoal = it },
                    label = { Text("Daily Water Goal (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightGoal,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) weightGoal = it 
                    },
                    label = { Text("Target Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val goals = UserGoals(
                        stepGoal = stepGoal.toIntOrNull() ?: currentGoals.stepGoal,
                        waterGoalMl = waterGoal.toIntOrNull() ?: currentGoals.waterGoalMl,
                        weightGoalKg = weightGoal.toDoubleOrNull() ?: currentGoals.weightGoalKg
                    )
                    onConfirm(goals)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
