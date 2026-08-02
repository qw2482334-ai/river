package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.LotteryRecord
import com.example.data.LotteryStatus
import com.example.data.LotteryType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryAnalyticsDialog(
    lotteryRecords: List<LotteryRecord>,
    onDismissRequest: () -> Unit,
    onAskAiAdvisor: (String) -> Unit = {}
) {
    // Kelly Calculator inputs
    var inputOdds by remember { mutableStateOf("2.10") }
    var inputProbabilityPercent by remember { mutableStateOf("52") }

    val odds = inputOdds.toDoubleOrNull()?.takeIf { it > 1.0 } ?: 2.10
    val prob = (inputProbabilityPercent.toDoubleOrNull()?.coerceIn(1.0, 99.0) ?: 52.0) / 100.0

    // Kelly Formula: f = (p * b - 1) / (b - 1)
    val kellyFraction = ((prob * odds) - 1.0) / (odds - 1.0)
    val kellyPercent = (kellyFraction * 100).coerceAtLeast(0.0)

    // Statistics from existing tickets
    val totalBets = lotteryRecords.size
    val totalWagered = remember(lotteryRecords) { lotteryRecords.sumOf { it.betAmount } }
    val totalWonAmount = remember(lotteryRecords) { lotteryRecords.filter { it.status == LotteryStatus.WON }.sumOf { it.winAmount } }
    val netProfit = totalWonAmount - totalWagered
    val roiPercent = if (totalWagered > 0) (netProfit / totalWagered) * 100 else 0.0

    val wonCount = remember(lotteryRecords) { lotteryRecords.count { it.status == LotteryStatus.WON } }
    val lostCount = remember(lotteryRecords) { lotteryRecords.count { it.status == LotteryStatus.LOST } }
    val pendingCount = remember(lotteryRecords) { lotteryRecords.count { it.status == LotteryStatus.PENDING } }
    val settledCount = wonCount + lostCount
    val hitRatePercent = if (settledCount > 0) (wonCount.toDouble() / settledCount) * 100 else 0.0

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("lottery_analytics_dialog"),
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
                                    imageVector = Icons.Default.SportsFootball,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "足彩与彩票数据复盘与风控分析",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "胜率与ROI率 · 凯利公式资金管理 · 投注盈亏模型",
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
                    // Overall Hit Rate & Net ROI Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎯 历史战绩、命中率与 ROI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "累计红单胜率", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = "${String.format("%.1f", hitRatePercent)}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "红单 $wonCount | 黑单 $lostCount | 未开 $pendingCount",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "净盈亏 ROI 收益率", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = "${if (netProfit >= 0) "+" else ""}￥${String.format("%.2f", netProfit)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netProfit >= 0) ExpenseRed else IncomeGreen
                                    )
                                    Text(
                                        text = "投资回报率: ${if (roiPercent >= 0) "+" else ""}${String.format("%.1f", roiPercent)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (roiPercent >= 0) ExpenseRed else IncomeGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { (hitRatePercent / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Kelly Criterion Calculator for Sports Betting
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📐 凯利公式 (Kelly Criterion) 仓位计算器",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "科学计算最佳下注仓位比例，避免重仓破产风险与追热上头。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = inputOdds,
                                    onValueChange = { inputOdds = it },
                                    label = { Text("比赛赔率 (如2.10)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = inputProbabilityPercent,
                                    onValueChange = { inputProbabilityPercent = it },
                                    label = { Text("预估胜率 (如52%)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = if (kellyFraction > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (kellyFraction > 0) "✅ 建议单场下注上限: ${String.format("%.1f", kellyPercent)}% 总本金" else "⚠️ 负期望值 (EV < 0)！建议放弃或观望，不宜下注",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (kellyFraction > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "半凯利 (Half-Kelly) 建议仓位: ${String.format("%.1f", (kellyPercent / 2.0).coerceAtLeast(0.0))}%（更加稳健）",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (kellyFraction > 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Betting Category Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📊 彩种类型与投注结构",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LotteryType.entries.forEach { type ->
                                val listForType = lotteryRecords.filter { it.type == type }
                                val wagerForType = listForType.sumOf { it.betAmount }
                                val wonForType = listForType.filter { it.status == LotteryStatus.WON }.sumOf { it.winAmount }
                                val pForType = wonForType - wagerForType

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = type.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "共 ${listForType.size} 笔 | 投注 ￥${String.format("%.0f", wagerForType)} | 净盈亏 ￥${String.format("%.1f", pForType)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pForType >= 0) ExpenseRed else IncomeGreen
                                    )
                                }
                            }
                        }
                    }

                    // Button: Ask AI to Review Lottery Bets
                    Button(
                        onClick = {
                            val prompt = "请帮我复盘目前的足彩/彩票投注记录：累计投注 ${totalBets} 笔，投入 ￥${totalWagered}，中奖派彩 ￥${totalWonAmount}，胜率 ${String.format("%.1f", hitRatePercent)}%。请给出战术复盘、心态风控指导与止损止盈规划。"
                            onDismissRequest()
                            onAskAiAdvisor(prompt)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成 AI 竞彩复盘与赔率资金策略分析")
                    }
                }
            }
        }
    }
}
