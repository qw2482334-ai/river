package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AiConfigManager
import com.example.data.GeminiService
import kotlinx.coroutines.launch

@Composable
fun AiSettingsDialog(
    aiConfigManager: AiConfigManager,
    geminiService: GeminiService,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf(aiConfigManager.getApiKey()) }
    var baseUrl by remember { mutableStateOf(aiConfigManager.getBaseUrl()) }
    var modelName by remember { mutableStateOf(aiConfigManager.getModelName()) }
    var protocolType by remember { mutableStateOf(aiConfigManager.getProtocolType()) }
    var selectedPreset by remember { mutableStateOf(aiConfigManager.getProvider()) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }
    var isSavedNoticeVisible by remember { mutableStateOf(false) }

    fun applyPreset(presetKey: String) {
        selectedPreset = presetKey
        when (presetKey) {
            "GEMINI" -> {
                baseUrl = "https://generativelanguage.googleapis.com/"
                modelName = "gemini-1.5-flash"
                protocolType = "GEMINI"
            }
            "DEEPSEEK" -> {
                baseUrl = "https://api.deepseek.com/"
                modelName = "deepseek-chat"
                protocolType = "OPENAI"
            }
            "SILICONFLOW" -> {
                baseUrl = "https://api.siliconflow.cn/v1/"
                modelName = "deepseek-ai/DeepSeek-V3"
                protocolType = "OPENAI"
            }
            "QWEN" -> {
                baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/"
                modelName = "qwen-turbo"
                protocolType = "OPENAI"
            }
            "KIMI" -> {
                baseUrl = "https://api.moonshot.cn/v1/"
                modelName = "moonshot-v1-8k"
                protocolType = "OPENAI"
            }
            "CUSTOM" -> {
                protocolType = "OPENAI"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("ai_settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // --- 1. Fixed Header Bar ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "自定义 AI & 国内模型配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Text(
                    text = "支持 Google Gemini、DeepSeek、硅基流动、Qwen、Kimi 或免费本地模式",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // --- 2. Scrollable Middle Form Section ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Provider Presets Selection
                    Text(
                        text = "一键选择 AI 服务商 Preset：",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val presets = listOf(
                        Triple("GEMINI", "Google Gemini 官方 (推荐)", "gemini-1.5-flash"),
                        Triple("DEEPSEEK", "DeepSeek 官方 API", "deepseek-chat"),
                        Triple("SILICONFLOW", "硅基流动 SiliconFlow", "DeepSeek-V3"),
                        Triple("QWEN", "阿里通义千问 Qwen", "qwen-turbo"),
                        Triple("KIMI", "Moonshot / Kimi 智能体", "moonshot-v1-8k"),
                        Triple("CUSTOM", "自定义 OpenAI 中转站", "自定义模型")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { (key, label, modelHint) ->
                            val isSelected = selectedPreset == key
                            OutlinedCard(
                                onClick = { applyPreset(key) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { applyPreset(key) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "默认模型: $modelHint", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. API Key Field
                    Text(
                        text = "1. API Key / Token 密钥 (留空即启用免费模式)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        placeholder = { Text("粘贴您的 API Key (如 AIzaSy... / sk-...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "显示/隐藏密钥"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Base URL Field
                    Text(
                        text = "2. API 接口基址 (Base URL)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        placeholder = { Text("例如 https://api.deepseek.com/") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Model Name Field
                    Text(
                        text = "3. AI 模型名称 (Model Name)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        placeholder = { Text("gemini-1.5-flash / deepseek-chat") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Test Connection Button
                    OutlinedButton(
                        onClick = {
                            aiConfigManager.saveApiKey(apiKey)
                            aiConfigManager.saveBaseUrl(baseUrl)
                            aiConfigManager.saveModelName(modelName)
                            aiConfigManager.saveProtocolType(protocolType)
                            aiConfigManager.saveProvider(selectedPreset)

                            isTesting = true
                            testResultText = null

                            coroutineScope.launch {
                                val res = geminiService.testConnection()
                                isTesting = false
                                res.onSuccess { msg ->
                                    isTestSuccess = true
                                    testResultText = msg
                                }.onFailure { err ->
                                    isTestSuccess = false
                                    testResultText = err.message ?: "网络连通失败"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在测试 API 连通状态...")
                        } else {
                            Icon(imageVector = Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📡 测试 API 接口连通状态")
                        }
                    }

                    // Display Test Result Box
                    if (testResultText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = if (isTestSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testResultText ?: "",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isTestSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isSavedNoticeVisible) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎉 AI 配置已成功保存并立即生效！",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // --- 3. Fixed Bottom Action Buttons (Always Visible) ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            aiConfigManager.saveApiKey(apiKey)
                            aiConfigManager.saveBaseUrl(baseUrl)
                            aiConfigManager.saveModelName(modelName)
                            aiConfigManager.saveProtocolType(protocolType)
                            aiConfigManager.saveProvider(selectedPreset)
                            isSavedNoticeVisible = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("保存配置并启用")
                    }
                }
            }
        }
    }
}
