package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String = TransactionType.EXPENSE.name,
    val category: String,
    val ledgerName: String = "日常账本",
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val isRecurring: Boolean = false
)
