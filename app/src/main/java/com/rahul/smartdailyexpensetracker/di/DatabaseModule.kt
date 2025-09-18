package com.rahul.smartdailyexpensetracker.di

import android.content.Context
import androidx.room.Room
import com.rahul.smartdailyexpensetracker.data.dao.ExpenseDao
import com.rahul.smartdailyexpensetracker.data.database.ExpenseDatabase
import com.rahul.smartdailyexpensetracker.data.database.ExpenseDatabase.Companion.DATABASE_NAME
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.data.repository.ExpenseRepositoryImpl
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun bindExpenseRepository(
        expenseDao: ExpenseDao,
        userPreferences: UserPreferences,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(expenseDao, userPreferences, dispatcher)
    }
}