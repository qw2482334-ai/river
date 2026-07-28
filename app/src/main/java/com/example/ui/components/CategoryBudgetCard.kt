package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MonthlySummary
import com.example.ui.model.ExpenseCategory

@Composable
fun CategoryBudgetCard(
    summary: MonthlySummary,
    onUpdateCategoryBudget: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var inputLimitText by remember { mutableStateOf("") }

    val categoryTotals = summary.categoryTotals
    val categoryBudgets = summary.categoryBudgets

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "📊 分类预算管控与预警",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "针对重点类别设置独立额度，防超支提醒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExpenseCategory.expenseCategories.take(5).forEach { category ->
                    val spent = categoryTotals[category] ?: 0.0
                    val budgetLimit = categoryBudgets[category.title] ?: 1000.0
                    val progress = if (budgetLimit > 0) (spent / budgetLimit).coerceIn(0.0, 1.0).toFloat() else 0f
                    val isOver = spent > budgetLimit

                    val statusColor = when {
                        isOver -> Color(0xFFEF4444)
                        progress > 0.8f -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = category.emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isOver) "⚠️ 超支 ￥${String.format("%.0f", spent - budgetLimit)}" else "已用 ￥${String.format("%.0f", spent)} / ￥${String.format("%.0f", budgetLimit)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )

                                    IconButton(
                                        onClick = {
                                            editingCategory = category
                                            inputLimitText = String.format("%.0f", budgetLimit)
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "修改", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("修改【${cat.title}】预算上限", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputLimitText,
                    onValueChange = { inputLimitText = it },
                    label = { Text("分类预算上限 (元)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = inputLimitText.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onUpdateCategoryBudget(cat.title, parsed)
                            editingCategory = null
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("取消")
                }
            }
        )
    }
}
