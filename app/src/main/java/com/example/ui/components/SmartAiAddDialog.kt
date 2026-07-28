package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ParsedExpense

@Composable
fun SmartAiAddDialog(
    isAiLoading: Boolean,
    onParseText: (String, (ParsedExpense) -> Unit) -> Unit,
    onParseReceiptImage: (String, (ParsedExpense) -> Unit) -> Unit,
    onConfirmExpense: (ParsedExpense) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var parsedExpenseResult by remember { mutableStateOf<ParsedExpense?>(null) }

    // Sample Base64 image placeholder for receipt OCR demo
    val sampleReceiptBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

    val presetPromptExamples = listOf(
        "昨天晚上跟同事吃海底捞花了280元",
        "今天公司发工资12000元",
        "打车去机场花了85块钱",
        "网购买服饰花费199元"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("AI 智能极速记账", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("说出或粘贴一句话，自动识别提取", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        parsedExpenseResult = null
                    },
                    placeholder = { Text("例如：昨天中午吃火锅花了260元...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (inputText.isBlank()) {
                                inputText = presetPromptExamples.random()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "示例/语音输入",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "💡 试一试快速输入例句：",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetPromptExamples.take(2).forEach { example ->
                        TextButton(
                            onClick = {
                                inputText = example
                                parsedExpenseResult = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(example, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        onParseReceiptImage(sampleReceiptBase64) { parsed ->
                            parsedExpenseResult = parsed
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAiLoading
                ) {
                    Text("📷 识别小票/账单图片（Gemini Vision 多模态）", fontSize = 12.sp)
                }

                if (isAiLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Gemini AI 正在智能解析语义...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                parsedExpenseResult?.let { parsed ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✅ 识别结果确认", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                Text(parsed.type, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (parsed.type == "收入") Color(0xFF047857) else Color(0xFFB91C1C))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• 金额: ￥${parsed.amount}", style = MaterialTheme.typography.bodyMedium)
                            Text("• 类别: ${parsed.category}", style = MaterialTheme.typography.bodyMedium)
                            Text("• 日期: ${parsed.date}", style = MaterialTheme.typography.bodyMedium)
                            Text("• 备注: ${parsed.note}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (parsedExpenseResult != null) {
                Button(
                    onClick = {
                        parsedExpenseResult?.let {
                            onConfirmExpense(it)
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("确认添加该笔账目")
                }
            } else {
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onParseText(inputText) { parsed ->
                                parsedExpenseResult = parsed
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isAiLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI 识别提取")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
