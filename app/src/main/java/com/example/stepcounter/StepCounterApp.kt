package com.example.stepcounter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stepcounter.data.local.AppDatabase
import com.example.stepcounter.data.preferences.GoalPreferences
import com.example.stepcounter.data.repository.HealthRepository
import com.example.stepcounter.worker.NudgeWorker
import java.util.concurrent.TimeUnit

class StepCounterApp : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        HealthRepository(
            database.stepDao(),
            database.waterDao(),
            database.calorieDao(),
            database.weightDao()
        )
    }
    val goalPreferences by lazy { GoalPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleNudges()
    }

    private fun scheduleNudges() {
        val nudgeRequest = PeriodicWorkRequestBuilder<NudgeWorker>(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "hourly_nudge",
            ExistingPeriodicWorkPolicy.KEEP,
            nudgeRequest
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Counter Notifications"
            val descriptionText = "Notifications for step counting and nudges"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "step_counter_channel"
    }
}
