package com.example.stepcounter.ui.logging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stepcounter.data.local.entities.CalorieType
import com.example.stepcounter.data.local.entities.FoodItem

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
fun FoodLoggingDialog(
    searchResults: List<FoodItem>,
    onSearch: (String) -> Unit,
    onLog: (FoodItem, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var amountGrams by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedFood == null) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            onSearch(it)
                        },
                        label = { Text("Search Food (e.g. Apple, Rice)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(searchResults) { food ->
                            ListItem(
                                headlineContent = { Text(food.name) },
                                supportingContent = { Text("${food.caloriesPer100g} kcal / 100g") },
                                modifier = Modifier.clickable { selectedFood = food }
                            )
                        }
                    }
                } else {
                    Text("Logging: ${selectedFood?.name}", fontWeight = FontWeight.Bold)
                    Text("Calories: ${selectedFood?.caloriesPer100g} kcal per 100g")
                    
                    OutlinedTextField(
                        value = amountGrams,
                        onValueChange = { if (it.all { c -> c.isDigit() }) amountGrams = it },
                        label = { Text("Amount (grams)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    TextButton(onClick = { selectedFood = null }) {
                        Text("Back to Search")
                    }
                }
            }
        },
        confirmButton = {
            if (selectedFood != null) {
                TextButton(
                    onClick = {
                        selectedFood?.let { onLog(it, amountGrams.toIntOrNull() ?: 100) }
                        onDismiss()
                    }
                ) {
                    Text("Log")
                }
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
