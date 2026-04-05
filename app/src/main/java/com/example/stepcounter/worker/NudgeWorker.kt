package com.example.stepcounter.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stepcounter.R
import com.example.stepcounter.StepCounterApp
import kotlinx.coroutines.flow.first

class NudgeWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val app = applicationContext as StepCounterApp
        val lastActivity = app.goalPreferences.lastActivityTimestampFlow.first()
        val currentTime = System.currentTimeMillis()
        
        // One hour in milliseconds
        val oneHour = 60 * 60 * 1000L
        
        // Only notify if the user has been idle for at least 1 hour
        if (currentTime - lastActivity >= oneHour) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = NotificationCompat.Builder(applicationContext, StepCounterApp.CHANNEL_ID)
                .setContentTitle("Stay Active!")
                .setContentText("You haven't moved in an hour! Try to get some steps in and stay hydrated.")
                .setSmallIcon(R.drawable.placeholder)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        return androidx.work.ListenableWorker.Result.success()
    }

    companion object {
        private const val NOTIFICATION_ID = 101
    }
}
