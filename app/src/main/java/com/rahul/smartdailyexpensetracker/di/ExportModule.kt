package com.rahul.smartdailyexpensetracker.di

import android.content.Context
import com.rahul.smartdailyexpensetracker.data.repository.ChartGeneratorImpl
import com.rahul.smartdailyexpensetracker.data.repository.ExpenseExporterImpl
import com.rahul.smartdailyexpensetracker.domain.repository.ChartGenerator
import com.rahul.smartdailyexpensetracker.presentation.utility.ExpenseExporter
import com.rahul.smartdailyexpensetracker.presentation.utility.SimplePdfExportManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExportModule {

    @Binds
    abstract fun bindExpenseExporter(
        expenseExporterImpl: ExpenseExporterImpl
    ): ExpenseExporter

    @Binds
    abstract fun bindChartGenerator(
        chartGeneratorImpl: ChartGeneratorImpl
    ): ChartGenerator

    companion object {
        @Provides
        @Singleton
        fun provideSimplePdfExportManager(
            @ApplicationContext context: Context
        ): SimplePdfExportManager {
            return SimplePdfExportManager(context)
        }
    }
}