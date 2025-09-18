package com.rahul.smartdailyexpensetracker.domain.usecases

import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import java.time.LocalDate
import javax.inject.Inject

class GetDailyTotalUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<LocalDate, Double>(dispatcher) {

    override suspend fun execute(parameters: LocalDate): Double {
        return expenseRepository.getTotalForDate(parameters)
    }
}