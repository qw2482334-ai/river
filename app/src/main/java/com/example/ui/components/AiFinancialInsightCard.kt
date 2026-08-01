package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExpenseEntity
import com.example.data.TransactionType
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed

@Composable
fun AiFinancialInsightCard(
    expenses: List<ExpenseEntity>,
    monthlyBudget: Double,
    isGeneratingInsight: Boolean,
    onlineInsightText: String?,
    onFetchOnlineInsight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpense = remember(expenses) {
        expenses.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    }
    val totalIncome = remember(expenses) {
        expenses.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
    }

    // Rationality score (0-100)
    val rationalityScore = remember(totalExpense, totalIncome, monthlyBudget) {
        if (monthlyBudget <= 0) 85
        else {
            val ratio = totalExpense / monthlyBudget
            when {
                ratio <= 0.5 -> 95
                ratio <= 0.8 -> 88
                ratio <= 1.0 -> 75
                ratio <= 1.2 -> 60
                else -> 45
            }
        }
    }

    // Detect spending anomalies (e.g., categories with many small transactions or single large transaction)
    val anomalies = remember(expenses) {
        val expenseList = expenses.filter { it.type == TransactionType.EXPENSE.name }
        val list = mutableListOf<String>()

        // 1. Single large transaction (> 500 RMB)
        val large = expenseList.filter { it.amount >= 500.0 }
        if (large.isNotEmpty()) {
            list.add("存在 ${large.size} 笔 ￥500 以上的大额开支 (最高: ￥${large.maxOf { it.amount }})")
        }

        // 2. High dining frequency
        val diningCount = expenseList.count { it.category == "餐饮" }
        if (diningCount >= 5) {
            list.add("近期餐饮频次偏高 (${diningCount} 次)，可适当减少外卖频次")
        }

        // 3. Overall ratio
        if (totalExpense > monthlyBudget && monthlyBudget > 0) {
            list.add("当月支出已超过设定的总预算限额 ￥${monthlyBudget.toInt()}")
        }

        list
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_financial_insight_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI 消费理性度与异常诊断",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "算法智能评级与联网深度财务洞察",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onFetchOnlineInsight,
                    enabled = !isGeneratingInsight,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (isGeneratingInsight) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("联网分析", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Gauge Section
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "当月理性消费指数",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$rationalityScore",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (rationalityScore >= 80) IncomeGreen else if (rationalityScore >= 60) MaterialTheme.colorScheme.primary else ExpenseRed
                            )
                            Text(
                                text = " / 100",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Surface(
                        color = if (rationalityScore >= 80) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (rationalityScore >= 80) "健康控支 保持良好" else "建议关注 适当调控",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (rationalityScore >= 80) IncomeGreen else ExpenseRed,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Anomaly Highlights if any
            if (anomalies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "检测到消费提醒事项",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        anomalies.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }
            }

            // Online AI Insight Response Text
            if (!onlineInsightText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "联网大模型实时财务诊断结果",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = onlineInsightText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
