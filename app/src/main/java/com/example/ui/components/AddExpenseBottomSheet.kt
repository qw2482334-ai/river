package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    ledgers: List<String>,
    currentLedger: String,
    onDismiss: () -> Unit,
    onSaveExpense: (ExpenseEntity) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("餐饮") }
    var selectedLedgerName by remember { mutableStateOf(if (currentLedger == "全部账本") "日常账本" else currentLedger) }
    var noteText by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }

    // Categories filter based on EXPENSE or INCOME
    val availableCategories = ExpenseCategory.Categories.filter {
        it.isIncome == (selectedType == TransactionType.INCOME)
    }

    LaunchedEffect(selectedType) {
        selectedCategory = availableCategories.firstOrNull()?.name ?: "餐饮"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .testTag("add_expense_bottom_sheet")
        ) {
            Text(
                text = "记一笔新账单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Type Switcher
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ExpenseRed.copy(alpha = 0.2f),
                        activeContentColor = ExpenseRed
                    )
                ) {
                    Text("支出 ↗", fontWeight = FontWeight.Bold)
                }

                SegmentedButton(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = IncomeGreen.copy(alpha = 0.2f),
                        activeContentColor = IncomeGreen
                    )
                ) {
                    Text("收入 ↙", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("金额 (元)") },
                placeholder = { Text("0.00") },
                prefix = { Text("￥ ", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title Field
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("账单名称 / 商户") },
                placeholder = { Text("例如：星巴克咖啡 / 7月份工资") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selection Grid
            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCategories) { cat ->
                    val isSelected = cat.name == selectedCategory
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) cat.color.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { selectedCategory = cat.name }
                            .padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) cat.color else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = cat.name,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ledger Selector
            Text(
                text = "归属账本",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ledgers.filter { it != "全部账本" }.forEach { ledger ->
                    FilterChip(
                        selected = ledger == selectedLedgerName,
                        onClick = { selectedLedgerName = ledger },
                        label = { Text(ledger) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note & Recurring
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("备注 (可选)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "设为每月固定周期账单", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountVal > 0) {
                        onSaveExpense(
                            ExpenseEntity(
                                title = titleText.ifBlank { selectedCategory },
                                amount = amountVal,
                                type = selectedType.name,
                                category = selectedCategory,
                                ledgerName = selectedLedgerName,
                                note = noteText,
                                isRecurring = isRecurring
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = amountVal > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "保存账单", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
