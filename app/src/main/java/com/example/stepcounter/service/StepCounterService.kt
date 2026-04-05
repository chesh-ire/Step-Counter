package com.example.stepcounter.service

import android.app.Notification
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
    private var lastUpdateDate = -1L

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
        val today = getStartOfDay()
        if (lastUpdateDate != -1L && today > lastUpdateDate) {
            currentDaySteps = 0
            initialStepCount = -1 
            lastUpdateDate = today
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

            val stepsSinceLastChange = totalStepsSinceBoot - initialStepCount
            initialStepCount = totalStepsSinceBoot
            
            if (stepsSinceLastChange > 0) {
                currentDaySteps += stepsSinceLastChange
                updateDatabase(currentDaySteps, stepsSinceLastChange)
                
                // Update last activity timestamp for nudge logic
                serviceScope.launch {
                    val app = application as StepCounterApp
                    app.goalPreferences.updateLastActivityTimestamp(System.currentTimeMillis())
                }
            }
        }
    }

    private fun updateDatabase(totalSteps: Int, deltaSteps: Int) {
        serviceScope.launch {
            val repository = (application as StepCounterApp).repository
            repository.updateSteps(totalSteps)
            
            val caloriesBurned = (deltaSteps * 0.04).toInt()
            if (caloriesBurned > 0) {
                repository.addCalorie(caloriesBurned, CalorieType.BURNED)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, StepCounterApp.CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText("Counting your steps in real-time...")
            .setSmallIcon(R.drawable.placeholder)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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
    }
}
