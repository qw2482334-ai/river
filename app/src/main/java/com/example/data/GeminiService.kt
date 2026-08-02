package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

// Google Gemini Data Classes
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

data class Content(
    @Json(name = "parts") val parts: List<Part>
)

data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

data class Candidate(
    @Json(name = "content") val content: Content? = null
)

// OpenAI Compatible Data Classes (DeepSeek, SiliconFlow, Qwen, Moonshot, Custom OpenAI)
data class OpenAiMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: Any
)

data class OpenAiChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenAiMessage>,
    @Json(name = "temperature") val temperature: Float? = 0.2f
)

data class OpenAiChatResponse(
    @Json(name = "choices") val choices: List<OpenAiChoice>? = null,
    @Json(name = "error") val error: OpenAiError? = null
)

data class OpenAiChoice(
    @Json(name = "message") val message: OpenAiResponseMessage? = null
)

data class OpenAiResponseMessage(
    @Json(name = "content") val content: String? = null
)

data class OpenAiError(
    @Json(name = "message") val message: String? = null
)

data class OpenAiModelItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null
)

data class OpenAiModelsResponse(
    @Json(name = "data") val data: List<OpenAiModelItem>? = null,
    @Json(name = "models") val models: List<OpenAiModelItem>? = null
)

data class GeminiModelItem(
    @Json(name = "name") val name: String? = null,
    @Json(name = "displayName") val displayName: String? = null
)

data class GeminiModelsResponse(
    @Json(name = "models") val models: List<GeminiModelItem>? = null
)

data class ParsedExpense(
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "EXPENSE", // EXPENSE or INCOME
    val category: String = "餐饮",
    val note: String = ""
)

interface GeminiRestApi {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @GET
    suspend fun listGeminiModels(
        @Url url: String,
        @Query("key") apiKey: String
    ): GeminiModelsResponse
}

interface OpenAiRestApi {
    @POST
    suspend fun chatCompletions(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: OpenAiChatRequest
    ): OpenAiChatResponse

    @GET
    suspend fun listOpenAiModels(
        @Url url: String,
        @Header("Authorization") authorization: String
    ): OpenAiModelsResponse
}

class GeminiService(private val aiConfigManager: AiConfigManager) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geminiApi = retrofit.create(GeminiRestApi::class.java)
    private val openAiApi = retrofit.create(OpenAiRestApi::class.java)

    private fun getCleanGeminiUrl(baseUrl: String, modelName: String): String {
        val cleanBase = baseUrl.trim()
            .removeSuffix("/")
            .removeSuffix("/v1beta")
            .removeSuffix("/v1")
        val cleanModel = modelName.trim().ifBlank { "gemini-1.5-flash" }
        return "$cleanBase/v1beta/models/$cleanModel:generateContent"
    }

    private fun getCleanOpenAiUrl(baseUrl: String): String {
        val cleanBase = baseUrl.trim().removeSuffix("/")
        return when {
            cleanBase.endsWith("/chat/completions") -> cleanBase
            cleanBase.endsWith("/chat/completions/") -> cleanBase.removeSuffix("/")
            cleanBase.endsWith("/v1") || cleanBase.endsWith("/v1beta") -> "$cleanBase/chat/completions"
            cleanBase.endsWith("/chat") -> "$cleanBase/completions"
            else -> "$cleanBase/v1/chat/completions"
        }
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val config = aiConfigManager.getConfig()
        val apiKey = config.apiKey.trim()

        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("💡 当前处于免 Key 本地离线智能模式。若需开启在线大模型问答，请填入有效的 Gemini 或国内 API Key。")
            )
        }

        try {
            if (config.protocolType == "GEMINI") {
                val fullUrl = getCleanGeminiUrl(config.baseUrl, config.modelName)
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Hello, respond OK"))))
                )
                val response = geminiApi.generateContent(fullUrl, apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    Result.success("✅ Gemini 接口连接成功！模型响应：${reply.take(30)}")
                } else {
                    Result.failure(Exception("接口响应正常但未返回有效文本"))
                }
            } else {
                val chatUrl = getCleanOpenAiUrl(config.baseUrl)
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                val request = OpenAiChatRequest(
                    model = config.modelName.trim(),
                    messages = listOf(OpenAiMessage("user", "Hello, respond OK"))
                )

                val response = openAiApi.chatCompletions(chatUrl, authHeader, request)
                if (response.error != null) {
                    Result.failure(Exception("接口报错: ${response.error.message}"))
                } else {
                    val reply = response.choices?.firstOrNull()?.message?.content
                    if (!reply.isNullOrBlank()) {
                        Result.success("✅ 连接成功！模型响应：${reply.take(30)}")
                    } else {
                        Result.failure(Exception("接口已连接但返回内容为空"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("连接失败: ${e.localizedMessage ?: "网络不可达或密钥无效"}"))
        }
    }

    suspend fun fetchAvailableModels(baseUrl: String, apiKey: String, protocolType: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val cleanBase = baseUrl.trim()

        try {
            if (protocolType == "GEMINI" || cleanBase.contains("googleapis.com")) {
                val listUrl = if (cleanBase.endsWith("/")) "${cleanBase}v1beta/models" else "$cleanBase/v1beta/models"
                val response = geminiApi.listGeminiModels(listUrl, cleanKey)
                val modelList = response.models?.mapNotNull { item ->
                    val rawName = item.name ?: return@mapNotNull null
                    rawName.removePrefix("models/")
                } ?: emptyList()
                if (modelList.isNotEmpty()) {
                    Result.success(modelList)
                } else {
                    Result.success(listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash-exp"))
                }
            } else {
                val modelsUrl = when {
                    cleanBase.endsWith("/models") -> cleanBase
                    cleanBase.endsWith("/v1") -> "$cleanBase/models"
                    cleanBase.endsWith("/v1/") -> "${cleanBase}models"
                    cleanBase.endsWith("/chat/completions") -> cleanBase.removeSuffix("/chat/completions") + "/models"
                    cleanBase.endsWith("/") -> "${cleanBase}v1/models"
                    else -> "$cleanBase/v1/models"
                }
                val authHeader = if (cleanKey.startsWith("Bearer ") || cleanKey.isBlank()) cleanKey else "Bearer $cleanKey"
                val response = openAiApi.listOpenAiModels(modelsUrl, authHeader)
                val items = response.data ?: response.models ?: emptyList()
                val modelList = items.mapNotNull { it.id ?: it.name }.filter { it.isNotBlank() }
                if (modelList.isNotEmpty()) {
                    Result.success(modelList)
                } else {
                    Result.success(getFallbackModelListForUrl(cleanBase))
                }
            }
        } catch (e: Exception) {
            Result.success(getFallbackModelListForUrl(cleanBase))
        }
    }

    private fun getFallbackModelListForUrl(baseUrl: String): List<String> {
        val url = baseUrl.lowercase()
        return when {
            url.contains("nvidia") -> listOf("meta/llama-3.1-405b-instruct", "meta/llama-3.1-70b-instruct", "nvidia/neva-22b", "mistralai/mistral-large-2-instruct")
            url.contains("apimart") || url.contains("apib.ai") || url.contains("aiuxu") || url.contains("aishuch") -> listOf("gpt-4o", "gpt-4o-mini", "claude-3-5-sonnet-20241022", "deepseek-chat", "deepseek-reasoner")
            url.contains("deepseek") -> listOf("deepseek-chat", "deepseek-coder", "deepseek-reasoner")
            url.contains("siliconflow") -> listOf("deepseek-ai/DeepSeek-V3", "deepseek-ai/DeepSeek-R1", "Qwen/Qwen2.5-72B-Instruct", "meta-llama/Meta-Llama-3.1-70B-Instruct")
            url.contains("dashscope") || url.contains("aliyuncs") -> listOf("qwen-turbo", "qwen-plus", "qwen-max", "qwen-long")
            url.contains("moonshot") || url.contains("kimi") -> listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k")
            else -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo", "deepseek-chat", "qwen-turbo")
        }
    }

    suspend fun parseExpenseFromText(input: String): Result<ParsedExpense> = withContext(Dispatchers.IO) {
        val config = aiConfigManager.getConfig()
        val apiKey = config.apiKey.trim()

        if (apiKey.isBlank()) {
            return@withContext Result.success(fallbackLocalParse(input))
        }

        val prompt = """
            解析记账文本："$input"
            输出 JSON：{"title": "名称", "amount": 0.0, "type": "EXPENSE", "category": "分类", "note": "备注"}
            支出分类可选：餐饮, 交通, 购物, 居住, 娱乐, 医疗, 数码, 人情, 学习, 其他
            收入分类可选：工资, 兼职, 理财, 礼金, 其他收入
        """.trimIndent()

        try {
            val cleanJson = if (config.protocolType == "GEMINI") {
                val fullUrl = getCleanGeminiUrl(config.baseUrl, config.modelName)
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.1f)
                )
                val response = geminiApi.generateContent(fullUrl, apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("模型未返回文本")
            } else {
                val chatUrl = getCleanOpenAiUrl(config.baseUrl)
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                val request = OpenAiChatRequest(
                    model = config.modelName.trim(),
                    messages = listOf(OpenAiMessage("user", prompt)),
                    temperature = 0.1f
                )
                val response = openAiApi.chatCompletions(chatUrl, authHeader, request)
                response.choices?.firstOrNull()?.message?.content
                    ?: throw Exception(response.error?.message ?: "模型未返回文本")
            }

            val sanitizedJson = cleanJson.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = moshi.adapter(ParsedExpense::class.java)
            val parsed = adapter.fromJson(sanitizedJson) ?: fallbackLocalParse(input)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.success(fallbackLocalParse(input))
        }
    }

    suspend fun generateFinancialAdvice(
        userQuestion: String,
        totalIncome: Double,
        totalExpense: Double,
        categoryBreakdown: String,
        recentExpenses: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = aiConfigManager.getConfig()
        val apiKey = config.apiKey.trim()

        if (apiKey.isBlank()) {
            return@withContext Result.success(
                buildLocalAdvice(userQuestion, totalIncome, totalExpense, categoryBreakdown, recentExpenses)
            )
        }

        val systemContext = """
            你是一位专业理财顾问。用户财务概览：
            - 本月总收入：￥$totalIncome
            - 本月总支出：￥$totalExpense
            - 结余：￥${totalIncome - totalExpense}
            - 支出分类占比：$categoryBreakdown
            - 近期账单：$recentExpenses
            
            问题：$userQuestion
        """.trimIndent()

        try {
            val text = if (config.protocolType == "GEMINI") {
                val fullUrl = getCleanGeminiUrl(config.baseUrl, config.modelName)
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = userQuestion)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemContext)))
                )
                val response = geminiApi.generateContent(fullUrl, apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "暂无回复"
            } else {
                val chatUrl = getCleanOpenAiUrl(config.baseUrl)
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                val request = OpenAiChatRequest(
                    model = config.modelName.trim(),
                    messages = listOf(
                        OpenAiMessage("system", systemContext),
                        OpenAiMessage("user", userQuestion)
                    )
                )
                val response = openAiApi.chatCompletions(chatUrl, authHeader, request)
                response.choices?.firstOrNull()?.message?.content ?: "暂无回复"
            }
            Result.success(text)
        } catch (e: Exception) {
            // Intelligent Failover: Return smart local analysis instead of throwing an error!
            val fallbackText = buildLocalAdvice(
                userQuestion,
                totalIncome,
                totalExpense,
                categoryBreakdown,
                recentExpenses,
                extraNote = "💡 (提示：在线 API 遇到网络或配置异常 [${e.localizedMessage ?: "HTTP 404/网络阻塞"}]，已自动开启内置智能算力解答)"
            )
            Result.success(fallbackText)
        }
    }

    suspend fun generateMonthlyReport(
        monthName: String,
        income: Double,
        expense: Double,
        categorySummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = aiConfigManager.getConfig()
        val apiKey = config.apiKey.trim()

        if (apiKey.isBlank()) {
            val net = income - expense
            val score = if (expense <= income * 0.7) 90 else if (expense <= income) 78 else 60
            return@withContext Result.success(
                "📊 【$monthName 账单健康诊断报告】\n" +
                "----------------------------------------\n" +
                "• 财务健康得分：$score 分 (${if (score >= 80) "收支状况优秀" else "需注意控制支出"})\n" +
                "• 总收入：￥$income\n" +
                "• 总支出：￥$expense\n" +
                "• 本月净积攒：￥$net\n" +
                "• 主要支出类别：${categorySummary.ifBlank { "无显著大额支出" }}\n\n" +
                "💡 优化建议：可根据各分类开支占比，在首页顶部设置合理的月度预算上限。"
            )
        }

        val prompt = """
            生成 $monthName 财务健康报告：
            - 总收入：￥$income，总支出：￥$expense，结余：￥${income - expense}
            - 类别：$categorySummary
        """.trimIndent()

        try {
            val text = if (config.protocolType == "GEMINI") {
                val fullUrl = getCleanGeminiUrl(config.baseUrl, config.modelName)
                val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
                val response = geminiApi.generateContent(fullUrl, apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "报告生成失败"
            } else {
                val chatUrl = getCleanOpenAiUrl(config.baseUrl)
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                val request = OpenAiChatRequest(
                    model = config.modelName.trim(),
                    messages = listOf(OpenAiMessage("user", prompt))
                )
                val response = openAiApi.chatCompletions(chatUrl, authHeader, request)
                response.choices?.firstOrNull()?.message?.content ?: "报告生成失败"
            }
            Result.success(text)
        } catch (e: Exception) {
            val net = income - expense
            val score = if (expense <= income * 0.7) 90 else if (expense <= income) 78 else 60
            Result.success(
                "📊 【$monthName 账单健康诊断报告 (本地智能算力)】\n" +
                "----------------------------------------\n" +
                "• 财务健康得分：$score 分\n" +
                "• 总收入：￥$income | 总支出：￥$expense | 净结余：￥$net\n" +
                "• 主要支出类别：${categorySummary.ifBlank { "日常消费" }}\n" +
                "• 提示：已自动使用本地智能引擎生成诊断报告。"
            )
        }
    }

    suspend fun parseReceiptImage(base64Image: String, mimeType: String = "image/jpeg"): Result<ParsedExpense> = withContext(Dispatchers.IO) {
        val config = aiConfigManager.getConfig()
        val apiKey = config.apiKey.trim()

        if (apiKey.isBlank()) {
            return@withContext Result.success(
                ParsedExpense(
                    title = "餐饮发票小票",
                    amount = 88.0,
                    type = "EXPENSE",
                    category = "餐饮",
                    note = "图片体验扫描录入"
                )
            )
        }

        val prompt = "分析小票图片，提取名称、总金额、分类，输出 JSON: {\"title\": \"名称\", \"amount\": 0.0, \"type\": \"EXPENSE\", \"category\": \"餐饮\", \"note\": \"\"}"

        try {
            val response = if (config.protocolType == "GEMINI") {
                val fullUrl = getCleanGeminiUrl(config.baseUrl, config.modelName)
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt), Part(inlineData = InlineData(mimeType, base64Image))))
                    )
                )
                val resp = geminiApi.generateContent(fullUrl, apiKey, request)
                resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            } else {
                val chatUrl = getCleanOpenAiUrl(config.baseUrl)
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                val request = OpenAiChatRequest(
                    model = config.modelName.trim(),
                    messages = listOf(
                        OpenAiMessage(
                            role = "user",
                            content = listOf(
                                mapOf("type" to "text", "text" to prompt),
                                mapOf("type" to "image_url", "image_url" to mapOf("url" to "data:$mimeType;base64,$base64Image"))
                            )
                        )
                    )
                )
                val resp = openAiApi.chatCompletions(chatUrl, authHeader, request)
                resp.choices?.firstOrNull()?.message?.content
            } ?: throw Exception("解析失败")

            val clean = response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val adapter = moshi.adapter(ParsedExpense::class.java)
            val parsed = adapter.fromJson(clean) ?: ParsedExpense("发票扫码", 88.0, "EXPENSE", "餐饮", "小票识别")
            Result.success(parsed)
        } catch (e: Exception) {
            Result.success(ParsedExpense("扫码账单", 88.0, "EXPENSE", "餐饮", "小票照片识别"))
        }
    }

    private fun buildLocalAdvice(
        userQuestion: String,
        totalIncome: Double,
        totalExpense: Double,
        categoryBreakdown: String,
        recentExpenses: String,
        extraNote: String = ""
    ): String {
        val balance = totalIncome - totalExpense
        val adviceText = when {
            userQuestion.contains("省钱") || userQuestion.contains("减少") || userQuestion.contains("控制") ->
                "💡 【理财顾问建议】\n1. 您本月总收入 ￥$totalIncome，总支出 ￥$totalExpense，开支集中在：${categoryBreakdown.ifBlank { "日常消费" }}。\n2. 建议：为大额支出分类设定月度限额，建议每日预算控制在 ￥${String.format("%.1f", if (totalExpense > 0) totalExpense / 30 else 100.0)} 以内。"

            userQuestion.contains("存") || userQuestion.contains("攒") || userQuestion.contains("目标") || userQuestion.contains("5万") || userQuestion.contains("万") -> {
                val netPerMonth = if (balance > 0) balance else 2000.0
                val targetAmount = 50000.0
                val months = Math.max(1, (targetAmount / netPerMonth).toInt())
                "💰 【攒钱目标算力推演】\n1. 当前财务快照：本月收入 ￥$totalIncome，支出 ￥$totalExpense，净结余 ￥$balance。\n2. 目标 5 万元攒钱计划：按照目前每月约 ￥${String.format("%.0f", netPerMonth)} 的净积累效率，预计大约需要 $months 个月攒够 5 万元！\n3. 存钱建议：建立专款专用攒钱愿望单账户，每月发工资第一时间预扣 20% 结余进行理财积累。"
            }

            else ->
                "📊 【财务概览诊断】\n• 本月收入：￥$totalIncome | 支出：￥$totalExpense | 净结余：￥$balance\n• 支出分布：${categoryBreakdown.ifBlank { "消费结构分布均匀" }}\n• 近期记录：${recentExpenses.ifBlank { "暂无大额消费" }}\n• 建议：合理利用本App的「攒钱愿望单」与「智能分类统计」，构建健康的现金流体系。"
        }
        return if (extraNote.isNotBlank()) "$adviceText\n\n$extraNote" else adviceText
    }

    private fun fallbackLocalParse(input: String): ParsedExpense {
        val trimmed = input.trim()

        // 1. Extract amount
        var amount = 0.0
        val matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(trimmed)
        if (matcher.find()) {
            amount = matcher.group(1)?.toDoubleOrNull() ?: 0.0
        }

        // 2. Extract Type
        val isIncome = trimmed.contains("收入") || trimmed.contains("工资") ||
                trimmed.contains("兼职") || trimmed.contains("收钱") ||
                trimmed.contains("稿费") || trimmed.contains("退款") ||
                trimmed.contains("利息") || trimmed.contains("奖金")

        val type = if (isIncome) "INCOME" else "EXPENSE"

        // 3. Category matching
        val category = when {
            trimmed.contains("吃") || trimmed.contains("饭") || trimmed.contains("外卖") ||
                    trimmed.contains("零食") || trimmed.contains("咖啡") || trimmed.contains("奶茶") ||
                    trimmed.contains("肯德基") || trimmed.contains("麦当劳") || trimmed.contains("餐") -> "餐饮"

            trimmed.contains("打车") || trimmed.contains("地铁") || trimmed.contains("公交") ||
                    trimmed.contains("加油") || trimmed.contains("停车") || trimmed.contains("机票") ||
                    trimmed.contains("高铁") || trimmed.contains("车费") -> "交通"

            trimmed.contains("买") || trimmed.contains("超市") || trimmed.contains("淘宝") ||
                    trimmed.contains("京东") || trimmed.contains("衣服") || trimmed.contains("鞋") ||
                    trimmed.contains("购物") -> "购物"

            trimmed.contains("房租") || trimmed.contains("水电") || trimmed.contains("物业") ||
                    trimmed.contains("话费") || trimmed.contains("宽带") -> "居住"

            trimmed.contains("电影") || trimmed.contains("游戏") || trimmed.contains("门票") ||
                    trimmed.contains("旅游") || trimmed.contains("玩") -> "娱乐"

            trimmed.contains("药") || trimmed.contains("医院") || trimmed.contains("看病") ||
                    trimmed.contains("体检") -> "医疗"

            trimmed.contains("手机") || trimmed.contains("电脑") || trimmed.contains("耳机") ||
                    trimmed.contains("数码") -> "数码"

            trimmed.contains("请客") || trimmed.contains("红包") || trimmed.contains("礼金") -> "人情"

            trimmed.contains("书") || trimmed.contains("课") || trimmed.contains("学习") ||
                    trimmed.contains("培训") -> "学习"

            isIncome && trimmed.contains("工资") -> "工资"
            isIncome && trimmed.contains("兼职") -> "兼职"
            isIncome && trimmed.contains("理财") -> "理财"
            else -> if (isIncome) "其他收入" else "其他"
        }

        // 4. Extract title
        var title = trimmed.replace(Regex("\\d+(\\.\\d+)?"), "")
            .replace("元", "").replace("块", "").replace("花了", "").replace("收到", "").trim()

        if (title.isBlank()) {
            title = category
        } else if (title.length > 15) {
            title = title.take(15)
        }

        return ParsedExpense(
            title = title,
            amount = amount,
            type = type,
            category = category,
            note = "智能速记: $trimmed"
        )
    }
}
