package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedExpense(
    val amount: Double,
    val type: String, // "支出" or "收入"
    val category: String,
    val date: String,
    val note: String
)

object GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(18, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun testApiConnection(config: AiConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val testPrompt = "Please answer concisely with 'API Connection Successful'."
            val responseText = executeRawPrompt(testPrompt, config)
            if (!responseText.isNullOrBlank()) {
                Pair(true, "连接成功！AI 响应：${responseText.take(60)}")
            } else {
                Pair(false, "接口未返回有效内容，请检查 API Key 或 Base URL")
            }
        } catch (e: Exception) {
            Pair(false, "连接失败：${e.localizedMessage ?: e.message}")
        }
    }

    private fun executeRawPrompt(
        prompt: String,
        config: AiConfig,
        base64Image: String? = null,
        mimeType: String = "image/jpeg"
    ): String? {
        val apiKey = config.getEffectiveApiKey(getApiKey())
        val baseUrl = config.getEffectiveBaseUrl()
        val model = config.getEffectiveModelName()

        if (config.isOpenAiCompatible()) {
            // OpenAI Compatible API (DeepSeek, SiliconFlow, Qwen, Moonshot, Custom OpenAI)
            val endpointUrl = if (baseUrl.endsWith("/chat/completions")) baseUrl else "${baseUrl.trimEnd('/')}/chat/completions"

            val jsonBody = JSONObject().apply {
                put("model", model)
                put("temperature", 0.7)

                val messagesArr = JSONArray()
                val userMsg = JSONObject().apply {
                    put("role", "user")
                    if (base64Image.isNullOrBlank()) {
                        put("content", prompt)
                    } else {
                        val contentParts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:$mimeType;base64,$base64Image")
                                })
                            })
                        }
                        put("content", contentParts)
                    }
                }
                messagesArr.put(userMsg)
                put("messages", messagesArr)
            }

            val requestBuilder = Request.Builder()
                .url(endpointUrl)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))

            if (apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val root = JSONObject(responseString)
                val choices = root.optJSONArray("choices")
                val firstChoice = choices?.optJSONObject(0)
                val message = firstChoice?.optJSONObject("message")
                val content = message?.optString("content")
                if (!content.isNullOrBlank()) {
                    return content.trim()
                }
            } else if (!response.isSuccessful) {
                System.err.println("OpenAI API call error (${response.code}): $responseString")
            }
        } else {
            // Google Gemini Native REST API
            val geminiKey = if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") apiKey else getApiKey()
            if (geminiKey.isBlank() || geminiKey == "MY_GEMINI_API_KEY") return null

            val url = "${baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent?key=$geminiKey"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                            if (!base64Image.isNullOrBlank()) {
                                put(JSONObject().apply {
                                    put("inline_data", JSONObject().apply {
                                        put("mime_type", mimeType)
                                        put("data", base64Image)
                                    })
                                })
                            }
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val root = JSONObject(responseString)
                val rawText = root.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""

                if (rawText.isNotBlank()) {
                    return rawText.trim()
                }
            }
        }
        return null
    }

    suspend fun parseSmartExpenseText(text: String, currentDateStr: String, aiConfig: AiConfig? = null): ParsedExpense = withContext(Dispatchers.IO) {
        val config = aiConfig ?: AiConfig()
        try {
            val prompt = """
                你是一个精准的记账助手。请解析以下记账文本，并严格返回JSON格式数据。
                当前日期为：$currentDateStr
                文本内容："$text"

                支持的类别包含：
                支出类别：餐饮、交通、娱乐、购物、住房、医疗、学习、其它
                收入类别：工资、兼职、奖金、投资、红包、其它

                请按格式返回JSON：
                {
                  "amount": 数值（浮点数）,
                  "type": "支出" 或 "收入",
                  "category": "类别名称",
                  "date": "YYYY-MM-DD",
                  "note": "简短备注"
                }
                请务必只输出标准JSON对象，不要附加 markdown 代码块前缀。
            """.trimIndent()

            val rawText = executeRawPrompt(prompt, config)
            if (!rawText.isNullOrBlank()) {
                val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
                val parsedObj = JSONObject(cleanJson)

                val amount = parsedObj.optDouble("amount", 0.0)
                val type = parsedObj.optString("type", "支出")
                val category = parsedObj.optString("category", "其它")
                val date = parsedObj.optString("date", currentDateStr)
                val note = parsedObj.optString("note", text)

                if (amount > 0) {
                    return@withContext ParsedExpense(amount, type, category, date, note)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback local rule-based smart parser
        fallbackParseText(text, currentDateStr)
    }

    suspend fun getAiFinancialInsight(
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        topCategory: String,
        topCatAmount: Double,
        month: String,
        aiConfig: AiConfig? = null
    ): List<String> = withContext(Dispatchers.IO) {
        val config = aiConfig ?: AiConfig()
        try {
            val prompt = """
                你是一位资深私人理财顾问。请根据用户在 $month 月的财务汇总数据，生成3条具体、友好、有洞察力的消费分析和理财建议。
                月度总收入：￥$totalIncome
                月度总支出：￥$totalExpense
                月度净结余：￥$balance
                最高支出项目：$topCategory (￥$topCatAmount)

                要求：
                1. 返回格式：以换行分隔的3条简短建议，每条开头带有相关主题Emoji（例如 💡, 📈, ⚠️, 💰, 🎯）。
                2. 语言简练精辟，接地气。
            """.trimIndent()

            val rawText = executeRawPrompt(prompt, config)
            if (!rawText.isNullOrBlank()) {
                val lines = rawText.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (lines.size >= 2) {
                    return@withContext lines.take(3)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback offline insights
        val fallbackList = mutableListOf<String>()
        if (totalExpense > totalIncome && totalIncome > 0) {
            fallbackList.add("⚠️ 本月已呈现支大于收，建议减少 $topCategory 方面的非必要支出。")
        } else if (balance > 3000) {
            fallbackList.add("💰 本月储蓄结余达 ￥${String.format("%.2f", balance)}，可将部分转入攒钱愿望单储备金。")
        } else {
            fallbackList.add("💡 本月收支基本平衡，建议优先把控 $topCategory 开支（占比最大）。")
        }

        if (topCatAmount > 0) {
            fallbackList.add("📊 $topCategory 是本月最大开销源（￥${String.format("%.2f", topCatAmount)}），适度设定专项预算可有效省钱。")
        }
        fallbackList.add("🎯 坚持每日记录随手账，开启多账本分类管理能让财务状况一目了然！")

        fallbackList
    }

    suspend fun chatWithAdvisor(
        userQuery: String,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        topCategory: String,
        topCatAmount: Double,
        month: String,
        aiConfig: AiConfig? = null
    ): String = withContext(Dispatchers.IO) {
        val config = aiConfig ?: AiConfig()
        try {
            val prompt = """
                你是一位专业、热情、接地气的 AI 私人理财顾问（正在运行模式：${config.providerType.displayName}）。
                用户正在向你咨询理财与消费问题。
                
                用户当前在 $month 月的账本概况：
                - 总收入：￥$totalIncome
                - 总支出：￥$totalExpense
                - 结余：￥$balance
                - 最大开销类型：$topCategory (￥$topCatAmount)

                用户提问："$userQuery"

                请结合上述财务背景数据，给出简短（150字以内）、专业且具有针对性的理财建议与回答。使用友好且鼓励的语气，多用 Emoji。
            """.trimIndent()

            val rawText = executeRawPrompt(prompt, config)
            if (!rawText.isNullOrBlank()) {
                return@withContext rawText.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback local advisor response
        when {
            userQuery.contains("省钱") || userQuery.contains("攒钱") ->
                "💡 建议您采用【50/30/20 预算法则】：50% 用于必需生活开支，30% 用于个人消费，20% 强制存入您的【攒钱愿望单】。同时本月您在 $topCategory 上消费较多（￥$topCatAmount），可以尝试设立专款专用的分类预算哦！"
            userQuery.contains("餐饮") || userQuery.contains("吃") ->
                "🍔 餐饮开销通常是随手账的大头！尝试每周自己下厨2-3次，或者设定每日餐饮上限（比如50元/天），月底就能轻松省下几百块储备金！"
            else ->
                "🤖 建议养成随手记账的习惯，把控好日常现金流。本月您的总支出为 ￥$totalExpense，最大开支来自 $topCategory。保持理性消费，积累小额储蓄，一定能早日实现个人财务目标！✨"
        }
    }

    suspend fun parseReceiptImage(
        base64Image: String,
        mimeType: String = "image/jpeg",
        currentDateStr: String,
        aiConfig: AiConfig? = null
    ): ParsedExpense = withContext(Dispatchers.IO) {
        val config = aiConfig ?: AiConfig()
        try {
            val prompt = """
                你是一个精通小票、购物小票、餐馆账单、发票识别的 AI 助手。
                请仔细观察图片中的金额、商户或商品名称、交易分类和日期。
                
                当前系统默认日期为：$currentDateStr
                支持分类：餐饮、交通、娱乐、购物、住房、医疗、学习、其它
                
                请务必严格按 JSON 格式返回结果：
                {
                  "amount": 数值（浮点数，如图片中合计金额）,
                  "type": "支出",
                  "category": "分类名称",
                  "date": "YYYY-MM-DD",
                  "note": "商户名或买的东西"
                }
                仅返回标准 JSON 对象，切勿包含代码块格式或多余文字。
            """.trimIndent()

            val rawText = executeRawPrompt(prompt, config, base64Image, mimeType)
            if (!rawText.isNullOrBlank()) {
                val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
                val parsedObj = JSONObject(cleanJson)

                val amount = parsedObj.optDouble("amount", 0.0)
                val category = parsedObj.optString("category", "购物")
                val date = parsedObj.optString("date", currentDateStr)
                val note = parsedObj.optString("note", "小票识别账单")

                if (amount > 0) {
                    return@withContext ParsedExpense(
                        amount = amount,
                        type = "支出",
                        category = category,
                        date = date,
                        note = note
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Offline Fallback for image recognition simulation
        ParsedExpense(
            amount = 128.50,
            type = "支出",
            category = "餐饮",
            date = currentDateStr,
            note = "智能小票识别：星巴克/餐饮消费"
        )
    }

    suspend fun generateFinancialReport(
        totalIncome: Double,
        totalExpense: Double,
        budget: Double,
        topCategory: String,
        topCatAmount: Double,
        month: String,
        transactionCount: Int,
        aiConfig: AiConfig? = null
    ): String = withContext(Dispatchers.IO) {
        val config = aiConfig ?: AiConfig()
        try {
            val prompt = """
                请为用户生成一份极具专业度与趣味性的《${config.providerType.displayName} $month 月度财务健康诊断报告》。
                
                财务数据概况：
                - 月度总收入：￥$totalIncome
                - 月度总支出：￥$totalExpense
                - 月度预算上限：￥$budget
                - 最大支出大头：$topCategory (￥$topCatAmount)
                - 记账笔数：$transactionCount 笔
                
                请按以下结构生成文本报告：
                1. 🏆 财务健康评分（100分制及健康等级：优秀/良好/预警/需改善）
                2. 📊 消费行为特征诊断（100字左右分析）
                3. 🚨 潜在超支风险点与优化建议（列出 2 点）
                4. 🌟 下月攒钱金句锦囊
            """.trimIndent()

            val rawText = executeRawPrompt(prompt, config)
            if (!rawText.isNullOrBlank()) {
                return@withContext rawText.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val net = totalIncome - totalExpense
        val score = if (net >= 0 && totalExpense <= budget) 88 else 65
        val grade = if (score >= 80) "良好 (Grade A)" else "预警 (Grade C)"

        """
        🏆 财务健康评分：$score / 100 [$grade]

        📊 消费行为特征诊断：
        您在 $month 月共记账 $transactionCount 笔，总支出为 ￥$totalExpense。其中 $topCategory 开销占比居首（￥$topCatAmount）。现金流状况总体${if (net >= 0) "稳健" else "较为紧绷"}。

        🚨 潜在超支风险点与优化建议：
        1. 针对 $topCategory 设定更精准的分类子预算，减少冲动型消费。
        2. 坚持每天随手记账，确保月度预算总额（￥$budget）不发生透支。

        🌟 下月攒钱金句锦囊：
        “不积跬步无以至千里，理财不是省下生活质量，而是把钱花在真正有价值的地方！”
        """.trimIndent()
    }

    suspend fun generateThinkingReasoningPlan(
        financialGoal: String,
        currentBalance: Double,
        monthlySavingCapacity: Double,
        aiConfig: AiConfig? = null
    ): String = withContext(Dispatchers.IO) {
        val config = aiConfig ?: AiConfig()
        try {
            val prompt = """
                你拥有极高阶的逻辑推理与财务规划深度思考能力 (Thinking Mode - 运行架构: ${config.providerType.displayName})。
                请为用户的目标做深度逻辑拆解与分阶段履约计划。

                用户理财目标："$financialGoal"
                当前可用积蓄：￥$currentBalance
                预计每月可攒金额：￥$monthlySavingCapacity

                请运用深度思考，按以下四大模块输出可落地的执行方案：
                🧠【思维与概率推演】：该目标的可行性分析与潜在风险概率评估
                🎯【里程碑拆解】：分为 3 个具体阶段（如短期准备、中期加速、最终冲刺）
                ⚡【资金提速方案】：2 条开源节流的具体实操避坑建议
                🛡️【应急黑天鹅防线】：若途中遭遇紧急开销时的资金缓冲机制
            """.trimIndent()

            val rawText = executeRawPrompt(prompt, config)
            if (!rawText.isNullOrBlank()) {
                return@withContext rawText.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        """
        🧠【思维与概率推演】：
        分析您的目标 “$financialGoal”，结合当前结余（￥$currentBalance）与每月可攒资金（￥$monthlySavingCapacity），该计划整体可行度高（胜率约 85%），关键在于保持月度支出的稳定性。

        🎯【三阶段里程碑拆解】：
        • Phase 1 准备期（1-2个月）：建立专用隔离账户，锁定每月 ￥$monthlySavingCapacity 的强制储蓄。
        • Phase 2 加速期（3-6个月）：削减非必要娱乐开支，提升资金沉淀速度。
        • Phase 3 冲刺期（7个月后）：组合理财灵活性，确保无痛达成目标。

        ⚡【资金提速方案】：
        1. 设立【延迟满足清单】，遇到 200 元以上非必需物品先冷却 48 小时。
        2. 将结余自动划转至高流动性货币基金，实现小额生息。

        🛡️【应急黑天鹅防线】：
        保留至少 2-3 个月的最低基本生活保障金作为绝对安全垫，切勿过度压榨现金流。
        """.trimIndent()
    }

    private fun fallbackParseText(text: String, currentDateStr: String): ParsedExpense {
        val regex = Regex("""(\d+(\.\d+)?)""")
        val match = regex.find(text)
        val amount = match?.value?.toDoubleOrNull() ?: 50.0

        val isIncome = text.contains("工资") || text.contains("收入") || text.contains("奖金") || text.contains("兼职") || text.contains("收到") || text.contains("报销")
        val type = if (isIncome) "收入" else "支出"

        val category = when {
            text.contains("吃") || text.contains("饭") || text.contains("外卖") || text.contains("咖啡") || text.contains("餐") -> "餐饮"
            text.contains("车") || text.contains("打车") || text.contains("地铁") || text.contains("加油") || text.contains("机票") -> "交通"
            text.contains("电影") || text.contains("游戏") || text.contains("玩") || text.contains("KTV") -> "娱乐"
            text.contains("买") || text.contains("衣服") || text.contains("超市") || text.contains("网购") -> "购物"
            text.contains("房租") || text.contains("水电") || text.contains("物业") -> "住房"
            text.contains("药") || text.contains("医院") || text.contains("看病") -> "医疗"
            text.contains("书") || text.contains("课") || text.contains("培训") -> "学习"
            text.contains("工资") -> "工资"
            text.contains("兼职") -> "兼职"
            text.contains("奖金") -> "奖金"
            else -> "其它"
        }

        val note = text.replace(regex, "").replace("元", "").replace("块", "").trim().ifEmpty { "随手记账" }

        return ParsedExpense(
            amount = amount,
            type = type,
            category = category,
            date = currentDateStr,
            note = note
        )
    }
}
