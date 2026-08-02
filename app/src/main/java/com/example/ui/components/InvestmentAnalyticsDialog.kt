package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.InvestmentItem
import com.example.data.InvestmentType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentAnalyticsDialog(
    investments: List<InvestmentItem>,
    onDismissRequest: () -> Unit,
    onAskAiAdvisor: (String) -> Unit = {}
) {
    var holdingMonthsInput by remember { mutableStateOf("6") }
    
    val totalPrincipal = remember(investments) { investments.sumOf { it.principal } }
    val totalCurrentValue = remember(investments) { investments.sumOf { it.currentValue } }
    val totalProfitLoss = totalCurrentValue - totalPrincipal
    val overallRoi = if (totalPrincipal > 0) (totalProfitLoss / totalPrincipal) * 100 else 0.0

    // Holding months calculation
    val months = holdingMonthsInput.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 6.0
    val annualizedReturnRate = if (totalPrincipal > 0) {
        val holdingReturn = totalCurrentValue / totalPrincipal
        // Compound annualized formula
        (Math.pow(holdingReturn, 12.0 / months) - 1.0) * 100
    } else 0.0

    // Asset allocation by Type
    val allocationMap = remember(investments) {
        investments.groupBy { it.type }
            .mapValues { entry -> entry.value.sumOf { it.currentValue } }
    }

    // Top performer & worst performer
    val topPerformer = remember(investments) {
        investments.maxByOrNull { if (it.principal > 0) (it.currentValue - it.principal) / it.principal else 0.0 }
    }
    val worstPerformer = remember(investments) {
        investments.minByOrNull { if (it.principal > 0) (it.currentValue - it.principal) / it.principal else 0.0 }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("investment_analytics_dialog"),
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
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "证券理财与持仓深度分析",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "资产配置比例 · 年化收益率推演 · 风险集中度",
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
                    // Overall ROI & Annualized Yield Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📈 收益率与年化回报推演",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "持仓累计盈亏", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = "${if (totalProfitLoss >= 0) "+" else ""}￥${String.format("%.2f", totalProfitLoss)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalProfitLoss >= 0) ExpenseRed else IncomeGreen
                                    )
                                    Text(
                                        text = "绝对收益率: ${if (overallRoi >= 0) "+" else ""}${String.format("%.2f", overallRoi)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (overallRoi >= 0) ExpenseRed else IncomeGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "折算年化收益率", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = "${if (annualizedReturnRate >= 0) "+" else ""}${String.format("%.2f", annualizedReturnRate)}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (annualizedReturnRate >= 0) ExpenseRed else IncomeGreen
                                    )
                                    Text(
                                        text = "基于 ${months.toInt()} 个月持仓期估算",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Holding period duration field
                            OutlinedTextField(
                                value = holdingMonthsInput,
                                onValueChange = { holdingMonthsInput = it },
                                label = { Text("假设持仓时长 (月)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    // Asset Allocation Visual Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📊 资产类型分布与风险暴露",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (totalCurrentValue <= 0) {
                                Text("暂无有效持仓市值", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                InvestmentType.entries.forEach { type ->
                                    val valForType = allocationMap[type] ?: 0.0
                                    val percentage = (valForType / totalCurrentValue) * 100.0

                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = type.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "￥${String.format("%.2f", valForType)} (${String.format("%.1f", percentage)}%)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = when (type) {
                                                InvestmentType.STOCK -> MaterialTheme.colorScheme.error
                                                InvestmentType.FUND -> MaterialTheme.colorScheme.primary
                                                InvestmentType.WEALTH -> MaterialTheme.colorScheme.tertiary
                                                InvestmentType.CRYPTO -> Color(0xFF9C27B0)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Winner vs Loser Product Comparison Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🏆 持仓极值诊断与贡献率",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Winner
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🥇 最佳盈利持仓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = topPerformer?.name ?: "无",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (topPerformer != null && topPerformer.principal > 0) {
                                        val p = topPerformer.currentValue - topPerformer.principal
                                        val rate = (p / topPerformer.principal) * 100
                                        Text(
                                            text = "${if (p >= 0) "+" else ""}￥${String.format("%.2f", p)} (${String.format("%.1f", rate)}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ExpenseRed
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier
                                        .height(45.dp)
                                        .width(1.dp)
                                        .padding(horizontal = 8.dp)
                                )

                                // Loser
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📉 较大亏损持仓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = worstPerformer?.name ?: "无",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (worstPerformer != null && worstPerformer.principal > 0) {
                                        val p = worstPerformer.currentValue - worstPerformer.principal
                                        val rate = (p / worstPerformer.principal) * 100
                                        Text(
                                            text = "${if (p >= 0) "+" else ""}￥${String.format("%.2f", p)} (${String.format("%.1f", rate)}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Button: Ask AI for Rebalancing Advice
                    Button(
                        onClick = {
                            val prompt = "请分析我的理财持仓总额 ￥${totalCurrentValue}，投入本金 ￥${totalPrincipal}，累计盈亏 ￥${totalProfitLoss}。资产分布包含 ${allocationMap.map { "${it.key.label}: ￥${it.value}" }.joinToString()}。请给出资产配置再平衡与风险调仓建议。"
                            onDismissRequest()
                            onAskAiAdvisor(prompt)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成 AI 智能理财调仓与风控建议")
                    }
                }
            }
        }
    }
}
