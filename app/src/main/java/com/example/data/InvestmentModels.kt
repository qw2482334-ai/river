package com.example.data

data class InvestmentItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val code: String = "",
    val type: InvestmentType,
    val principal: Double, // 本金/买入总成本
    val currentValue: Double, // 当前估值/最新市值
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

data class LotteryRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String, // 例如: 英超 曼城 VS 阿森纳
    val type: LotteryType,
    val betAmount: Double, // 投注/购彩金额
    val winAmount: Double = 0.0, // 中奖/派奖金额
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
