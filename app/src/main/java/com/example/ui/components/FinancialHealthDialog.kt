package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExpenseEntity
import com.example.data.InvestmentItem
import com.example.data.SavingsGoalEntity
import com.example.data.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialHealthDialog(
    expenses: List<ExpenseEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    investments: List<InvestmentItem>,
    onDismissRequest: () -> Unit,
    onAskAiAdvisor: (String) -> Unit = {}
) {
    val totalCashIncome = remember(expenses) { expenses.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount } }
    val totalCashExpense = remember(expenses) { expenses.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount } }
    val cashBalance = totalCashIncome - totalCashExpense

    val totalSavings = remember(savingsGoals) { savingsGoals.sumOf { it.currentAmount } }
    val totalInvestmentsVal = remember(investments) { investments.sumOf { it.currentValue } }
    val liquidAssets = cashBalance.coerceAtLeast(0.0) + totalSavings
    val totalNetWorth = liquidAssets + totalInvestmentsVal

    // Estimate monthly average expense (if expenses span multiple months or fallback to total expenses / 3)
    val monthlyExpenseEstimate = if (totalCashExpense > 0) totalCashExpense / 3.0 else 3000.0
    val emergencyMonths = if (monthlyExpenseEstimate > 0) liquidAssets / monthlyExpenseEstimate else 0.0

    // Savings rate
    val savingsRate = if (totalCashIncome > 0) ((totalCashIncome - totalCashExpense) / totalCashIncome) * 100.0 else 0.0

    // Health Score calculation (0 - 100)
    // Emergency fund score (max 35 pts for >= 6 months)
    val emergencyScore = (emergencyMonths / 6.0 * 35.0).coerceIn(0.0, 35.0)
    // Savings rate score (max 35 pts for >= 30%)
    val savingsScore = (savingsRate / 30.0 * 35.0).coerceIn(0.0, 35.0)
    // Investment participation score (max 30 pts if investments > 0)
    val investmentScore = if (totalInvestmentsVal > 0) 30.0 else 10.0

    val totalHealthScore = (emergencyScore + savingsScore + investmentScore).toInt().coerceIn(0, 100)

    val gradeText = when {
        totalHealthScore >= 85 -> "🟢 优秀 (A+ 级财务健康)"
        totalHealthScore >= 70 -> "🔵 良好 (B 级稳健防御)"
        totalHealthScore >= 50 -> "🟡 亚健康 (C 级需优化现金流)"
        else -> "🔴 高风险警报 (D 级亟需开源节流)"
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("financial_health_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "个人财务健康与应急安全评测",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "应急金月数 · 储蓄率指标 · 资产配置弹性诊断",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Overall Score Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "财务健康综合评分", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$totalHealthScore 分",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gradeText,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (totalHealthScore / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Emergency Fund Security Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🛡️ 应急金安全防线 (Emergency Buffer)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "流动资产 (现金 + 攒钱目标): ￥${String.format("%.2f", liquidAssets)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "估算月均支出: ￥${String.format("%.2f", monthlyExpenseEstimate)} /月",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "抗风险生存能力: 约 ${String.format("%.1f", emergencyMonths)} 个月",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (emergencyMonths >= 6.0) IncomeGreen else if (emergencyMonths >= 3.0) MaterialTheme.colorScheme.primary else ExpenseRed
                            )
                            Text(
                                text = if (emergencyMonths >= 6.0) "✅ 应急资金非常充裕，可抵御失业或突发风险。" else if (emergencyMonths >= 3.0) "⚠️ 应急资金达标（3个月以上），建议向6个月看齐。" else "❌ 应急储备严重不足！建议减少非必要支出，增加现金存款。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Savings Rate & Income/Expense Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "💰 现金流与储蓄率评估",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "总收入", style = MaterialTheme.typography.labelSmall)
                                    Text(text = "￥${String.format("%.2f", totalCashIncome)}", fontWeight = FontWeight.Bold, color = IncomeGreen)
                                }
                                Column {
                                    Text(text = "总支出", style = MaterialTheme.typography.labelSmall)
                                    Text(text = "￥${String.format("%.2f", totalCashExpense)}", fontWeight = FontWeight.Bold, color = ExpenseRed)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "当前储蓄率", style = MaterialTheme.typography.labelSmall)
                                    Text(text = "${String.format("%.1f", savingsRate)}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // Action Button: Ask AI for Financial Health Improvement Plan
                    Button(
                        onClick = {
                            val prompt = "请帮我诊断我的个人财务健康状况：总净资产 ￥${totalNetWorth}，流动资金 ￥${liquidAssets}，抗风险应急月数 ${String.format("%.1f", emergencyMonths)} 个月，储蓄率 ${String.format("%.1f", savingsRate)}%，财务健康综合评分 ${totalHealthScore} 分。请给出全面的理财规划与防险提升建议。"
                            onDismissRequest()
                            onAskAiAdvisor(prompt)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成 AI 专属财务健康与风控提升方案")
                    }
                }
            }
        }
    }
}
