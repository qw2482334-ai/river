package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.TransactionType
import com.example.ui.theme.ExpenseRed

@Composable
fun CategoryBudgetSection(
    expenses: List<ExpenseEntity>,
    categoryBudgets: Map<String, Double>,
    onSetCategoryBudget: (category: String, budget: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    // Calculate spending per category
    val categorySpentMap = remember(expenses) {
        expenses.filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val expenseCategories = remember { ExpenseCategory.Categories.filter { !it.isIncome } }

    // Find warning/alert categories
    val alertCategories = remember(categorySpentMap, categoryBudgets) {
        expenseCategories.mapNotNull { cat ->
            val spent = categorySpentMap[cat.name] ?: 0.0
            val budget = categoryBudgets[cat.name] ?: cat.defaultMonthlyBudget
            if (budget > 0) {
                val ratio = spent / budget
                if (ratio >= 1.0) {
                    Triple(cat, spent, budget to "超限")
                } else if (ratio >= 0.8) {
                    Triple(cat, spent, budget to "预警")
                } else null
            } else null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_budget_section_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Section Header
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "分类支出预算与预警",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "为不同消费品类独立设限并实时监控",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (alertCategories.isNotEmpty()) {
                    Surface(
                        color = ExpenseRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${alertCategories.size}项需关注",
                                style = MaterialTheme.typography.labelMedium,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Global Category Alert Notification Banner if any alert exists
            AnimatedVisibility(visible = alertCategories.isNotEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = ExpenseRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "预警通知",
                                tint = ExpenseRed,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "🚨 分类预算预警通知",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                alertCategories.forEach { (cat, spent, pair) ->
                                    val (budget, status) = pair
                                    val pct = (spent / budget * 100).toInt()
                                    val text = if (status == "超限") {
                                        "• 【${cat.name}】已超出限额！已支出 ￥${String.format("%.1f", spent)} (额度 ￥${budget.toInt()})"
                                    } else {
                                        "• 【${cat.name}】已使用 ${pct}%！接近限额 (￥${String.format("%.1f", spent)} / ￥${budget.toInt()})"
                                    }
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Items Progress List
            expenseCategories.forEach { cat ->
                val spent = categorySpentMap[cat.name] ?: 0.0
                val budget = categoryBudgets[cat.name] ?: cat.defaultMonthlyBudget
                val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
                val isExceeded = budget > 0 && spent >= budget
                val isApproaching = budget > 0 && !isExceeded && spent >= budget * 0.8

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cat.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.name,
                            tint = cat.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (isExceeded) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🚨 超额",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (isApproaching) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "⚠️ 预警",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "￥${String.format("%.0f", spent)} / ￥${budget.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isExceeded) ExpenseRed else if (isApproaching) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { editingCategory = cat },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "修改【${cat.name}】预算",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                isExceeded -> ExpenseRed
                                isApproaching -> Color(0xFFE65100)
                                else -> cat.color
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Edit Category Budget Dialog
    editingCategory?.let { cat ->
        var inputBudget by remember {
            mutableStateOf((categoryBudgets[cat.name] ?: cat.defaultMonthlyBudget).toInt().toString())
        }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        tint = cat.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("设置【${cat.name}】支出预算", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "当该分类当月总支出达到预算的 80% 或超出时，将自动向您发出 UI 预警通知。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputBudget,
                        onValueChange = { inputBudget = it },
                        label = { Text("${cat.name}月度限额 (元)") },
                        prefix = { Text("￥ ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newB = inputBudget.toDoubleOrNull() ?: (categoryBudgets[cat.name] ?: cat.defaultMonthlyBudget)
                        if (newB >= 0) {
                            onSetCategoryBudget(cat.name, newB)
                            editingCategory = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("保存设置")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingCategory = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("取消")
                }
            }
        )
    }
}
