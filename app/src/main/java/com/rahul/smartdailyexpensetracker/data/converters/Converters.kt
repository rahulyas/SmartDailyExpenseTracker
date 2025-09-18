package com.rahul.smartdailyexpensetracker.data.converters

import androidx.room.TypeConverter
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return try {
            value?.let { LocalDateTime.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return try {
            value?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory?): String? {
        return value?.name
    }

    @TypeConverter
    fun toExpenseCategory(value: String?): ExpenseCategory? {
        return try {
            value?.let { ExpenseCategory.valueOf(it) }
        } catch (e: Exception) {
            ExpenseCategory.STAFF
        }
    }
}
