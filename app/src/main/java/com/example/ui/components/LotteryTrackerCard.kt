package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.LotteryRecord
import com.example.data.LotteryStatus
import com.example.data.LotteryType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryTrackerCard(
    lotteryRecords: List<LotteryRecord>,
    onAddRecord: (LotteryRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onUpdateRecordStatus: (String, LotteryStatus, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val totalBet = remember(lotteryRecords) { lotteryRecords.sumOf { it.betAmount } }
    val totalWin = remember(lotteryRecords) { lotteryRecords.sumOf { it.winAmount } }
    val netProfit = totalWin - totalBet
    val roi = if (totalBet > 0) (netProfit / totalBet) * 100 else 0.0

    // Risk control check: if net profit is significantly negative or betting frequency high
    val isHighRisk = totalBet >= 1000.0 || netProfit < -300.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lottery_tracker_card"),
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
                            imageVector = Icons.Default.SportsFootball,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "足彩与彩票娱乐记账",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "竞彩/大乐透投注统计与理性风控",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新增注单",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Banner
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "总投注额",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "￥${String.format("%.2f", totalBet)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "总派奖/中奖: ￥${String.format("%.2f", totalWin)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "净盈亏 (ROI)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "${if (netProfit >= 0) "+" else ""}￥${String.format("%.2f", netProfit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) IncomeGreen else ExpenseRed
                        )
                        Text(
                            text = "回报率: ${if (roi >= 0) "+" else ""}${String.format("%.1f", roi)}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }

            // Discipline Warning Banner
            if (isHighRisk) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = ExpenseRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🚨 理性购彩提醒：彩票/竞彩属于高风险娱乐，请严格控制个人预算，切勿盲目追单！",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Records List
            if (lotteryRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无彩票投注记录，点击右上角【+】记一笔",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lotteryRecords.forEach { rec ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = rec.type.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = rec.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "投注: ￥${String.format("%.1f", rec.betAmount)} | 中奖: ￥${String.format("%.1f", rec.winAmount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AssistChip(
                                            onClick = {
                                                val nextStatus = when (rec.status) {
                                                    LotteryStatus.PENDING -> LotteryStatus.WON
                                                    LotteryStatus.WON -> LotteryStatus.LOST
                                                    LotteryStatus.LOST -> LotteryStatus.PENDING
                                                }
                                                val winA = if (nextStatus == LotteryStatus.WON) rec.betAmount * 2.0 else 0.0
                                                onUpdateRecordStatus(rec.id, nextStatus, winA)
                                            },
                                            label = { Text(rec.status.label, style = MaterialTheme.typography.labelSmall) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = when (rec.status) {
                                                    LotteryStatus.WON -> IncomeGreen.copy(alpha = 0.2f)
                                                    LotteryStatus.LOST -> ExpenseRed.copy(alpha = 0.15f)
                                                    LotteryStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                        )

                                        IconButton(
                                            onClick = { onDeleteRecord(rec.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "删除",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Lottery Record Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(LotteryType.FOOTBALL) }
        var betAmountText by remember { mutableStateOf("") }
        var winAmountText by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增彩票/足彩记账", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LotteryType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("比赛/彩种描述 (例: 曼城vs阿森纳)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = betAmountText,
                        onValueChange = { betAmountText = it },
                        label = { Text("投注金额 (元)") },
                        prefix = { Text("￥ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = winAmountText,
                        onValueChange = { winAmountText = it },
                        label = { Text("预期/已中奖金额 (元，开奖后可修改)") },
                        prefix = { Text("￥ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val b = betAmountText.toDoubleOrNull() ?: 0.0
                        val w = winAmountText.toDoubleOrNull() ?: 0.0
                        val st = if (w > 0) LotteryStatus.WON else LotteryStatus.PENDING
                        if (title.isNotBlank() && b > 0) {
                            onAddRecord(
                                LotteryRecord(
                                    title = title,
                                    type = selectedType,
                                    betAmount = b,
                                    winAmount = w,
                                    status = st
                                )
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("保存彩单")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
