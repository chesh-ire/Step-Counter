package com.example.stepcounter.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.stepcounter.MainActivity
import com.example.stepcounter.R
import com.example.stepcounter.StepCounterApp
import com.example.stepcounter.data.local.entities.CalorieType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var initialStepCount = -1
    private var currentDaySteps = 0
    private var stepsAtStartOfHour = -1
    private var lastUpdateDate = -1L
    private var currentHour = -1

    private var caloriesAccumulator = 0.0

    private val midnightReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_DATE_CHANGED || intent?.action == Intent.ACTION_TIME_CHANGED) {
                checkAndResetSteps()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
        }
        registerReceiver(midnightReceiver, filter)

        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            val repository = (application as StepCounterApp).repository
            val todayEntry = repository.getStepsForToday().first()
            currentDaySteps = todayEntry?.steps ?: 0
            lastUpdateDate = getStartOfDay()
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun checkAndResetSteps() {
        val now = Calendar.getInstance()
        val today = getStartOfDay()
        val hour = now.get(Calendar.HOUR_OF_DAY)

        if (lastUpdateDate != -1L && today > lastUpdateDate) {
            currentDaySteps = 0
            initialStepCount = -1 
            stepsAtStartOfHour = -1
            lastUpdateDate = today
            currentHour = hour
        } else if (hour != currentHour) {
            // New hour started, reset hourly baseline
            stepsAtStartOfHour = -1
            currentHour = hour
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            checkAndResetSteps()
            val totalStepsSinceBoot = event.values[0].toInt()

            if (initialStepCount == -1) {
                initialStepCount = totalStepsSinceBoot
            }
            
            if (stepsAtStartOfHour == -1) {
                stepsAtStartOfHour = totalStepsSinceBoot
            }

            val stepsSinceLastChange = totalStepsSinceBoot - initialStepCount
            val stepsInCurrentHour = totalStepsSinceBoot - stepsAtStartOfHour
            
            initialStepCount = totalStepsSinceBoot
            
            if (stepsSinceLastChange > 0) {
                currentDaySteps += stepsSinceLastChange
                updateDatabase(currentDaySteps, stepsSinceLastChange, stepsInCurrentHour)
                
                serviceScope.launch {
                    val app = application as StepCounterApp
                    app.goalPreferences.updateLastActivityTimestamp(System.currentTimeMillis())
                }
            }
        }
    }

    private fun updateDatabase(totalSteps: Int, deltaSteps: Int, stepsInHour: Int) {
        serviceScope.launch {
            val repository = (application as StepCounterApp).repository
            repository.updateSteps(totalSteps)
            repository.updateHourlySteps(stepsInHour)

            // Accumulate fractional calories and only commit whole numbers, carrying
            // the remainder forward. Avoids truncating every small step delta to 0.
            caloriesAccumulator += deltaSteps * CALORIES_PER_STEP
            val caloriesBurned = caloriesAccumulator.toInt()
            if (caloriesBurned > 0) {
                repository.addCalorie(caloriesBurned, CalorieType.BURNED)
                caloriesAccumulator -= caloriesBurned
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, StepCounterApp.CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText("Counting your steps in real-time...")
            .setSmallIcon(R.drawable.placeholder)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(midnightReceiver)
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CALORIES_PER_STEP = 0.04
    }
}
