package com.example.stepcounter.ui.logging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stepcounter.data.local.entities.CalorieType

@Composable
fun WaterLoggingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Water Intake") },
        text = {
            Column {
                Text("Quick Add:")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { onConfirm(250); onDismiss() }) { Text("+250ml") }
                    Button(onClick = { onConfirm(500); onDismiss() }) { Text("+500ml") }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("Custom Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toIntOrNull()?.let { onConfirm(it) }
                    onDismiss()
                },
                enabled = amount.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CalorieLoggingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, CalorieType) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CalorieType.CONSUMED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Calories") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = type == CalorieType.CONSUMED,
                        onClick = { type = CalorieType.CONSUMED },
                        label = { Text("Consumed") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = type == CalorieType.BURNED,
                        onClick = { type = CalorieType.BURNED },
                        label = { Text("Burned") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toIntOrNull()?.let { onConfirm(it, type) }
                    onDismiss()
                },
                enabled = amount.isNotEmpty()
            ) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WeightLoggingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var weight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Weight") },
        text = {
            OutlinedTextField(
                value = weight,
                onValueChange = { 
                    if (it.isEmpty() || it.toDoubleOrNull() != null) weight = it 
                },
                label = { Text("Current Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    weight.toDoubleOrNull()?.let { onConfirm(it) }
                    onDismiss()
                },
                enabled = weight.isNotEmpty()
            ) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
