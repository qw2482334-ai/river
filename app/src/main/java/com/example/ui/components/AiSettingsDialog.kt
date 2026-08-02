package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.CustomApiProfile
import com.example.data.GeminiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    // --- Dynamic Model List Fetching States ---
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetchingModels by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    // --- Custom Profile Management States ---
    var customProfiles by remember { mutableStateOf(aiConfigManager.getCustomProfiles()) }
    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var newProfileNameInput by remember { mutableStateOf("") }

    fun autoFetchModels(url: String, key: String, protocol: String) {
        if (url.isBlank()) return
        // First load from local persistent cache if available
        val cached = aiConfigManager.getCachedModelList(url)
        if (cached.isNotEmpty()) {
            availableModels = cached
        }
        isFetchingModels = true
        coroutineScope.launch {
            val result = geminiService.fetchAvailableModels(url, key, protocol)
            isFetchingModels = false
            result.onSuccess { models ->
                if (models.isNotEmpty()) {
                    availableModels = models
                    aiConfigManager.saveCachedModelList(url, models)
                    if (modelName.isBlank() || !models.contains(modelName)) {
                        modelName = models.first()
                    }
                }
            }
        }
    }

    // Auto fetch models on open
    LaunchedEffect(Unit) {
        autoFetchModels(baseUrl, apiKey, protocolType)
    }

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
            "NVIDIA" -> {
                baseUrl = "https://integrate.api.nvidia.com/v1/"
                modelName = "meta/llama-3.1-405b-instruct"
                protocolType = "OPENAI"
            }
            "APIMART_1" -> {
                baseUrl = "https://api.apimart.ai/v1/"
                modelName = "gpt-4o"
                protocolType = "OPENAI"
            }
            "APIMART_2" -> {
                baseUrl = "https://api.apib.ai/v1/"
                modelName = "gpt-4o"
                protocolType = "OPENAI"
            }
            "APIMART_3" -> {
                baseUrl = "https://api.aiuxu.com/v1/"
                modelName = "gpt-4o"
                protocolType = "OPENAI"
            }
            "APIMART_4" -> {
                baseUrl = "https://api.aishuch.com/v1/"
                modelName = "gpt-4o"
                protocolType = "OPENAI"
            }
            "CUSTOM" -> {
                protocolType = "OPENAI"
            }
        }
        autoFetchModels(baseUrl, apiKey, protocolType)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
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
                            text = "自定义 AI & 模型搜索配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Text(
                    text = "填写 Base URL 自动检索可调用的大模型，并支持增加管理多个自定义 OpenAI 接口",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "网络状态: 已具备 INTERNET 联网权限与 HTTP/HTTPS 实时通信能力",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

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
                        Triple("NVIDIA", "英伟达 NVIDIA NIM API", "meta/llama-3.1-405b-instruct"),
                        Triple("APIMART_1", "Apimart 中转 1 (api.apimart.ai)", "gpt-4o / deepseek"),
                        Triple("APIMART_2", "Apimart 中转 2 (api.apib.ai)", "gpt-4o / deepseek"),
                        Triple("APIMART_3", "Apimart 中转 3 (api.aiuxu.com)", "gpt-4o / deepseek"),
                        Triple("APIMART_4", "Apimart 中转 4 (api.aishuch.com)", "gpt-4o / deepseek"),
                        Triple("CUSTOM", "自定义 OpenAI 中转站", "自动拉取可用模型")
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

                    // --- Custom OpenAI Multi-Profile Management ---
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚙️ 我的自定义 OpenAI 接口库 (${customProfiles.size}个)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                newProfileNameInput = "自定义接口 ${customProfiles.size + 1}"
                                showSaveProfileDialog = true
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("增加新接口配置", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (customProfiles.isEmpty()) {
                        Text(
                            text = "尚未保存自定义接口。在下方填写 Base URL 及 API Key 后，点击上方「增加新接口配置」可无限保存多个中转站并随时切换！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            customProfiles.forEach { profile ->
                                val isActive = selectedPreset == "CUSTOM_${profile.id}"
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = profile.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = profile.baseUrl,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "模型: ${profile.modelName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Button(
                                                onClick = {
                                                    aiConfigManager.applyCustomProfile(profile)
                                                    selectedPreset = "CUSTOM_${profile.id}"
                                                    baseUrl = profile.baseUrl
                                                    apiKey = profile.apiKey
                                                    modelName = profile.modelName
                                                    protocolType = profile.protocolType
                                                    autoFetchModels(profile.baseUrl, profile.apiKey, profile.protocolType)
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(if (isActive) "已激活" else "切换启用", style = MaterialTheme.typography.labelMedium)
                                            }
                                            IconButton(
                                                onClick = {
                                                    aiConfigManager.deleteCustomProfile(profile.id)
                                                    customProfiles = aiConfigManager.getCustomProfiles()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "删除接口",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
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
                        onValueChange = {
                            apiKey = it
                        },
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

                    // 2. Base URL Field with Search Trigger
                    Text(
                        text = "2. API 接口基址 (Base URL)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = {
                            baseUrl = it
                            if (it.startsWith("http://") || it.startsWith("https://")) {
                                autoFetchModels(it, apiKey, protocolType)
                            }
                        },
                        placeholder = { Text("例如 https://api.deepseek.com/") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { autoFetchModels(baseUrl, apiKey, protocolType) }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "检索模型",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Model Name Field with Auto-List Selection Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. AI 模型名称 (已从 Base URL 自动检索列表)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { autoFetchModels(baseUrl, apiKey, protocolType) },
                            enabled = !isFetchingModels
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("刷新模型列表", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (isFetchingModels) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在通过该接口地址拉取所有可用 AI 模型列表...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = isModelDropdownExpanded,
                        onExpandedChange = { isModelDropdownExpanded = !isModelDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            placeholder = { Text("点击右侧下拉或手动输入") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = isModelDropdownExpanded,
                            onDismissRequest = { isModelDropdownExpanded = false }
                        ) {
                            if (availableModels.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("（点击此处或输入文字来自定义模型名称）") },
                                    onClick = { isModelDropdownExpanded = false }
                                )
                            } else {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = model,
                                                    fontWeight = if (model == modelName) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (model == modelName) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        },
                                        onClick = {
                                            modelName = model
                                            isModelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Quick Chips for Available Models
                    if (availableModels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("快捷点击选择已检索到的模型：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableModels.take(8).forEach { model ->
                                FilterChip(
                                    selected = model == modelName,
                                    onClick = { modelName = model },
                                    label = { Text(model, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

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

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Developer Information Section
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "开发者信息 (Developer Info)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "👨‍💻 开发者代号/昵称：river_wzh",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🚀 系统版本：v2026.1.0 智能理财与全景资产管理系统",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "🛡️ 架构模式：Jetpack Compose + Room + Coroutines + Multi-Engine AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // --- 3. Fixed Bottom Action Buttons (Always Visible) ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            newProfileNameInput = "自定义接口 ${customProfiles.size + 1}"
                            showSaveProfileDialog = true
                        }
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("存为新接口", style = MaterialTheme.typography.labelMedium)
                    }

                    Row {
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

    // Save New Custom API Profile Dialog
    if (showSaveProfileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProfileDialog = false },
            title = { Text("保存自定义 API 接口配置") },
            text = {
                Column {
                    Text("请为此接口（例如：DeepSeek专线、公司OneAPI中转站）命名：", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProfileNameInput,
                        onValueChange = { newProfileNameInput = it },
                        label = { Text("接口名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileNameInput.isNotBlank()) {
                            val newProfile = CustomApiProfile(
                                name = newProfileNameInput.trim(),
                                baseUrl = baseUrl,
                                apiKey = apiKey,
                                modelName = modelName,
                                protocolType = protocolType
                            )
                            aiConfigManager.addCustomProfile(newProfile)
                            aiConfigManager.applyCustomProfile(newProfile)
                            selectedPreset = "CUSTOM_${newProfile.id}"
                            customProfiles = aiConfigManager.getCustomProfiles()
                            showSaveProfileDialog = false
                        }
                    }
                ) {
                    Text("保存接口")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProfileDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

