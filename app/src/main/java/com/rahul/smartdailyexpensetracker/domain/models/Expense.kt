package com.rahul.smartdailyexpensetracker.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val notes: String = "",
    val receiptImageUris: List<String> = emptyList(),
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val date: LocalDate = LocalDate.now()
)