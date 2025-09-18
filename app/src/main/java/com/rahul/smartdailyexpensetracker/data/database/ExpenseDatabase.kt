package com.rahul.smartdailyexpensetracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rahul.smartdailyexpensetracker.data.converters.Converters
import com.rahul.smartdailyexpensetracker.data.dao.ExpenseDao
import com.rahul.smartdailyexpensetracker.domain.models.Expense

@Database(
    entities = [Expense::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_database"
    }
}