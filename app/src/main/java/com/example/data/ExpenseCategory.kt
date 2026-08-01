package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ExpenseCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isIncome: Boolean = false,
    val defaultMonthlyBudget: Double = 1000.0
) {
    companion object {
        val Categories = listOf(
            // 支出分类
            ExpenseCategory("餐饮", Icons.Default.Restaurant, Color(0xFFFF6B6B), false, 2500.0),
            ExpenseCategory("交通", Icons.Default.DirectionsBus, Color(0xFF4ECDC4), false, 800.0),
            ExpenseCategory("购物", Icons.Default.ShoppingBag, Color(0xFFFFD166), false, 2000.0),
            ExpenseCategory("居住", Icons.Default.Home, Color(0xFF118AB2), false, 3500.0),
            ExpenseCategory("娱乐", Icons.Default.SportsEsports, Color(0xFF06D6A0), false, 1200.0),
            ExpenseCategory("医疗", Icons.Default.LocalHospital, Color(0xFFEF476F), false, 1000.0),
            ExpenseCategory("数码", Icons.Default.Devices, Color(0xFF9D4EDD), false, 1500.0),
            ExpenseCategory("人情", Icons.Default.CardGiftcard, Color(0xFFFF9F1C), false, 1000.0),
            ExpenseCategory("学习", Icons.Default.School, Color(0xFF2A9D8F), false, 800.0),
            ExpenseCategory("其他", Icons.Default.MoreHoriz, Color(0xFF8D99AE), false, 1000.0),

            // 收入分类
            ExpenseCategory("工资", Icons.Default.AccountBalanceWallet, Color(0xFF00B4D8), true, 0.0),
            ExpenseCategory("兼职", Icons.Default.Work, Color(0xFF48CAE4), true, 0.0),
            ExpenseCategory("理财", Icons.Default.TrendingUp, Color(0xFF52B788), true, 0.0),
            ExpenseCategory("礼金", Icons.Default.Redeem, Color(0xFFE76F51), true, 0.0),
            ExpenseCategory("其他收入", Icons.Default.AttachMoney, Color(0xFF70E000), true, 0.0)
        )

        fun getCategoryByName(name: String): ExpenseCategory {
            return Categories.find { it.name == name } ?: ExpenseCategory("其他", Icons.Default.MoreHoriz, Color(0xFF8D99AE))
        }
    }
}
