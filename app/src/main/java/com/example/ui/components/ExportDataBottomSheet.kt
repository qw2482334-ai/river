package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExpenseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataBottomSheet(
    expenses: List<ExpenseEntity>,
    onImportCsv: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExported by remember { mutableStateOf(false) }

    var pendingExportAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    fun generateCsv(): String {
        val sb = StringBuilder()
        sb.append("ID,账单名称,金额,类型,分类,账本,日期,备注\n")
        expenses.forEach { exp ->
            sb.append("${exp.id},\"${exp.title}\",${exp.amount},${exp.type},${exp.category},${exp.ledgerName},${exp.dateMillis},\"${exp.note}\"\n")
        }
        return sb.toString()
    }

    fun shareCsvData(csv: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csv)
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "导出/分享账单数据")
        context.startActivity(shareIntent)
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingExportAction = null
            },
            title = {
                Text("确认导出账单备份数据？", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("即将全量导出当前筛选的 ${expenses.size} 条账单数据，并生成 CSV 表格与 Room 数据备份。是否确认继续？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        pendingExportAction?.invoke()
                        pendingExportAction = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("确认导出")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmDialog = false
                        pendingExportAction = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("取消")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
                .testTag("export_data_bottom_sheet")
        ) {
            Text(
                text = "导入与导出账单数据",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "共有 ${expenses.size} 条账单记录，可复制为 CSV 或调用系统分享发送",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "📄 CSV 数据文本预览：", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = generateCsv().take(250) + if (expenses.size > 3) "\n..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isExported) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✅ 完整 CSV 表格数据已复制到系统剪贴板！",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        pendingExportAction = {
                            val csv = generateCsv()
                            clipboardManager.setText(AnnotatedString(csv))
                            isExported = true
                        }
                        showConfirmDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("复制 CSV 文本")
                }

                FilledTonalButton(
                    onClick = {
                        pendingExportAction = {
                            val csv = generateCsv()
                            shareCsvData(csv)
                        }
                        showConfirmDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("系统分享/导出")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("关闭")
            }
        }
    }
}
