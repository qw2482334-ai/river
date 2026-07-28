package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val type: String = "支出", // "支出" or "收入"
    val ledger: String = "日常账本" // "日常账本", "家庭账本", "旅行出差", "工作报销"
)
