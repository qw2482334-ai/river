cat << 'INNER_EOF' > app/src/main/java/com/example/data/InvestmentModels.kt
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "investments")
data class InvestmentItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: Long = 0L,
    val name: String,
    val code: String = "",
    val type: InvestmentType,
    val principal: Double,
    val currentValue: Double,
    val notes: String = ""
) {
    val profitLoss: Double get() = currentValue - principal
    val profitLossRate: Double get() = if (principal > 0) (currentValue - principal) / principal * 100 else 0.0
}

enum class InvestmentType(val label: String) {
    STOCK("股票"),
    FUND("基金/ETF"),
    WEALTH("定期/理财"),
    CRYPTO("数字资产/其他")
}

@Entity(tableName = "lotteries")
data class LotteryRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: Long = 0L,
    val title: String,
    val type: LotteryType,
    val betAmount: Double,
    val winAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: LotteryStatus = LotteryStatus.PENDING,
    val notes: String = ""
) {
    val netProfit: Double get() = winAmount - betAmount
}

enum class LotteryType(val label: String) {
    FOOTBALL("竞彩足球"),
    BASKETBALL("竞彩篮球"),
    NUMBER("大乐透/双色球"),
    SCRATCH("即开型刮刮乐")
}

enum class LotteryStatus(val label: String) {
    PENDING("待开奖"),
    WON("已中奖"),
    LOST("未中奖")
}
INNER_EOF
