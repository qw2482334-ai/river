package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val category: String = "储蓄",
    val targetDateMillis: Long = System.currentTimeMillis() + 90L * 24 * 3600 * 1000,
    val iconName: String = "savings",
    val note: String = ""
)
