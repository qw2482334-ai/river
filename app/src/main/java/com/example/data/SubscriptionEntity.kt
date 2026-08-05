package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String = "EXPENSE",
    val cycle: String, // "MONTHLY", "YEARLY"
    val nextBillingDate: Long,
    val note: String = "",
    val ledgerName: String = "日常账本"
)
