package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.ColorEducation
import com.example.ui.theme.ColorEntertainment
import com.example.ui.theme.ColorFood
import com.example.ui.theme.ColorHousing
import com.example.ui.theme.ColorMedical
import com.example.ui.theme.ColorOther
import com.example.ui.theme.ColorShopping
import com.example.ui.theme.ColorTransport

enum class ExpenseCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val badgeBg: Color,
    val emoji: String,
    val isIncome: Boolean = false
) {
    // Expense categories
    FOOD("餐饮", Icons.Default.Restaurant, Color(0xFFC026D3), ColorFood, "🍔"),
    TRANSPORT("交通", Icons.Default.DirectionsCar, Color(0xFF6750A4), ColorTransport, "🚗"),
    ENTERTAINMENT("娱乐", Icons.Default.LocalMovies, Color(0xFF0284C7), ColorEntertainment, "🎬"),
    SHOPPING("购物", Icons.Default.ShoppingBag, Color(0xFF7E22CE), ColorShopping, "🛍️"),
    HOUSING("住房", Icons.Default.Home, Color(0xFF059669), ColorHousing, "🏠"),
    MEDICAL("医疗", Icons.Default.MedicalServices, Color(0xFFEA580C), ColorMedical, "💊"),
    EDUCATION("学习", Icons.Default.School, Color(0xFFCA8A04), ColorEducation, "📚"),
    
    // Income categories
    SALARY("工资", Icons.Default.Work, Color(0xFF16A34A), Color(0xFFDCFCE7), "💰", true),
    PART_TIME("兼职", Icons.Default.MonetizationOn, Color(0xFF0D9488), Color(0xFFCCFBF1), "💼", true),
    BONUS("奖金", Icons.Default.TrendingUp, Color(0xFFD97706), Color(0xFFFEF3C7), "🎁", true),
    INVESTMENT("投资", Icons.Default.AccountBalance, Color(0xFF2563EB), Color(0xFFDBEAFE), "📈", true),
    RED_PACKET("红包", Icons.Default.CardGiftcard, Color(0xFFDC2626), Color(0xFFFEE2E2), "🧧", true),
    
    OTHER("其它", Icons.Default.Category, Color(0xFF475569), ColorOther, "📦");

    companion object {
        fun fromTitle(title: String): ExpenseCategory {
            return entries.find { it.title == title } ?: OTHER
        }

        val expenseCategories = entries.filter { !it.isIncome || it == OTHER }
        val incomeCategories = entries.filter { it.isIncome || it == OTHER }
        val allCategories = entries.toList()
    }
}
