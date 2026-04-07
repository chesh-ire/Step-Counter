package com.example.stepcounter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stepcounter.data.local.dao.*
import com.example.stepcounter.data.local.entities.*

@Database(
    entities = [StepEntry::class, HourlyStep::class, WaterEntry::class, CalorieEntry::class, WeightEntry::class, FoodItem::class, FoodConsumption::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun waterDao(): WaterDao
    abstract fun calorieDao(): CalorieDao
    abstract fun weightDao(): WeightDao
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
