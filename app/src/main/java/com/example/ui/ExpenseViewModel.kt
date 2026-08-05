package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.example.ui.components.ChatMessage
import com.example.data.AiConfigManager
import java.util.Calendar
import com.example.data.SecurityManager

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = ExpenseRepository(db.expenseDao(), db.savingsGoalDao(), db.investmentDao(), db.lotteryDao(), db.subscriptionDao())
    val securityManager = SecurityManager(application)

    private val _subscriptions = MutableStateFlow<List<SubscriptionEntity>>(emptyList())
    val subscriptions = _subscriptions.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSubscriptions().collect { subs ->
                _subscriptions.value = subs
                checkDueSubscriptions(subs)
            }
        }
    }

    private suspend fun checkDueSubscriptions(subs: List<SubscriptionEntity>) {
        val now = System.currentTimeMillis()
        subs.forEach { sub ->
            if (sub.nextBillingDate <= now) {
                // Add expense
                val exp = ExpenseEntity(
                    userId = _currentUserId.value,
                    title = "${sub.title} (周期扣费)",
                    amount = sub.amount,
                    type = sub.type,
                    category = sub.category,
                    dateMillis = now,
                    note = "自动记账: ${sub.note}",
                    ledgerName = sub.ledgerName
                )
                repository.insertExpense(exp)

                // Update next billing date
                val cal = Calendar.getInstance()
                cal.timeInMillis = sub.nextBillingDate
                if (sub.cycle == "MONTHLY") {
                    cal.add(Calendar.MONTH, 1)
                } else if (sub.cycle == "YEARLY") {
                    cal.add(Calendar.YEAR, 1)
                }
                
                // If it's still overdue, fast-forward to future
                while (cal.timeInMillis <= now) {
                    if (sub.cycle == "MONTHLY") cal.add(Calendar.MONTH, 1)
                    else cal.add(Calendar.YEAR, 1)
                }
                
                val nextDate = cal.timeInMillis
                repository.updateSubscription(sub.copy(nextBillingDate = nextDate))
                
                _snackbarEvent.emit("🔄 周期账单已自动入账: ${sub.title}")
            }
        }
    }

    fun addSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch {
            repository.insertSubscription(sub)
            _snackbarEvent.emit("✅ 已添加周期账单")
        }
    }
    
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (char in line) {
            if (char == '"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString())
                current = StringBuilder()
            } else {
                current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }

    fun importCsvData(csvData: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lines = csvData.trim().split("\n")
                if (lines.size <= 1) {
                    _snackbarEvent.emit("❌ 导入失败: 格式错误或无有效数据")
                    return@launch
                }
                
                var importedCount = 0
                for (i in 1 until lines.size) {
                    val line = lines[i].trim()
                    if (line.isBlank()) continue
                    
                    val columns = parseCsvLine(line)
                    if (columns.size >= 8) {
                        val title = columns[1]
                        val amount = columns[2].toDoubleOrNull() ?: 0.0
                        val type = columns[3]
                        val category = columns[4]
                        val ledgerName = columns[5]
                        val dateMillis = columns[6].toLongOrNull() ?: System.currentTimeMillis()
                        val note = columns[7]
                        
                        val expense = ExpenseEntity(
                            userId = _currentUserId.value,
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            ledgerName = ledgerName,
                            dateMillis = dateMillis,
                            note = note
                        )
                        repository.insertExpense(expense)
                        importedCount++
                    }
                }
                _snackbarEvent.emit("✅ 成功导入 $importedCount 条账单记录")
            } catch (e: Exception) {
                _snackbarEvent.emit("❌ 导入失败: ${e.message}")
            }
        }
    }

    fun deleteSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(sub)
        }
    }

    val aiConfigManager = AiConfigManager(application)
    val geminiService = GeminiService(aiConfigManager)
    private val marketService = FinancialMarketService()

    private val _currentUserId = MutableStateFlow(0L)
    fun setUserId(id: Long) {
        _currentUserId.value = id
        viewModelScope.launch { repository.seedInitialDataIfEmpty(id) }
    }

    val isNetworkOnline = MutableStateFlow(true).asStateFlow()

    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(
        mapOf("CNY" to 1.0, "USD" to 0.138, "EUR" to 0.128, "JPY" to 21.2, "HKD" to 1.08, "GBP" to 0.109)
    )
    val exchangeRates = _exchangeRates.asStateFlow()
    val isLoadingRates = MutableStateFlow(false).asStateFlow()
    
fun refreshExchangeRates() {
        viewModelScope.launch(Dispatchers.IO) {
            _snackbarEvent.emit("💱 正在联网更新实时汇率...")
            try {
                val url = java.net.URL("https://api.exchangerate-api.com/v4/latest/CNY")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                
                val ratesPattern = java.util.regex.Pattern.compile("\"rates\":\\{(.*?)\\}")
                val matcher = ratesPattern.matcher(response)
                if (matcher.find()) {
                    val ratesStr = matcher.group(1) ?: ""
                    val pairs = ratesStr.split(",")
                    val newRates = mutableMapOf<String, Double>()
                    for (pair in pairs) {
                        val kv = pair.split(":")
                        if (kv.size == 2) {
                            val key = kv[0].replace("\"", "").trim()
                            val value = kv[1].toDoubleOrNull() ?: continue
                            newRates[key] = value
                        }
                    }
                    if (newRates.isNotEmpty()) {
                        _exchangeRates.value = newRates
                        _snackbarEvent.emit("✅ 联网汇率已更新！(基准: CNY)")
                    } else {
                        throw Exception("解析汇率失败")
                    }
                } else {
                    throw Exception("接口返回格式异常")
                }
            } catch (e: Exception) {
                _exchangeRates.value = mapOf("CNY" to 1.0, "USD" to 0.138, "EUR" to 0.128, "JPY" to 21.2, "HKD" to 1.08, "GBP" to 0.109, "KRW" to 190.5)
                _snackbarEvent.emit("❌ 联网失败: ${e.message}，已加载离线参考汇率")
            }
        }
    }

    private val _selectedLedger = MutableStateFlow("全部账本")
    val selectedLedger = _selectedLedger.asStateFlow()
    val ledgers = listOf("全部账本", "日常账本", "旅游专项", "家庭共享", "创业资金")

    val allExpenses = kotlinx.coroutines.flow.combine(_currentUserId, _selectedLedger) { uid, ledger -> Pair(uid, ledger) }.flatMapLatest { (uid, ledger) ->
        if (ledger == "全部账本") repository.getAllExpenses(uid)
        else repository.getExpensesByLedger(uid, ledger)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals = _currentUserId.flatMapLatest { uid -> repository.getAllGoals(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val investments = _currentUserId.flatMapLatest { uid -> repository.getInvestments(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lotteryRecords = _currentUserId.flatMapLatest { uid -> repository.getLotteries(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter = _categoryFilter.asStateFlow()
    
    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter = _typeFilter.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(8000.0)
    val monthlyBudget = _monthlyBudget.asStateFlow()

    private val _categoryBudgets = MutableStateFlow<Map<String, Double>>(
        ExpenseCategory.Categories.filter { !it.isIncome }.associate { it.name to it.defaultMonthlyBudget }
    )
    val categoryBudgets = _categoryBudgets.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab = _activeTab.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private val _isLoadingMarket = MutableStateFlow(false)
    val isLoadingMarket = _isLoadingMarket.asStateFlow()

    private val _isCheckingLottery = MutableStateFlow(false)
    val isCheckingLottery = _isCheckingLottery.asStateFlow()

    val onlineInsightText = MutableStateFlow<String?>(null)
    val isGeneratingInsight = MutableStateFlow(false)

    val isParsingAi = MutableStateFlow(false)
    val parsedExpenseResult = MutableStateFlow<ParsedExpense?>(null)
    val aiErrorMessage = MutableStateFlow<String?>(null)

    private val _chatMessages = MutableStateFlow(listOf(ChatMessage(sender = "AI", text = "您好！我是您的AI理财顾问。")))
    val chatMessages = _chatMessages.asStateFlow()
    val isAiThinking = MutableStateFlow(false)

    val monthlyReport = MutableStateFlow<String?>(null)
    val isReportLoading = MutableStateFlow(false)

fun fetchOnlineInsight() {
        viewModelScope.launch {
            isGeneratingInsight.value = true
            _snackbarEvent.emit("💡 正在联系 AI 生成理财诊断...")
            val list = allExpenses.value
            val totalIncome = list.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val totalExpense = list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val summary = list.filter { it.type == TransactionType.EXPENSE.name }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .entries.joinToString { "${it.key}: ￥${it.value}" }
            
            val prompt = "请根据我本月财务情况进行理性消费度打分与优化诊断（收入:￥$totalIncome, 支出:￥$totalExpense, 分类: $summary）。请提供3条具体可落地的省钱与财富增长实操建议。"
            geminiService.generateFinancialAdvice(prompt, totalIncome, totalExpense, summary, "全量交易样本")
                .onSuccess { text ->
                    onlineInsightText.value = text
                    isGeneratingInsight.value = false
                    _snackbarEvent.emit("✨ AI 深度理财诊断已完成！")
                }
                .onFailure {
                    isGeneratingInsight.value = false
                    _snackbarEvent.emit("⚠️ 诊断失败，请检查网络或配置")
                }
        }
    }

    fun addInvestment(item: InvestmentItem) {
        viewModelScope.launch {
            repository.insertInvestment(item.copy(userId = _currentUserId.value))
            _snackbarEvent.emit("📈 已添加持仓: ${item.name}")
        }
    }

    fun deleteInvestment(id: String) {
        viewModelScope.launch {
            repository.deleteInvestment(id, _currentUserId.value)
            _snackbarEvent.emit("已移除该笔理财资产")
        }
    }

    fun refreshMarketQuotes() {
        viewModelScope.launch {
            _isLoadingMarket.value = true
            _snackbarEvent.emit("💹 正在联网同步最新行情...")
            val currentInvestments = investments.value
            currentInvestments.forEach { item ->
                val result = marketService.fetchFundQuote(item.code, item.principal, item.currentValue)
                result.getOrNull()?.let { quote ->
                    repository.updateInvestment(item.copy(currentValue = quote.estimatedValue))
                }
            }
            _isLoadingMarket.value = false
            _snackbarEvent.emit("✅ 行情更新完成！")
        }
    }

    fun addLotteryRecord(record: LotteryRecord) {
        viewModelScope.launch {
            repository.insertLottery(record.copy(userId = _currentUserId.value))
            _snackbarEvent.emit("⚽ 已记录彩单: ${record.title}")
        }
    }

    fun deleteLotteryRecord(id: String) {
        viewModelScope.launch {
            repository.deleteLottery(id, _currentUserId.value)
            _snackbarEvent.emit("已删除该笔记录")
        }
    }

    fun updateLotteryStatus(id: String, status: LotteryStatus, winAmount: Double) {
        viewModelScope.launch {
            val rec = lotteryRecords.value.find { it.id == id }
            if (rec != null) {
                repository.updateLottery(rec.copy(status = status, winAmount = winAmount))
                _snackbarEvent.emit("🎯 状态已更新为: ${status.label}")
            }
        }
    }

    fun checkLotteryLiveResults() {
        viewModelScope.launch {
            if (lotteryRecords.value.isEmpty()) return@launch
            _isCheckingLottery.value = true
            marketService.checkLotteryLiveResults(lotteryRecords.value, geminiService)
                .onSuccess { results ->
                    results.forEach { res ->
                        val rec = lotteryRecords.value.find { it.id == res.recordId }
                        if (rec != null) {
                            repository.updateLottery(rec.copy(status = res.status, winAmount = res.winAmount))
                        }
                    }
                    _isCheckingLottery.value = false
                    _snackbarEvent.emit("🎯 核对完成！")
                }
                .onFailure { _isCheckingLottery.value = false }
        }
    }

    fun setLedger(ledger: String) { _selectedLedger.value = ledger }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(category: String?) { _categoryFilter.value = category }
    fun setTypeFilter(type: String?) { _typeFilter.value = type }
    fun setActiveTab(index: Int) { _activeTab.value = index }
    fun setMonthlyBudget(budget: Double) { _monthlyBudget.value = budget }
    fun setCategoryBudget(category: String, budget: Double) {
        _categoryBudgets.value = _categoryBudgets.value.toMutableMap().apply { put(category, budget) }
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.insertExpense(expense.copy(userId = _currentUserId.value))
            _snackbarEvent.emit("✅ 已记录账单")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun addGoal(title: String, targetAmount: Double, targetDateMillis: Long) {
        viewModelScope.launch {
            repository.insertGoal(SavingsGoalEntity(userId = _currentUserId.value, title = title, targetAmount = targetAmount, targetDateMillis = targetDateMillis))
        }
    }

    fun depositToGoal(goal: SavingsGoalEntity, depositAmount: Double) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(currentAmount = goal.currentAmount + depositAmount))
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    fun parseExpenseWithAi(input: String) {
        viewModelScope.launch {
            isParsingAi.value = true
            aiErrorMessage.value = null
            geminiService.parseExpenseFromText(input)
                .onSuccess { parsed -> parsedExpenseResult.value = parsed; isParsingAi.value = false }
                .onFailure { err -> aiErrorMessage.value = err.message; isParsingAi.value = false }
        }
    }

    fun parseExpenseImageWithAi(base64Image: String, mimeType: String = "image/jpeg") {
        viewModelScope.launch {
            isParsingAi.value = true
            aiErrorMessage.value = null
            geminiService.parseReceiptImage(base64Image, mimeType)
                .onSuccess { parsed -> parsedExpenseResult.value = parsed; isParsingAi.value = false }
                .onFailure { err -> aiErrorMessage.value = err.message; isParsingAi.value = false }
        }
    }

    fun clearAiParsedResult() {
        parsedExpenseResult.value = null
        aiErrorMessage.value = null
    }

    fun sendChatMessage(question: String) {
        val currentMsgs = _chatMessages.value.toMutableList()
        currentMsgs.add(ChatMessage(sender = "USER", text = question))
        _chatMessages.value = currentMsgs
        viewModelScope.launch {
            isAiThinking.value = true
            val list = allExpenses.value
            val totalIncome = list.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val totalExpense = list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val breakdown = list.filter { it.type == TransactionType.EXPENSE.name }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { exp -> exp.amount } }
                .entries.joinToString { "${it.key}: ${it.value}" }
            
            geminiService.generateFinancialAdvice(question, totalIncome, totalExpense, breakdown, "").onSuccess { reply ->
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(sender = "AI", text = reply))
                _chatMessages.value = updated
                isAiThinking.value = false
            }.onFailure { err ->
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(sender = "AI", text = "失败: ${err.message}"))
                _chatMessages.value = updated
                isAiThinking.value = false
            }
        }
    }

    fun generateMonthlyReport() {
        viewModelScope.launch {
            isReportLoading.value = true
            val list = allExpenses.value
            val income = list.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val expense = list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val summary = list.filter { it.type == TransactionType.EXPENSE.name }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .entries.joinToString { "${it.key}: ${it.value}" }
            
            geminiService.generateMonthlyReport("本月", income, expense, summary)
                .onSuccess { report -> monthlyReport.value = report; isReportLoading.value = false }
                .onFailure { err -> monthlyReport.value = "失败: ${err.message}"; isReportLoading.value = false }
        }
    }

    fun dismissReport() {
        monthlyReport.value = null
    }
}
