package com.example.stepcounter.data.local

import androidx.room.TypeConverter
import com.example.stepcounter.data.local.entities.CalorieType

class Converters {
    @TypeConverter
    fun fromCalorieType(value: CalorieType): String {
        return value.name
    }

    @TypeConverter
    fun toCalorieType(value: String): CalorieType {
        return CalorieType.valueOf(value)
    }
}
