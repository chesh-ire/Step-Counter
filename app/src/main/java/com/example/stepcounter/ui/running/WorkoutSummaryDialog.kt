package com.example.stepcounter.ui.running

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepcounter.data.local.entities.Workout
import com.example.stepcounter.ui.theme.AccentCyan
import java.util.*

@Composable
fun WorkoutSummaryDialog(
    workout: Workout,
    onDismiss: () -> Unit
) {
    val pace = if (workout.distanceKm > 0) {
        (workout.durationSeconds / 60.0) / workout.distanceKm
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Run Summary", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem("Distance", "%.2f km".format(Locale.getDefault(), workout.distanceKm))
                    SummaryItem("Duration", formatDuration(workout.durationSeconds))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem("Avg Pace", "%.1f min/km".format(Locale.getDefault(), pace))
                    SummaryItem("Steps", workout.steps.toString())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem("Calories", "${workout.calories} kcal")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("Great Work!", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
