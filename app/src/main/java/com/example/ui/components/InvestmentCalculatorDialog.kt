package com.example.ui.components

import androidx.compose.foundation.background
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
fun InvestmentCalculatorDialog(
    onDismissRequest: () -> Unit,
    onSaveToInvestments: (InvestmentItem) -> Unit
) {
    var productName by remember { mutableStateOf("稳健复利理财计划") }
    var selectedType by remember { mutableStateOf(InvestmentType.WEALTH) }
    var principalInput by remember { mutableStateOf("50000") }
    var rateInput by remember { mutableStateOf("6.5") }
    var yearsInput by remember { mutableStateOf("3") }
    var compoundMode by remember { mutableStateOf(true) } // true = compound, false = simple

    val principal = principalInput.toDoubleOrNull() ?: 0.0
    val annualRate = rateInput.toDoubleOrNull() ?: 0.0
    val years = yearsInput.toDoubleOrNull() ?: 1.0

    // Calculate final value & profit
    val rateDecimal = annualRate / 100.0
    val finalValue = if (compoundMode) {
        principal * Math.pow(1.0 + rateDecimal, years)
    } else {
        principal * (1.0 + rateDecimal * years)
    }
    val totalProfit = finalValue - principal
    val roiRate = if (principal > 0) (totalProfit / principal) * 100.0 else 0.0

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("investment_calculator_dialog"),
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
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "理财收益计算器",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "输入本金、年化利率与周期，一键推演复利终值",
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
                    // Result Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "预期到期总资产 (终值)", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "￥${String.format("%.2f", finalValue)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "投入本金", style = MaterialTheme.typography.labelSmall)
                                    Text(text = "￥${String.format("%.2f", principal)}", fontWeight = FontWeight.Bold)
                                }
                                Divider(modifier = Modifier.height(30.dp).width(1.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "预计净收益", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "+￥${String.format("%.2f", totalProfit)}",
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                                Divider(modifier = Modifier.height(30.dp).width(1.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "总回报率 (ROI)", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "+${String.format("%.2f", roiRate)}%",
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                            }
                        }
                    }

                    // Input Form Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📝 计算参数设置",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = productName,
                                onValueChange = { productName = it },
                                label = { Text("理财计划/产品名称") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Type Selector
                            Text(text = "资产类型", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InvestmentType.entries.forEach { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type.label, style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = principalInput,
                                onValueChange = { principalInput = it },
                                label = { Text("投资本金 (￥)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = rateInput,
                                    onValueChange = { rateInput = it },
                                    label = { Text("年化收益率 (%)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = yearsInput,
                                    onValueChange = { yearsInput = it },
                                    label = { Text("投资周期 (年)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            // Compound vs Simple Mode Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "计算模式", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (compoundMode) "复利计息 (利滚利)" else "单利计息 (仅本金生息)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = compoundMode,
                                    onCheckedChange = { compoundMode = it }
                                )
                            }
                        }
                    }

                    // Action: Save to investments & dismiss
                    Button(
                        onClick = {
                            val newItem = InvestmentItem(
                                name = productName.ifBlank { "理财计算计划" },
                                type = selectedType,
                                principal = principal,
                                currentValue = finalValue,
                                notes = "收益计算器生成：年化 ${annualRate}%，周期 ${years} 年，${if (compoundMode) "复利" else "单利"}"
                            )
                            onSaveToInvestments(newItem)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("将计算结果存入理财持仓档案")
                    }
                }
            }
        }
    }
}
