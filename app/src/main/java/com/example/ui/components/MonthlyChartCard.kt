package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthlyChartCard(
    expenses: List<ExpenseEntity>,
    modifier: Modifier = Modifier
) {
    var selectedChartType by remember { mutableStateOf(0) } // 0: 支出结构, 1: 趋势对比

    // Group expenses by category
    val expenseList = expenses.filter { it.type == TransactionType.EXPENSE.name }
    val totalExpense = expenseList.sumOf { it.amount }

    val categoryTotals = expenseList
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "图表与支出分析",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = selectedChartType == 0,
                        onClick = { selectedChartType = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("分类占比", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = selectedChartType == 1,
                        onClick = { selectedChartType = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("七日趋势", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedChartType == 0) {
                // Category breakdown progress bars
                if (categoryTotals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无支出记录，快去记一笔吧~", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categoryTotals.take(6).forEach { (catName, amount) ->
                            val catObj = ExpenseCategory.getCategoryByName(catName)
                            val percent = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(catObj.color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = catObj.icon,
                                                contentDescription = null,
                                                tint = catObj.color,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = catName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }

                                    Text(
                                        text = "￥${String.format("%.2f", amount)} (${(percent * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { percent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = catObj.color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // 7-day Bar Chart
                val last7DaysData = remember(expenses) {
                    val cal = Calendar.getInstance()
                    val result = mutableListOf<Pair<String, Double>>()
                    val dayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

                    for (i in 6 downTo 0) {
                        val tempCal = Calendar.getInstance()
                        tempCal.add(Calendar.DAY_OF_YEAR, -i)
                        tempCal.set(Calendar.HOUR_OF_DAY, 0)
                        tempCal.set(Calendar.MINUTE, 0)
                        tempCal.set(Calendar.SECOND, 0)
                        tempCal.set(Calendar.MILLISECOND, 0)
                        val startMs = tempCal.timeInMillis

                        tempCal.add(Calendar.DAY_OF_YEAR, 1)
                        val endMs = tempCal.timeInMillis

                        val dayTotal = expenses.filter {
                            it.type == TransactionType.EXPENSE.name &&
                                    it.dateMillis in startMs..<endMs
                        }.sumOf { it.amount }

                        result.add(Pair(dayFormat.format(Date(startMs)), dayTotal))
                    }
                    result
                }

                val maxVal = (last7DaysData.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(100.0)

                Column {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = size.width / (last7DaysData.size * 2)
                        val maxBarHeight = size.height - 30.dp.toPx()

                        last7DaysData.forEachIndexed { index, pair ->
                            val x = (index * 2 + 0.5f) * barWidth
                            val barHeight = ((pair.second / maxVal) * maxBarHeight).toFloat()
                            val y = maxBarHeight - barHeight

                            // Background bar
                            drawRoundRect(
                                color = trackColor,
                                topLeft = Offset(x, 0f),
                                size = Size(barWidth, maxBarHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )

                            // Active value bar
                            if (barHeight > 0) {
                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        last7DaysData.forEach { (dateStr, _) ->
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
