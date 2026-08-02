package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.components.ChatMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = ExpenseRepository(db.expenseDao(), db.savingsGoalDao())
    val aiConfigManager = AiConfigManager(application)
    val geminiService = GeminiService(aiConfigManager)
    val currencyService = CurrencyService()
    val marketService = FinancialMarketService()
    private val networkObserver = NetworkObserver(application)

    val isNetworkOnline: StateFlow<Boolean> = networkObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkObserver.isCurrentlyOnline())

    // Real-Time Currency Exchange Rates State (REST API)
    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(
        mapOf("CNY" to 1.0, "USD" to 0.138, "EUR" to 0.128, "JPY" to 21.2, "HKD" to 1.08, "GBP" to 0.109, "SGD" to 0.186, "KRW" to 191.5)
    )
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates.asStateFlow()

    private val _isLoadingRates = MutableStateFlow(false)
    val isLoadingRates: StateFlow<Boolean> = _isLoadingRates.asStateFlow()

    // Online AI Financial Insight State
    private val _onlineInsightText = MutableStateFlow<String?>(null)
    val onlineInsightText: StateFlow<String?> = _onlineInsightText.asStateFlow()

    private val _isGeneratingInsight = MutableStateFlow(false)
    val isGeneratingInsight: StateFlow<Boolean> = _isGeneratingInsight.asStateFlow()

    // Investment Portfolio State
    private val _investments = MutableStateFlow<List<InvestmentItem>>(
        listOf(
            InvestmentItem(name = "沪深300ETF", code = "510300", type = InvestmentType.FUND, principal = 10000.0, currentValue = 10850.0),
            InvestmentItem(name = "招商中证白酒", code = "161725", type = InvestmentType.FUND, principal = 5000.0, currentValue = 4720.0),
            InvestmentItem(name = "余额宝/微信理财通", code = "000001", type = InvestmentType.WEALTH, principal = 20000.0, currentValue = 20180.0)
        )
    )
    val investments: StateFlow<List<InvestmentItem>> = _investments.asStateFlow()

    private val _isLoadingMarket = MutableStateFlow(false)
    val isLoadingMarket: StateFlow<Boolean> = _isLoadingMarket.asStateFlow()

    // Lottery & Sports Betting State
    private val _lotteryRecords = MutableStateFlow<List<LotteryRecord>>(
        listOf(
            LotteryRecord(title = "英超 曼城 VS 阿森纳 (主胜)", type = LotteryType.FOOTBALL, betAmount = 100.0, winAmount = 185.0, status = LotteryStatus.WON),
            LotteryRecord(title = "超级大乐透 10注追加", type = LotteryType.NUMBER, betAmount = 30.0, winAmount = 0.0, status = LotteryStatus.LOST)
        )
    )
    val lotteryRecords: StateFlow<List<LotteryRecord>> = _lotteryRecords.asStateFlow()

    private val _isCheckingLottery = MutableStateFlow(false)
    val isCheckingLottery: StateFlow<Boolean> = _isCheckingLottery.asStateFlow()

    // Current Ledger Selection
    private val _selectedLedger = MutableStateFlow("全部账本")
    val selectedLedger: StateFlow<String> = _selectedLedger.asStateFlow()

    val ledgers = listOf("全部账本", "日常账本", "旅游专项", "家庭共享", "创业资金")

    // Expenses Stream
    val allExpenses: StateFlow<List<ExpenseEntity>> = _selectedLedger.flatMapLatest { ledger ->
        if (ledger == "全部账本") repository.allExpenses
        else repository.getExpensesByLedger(ledger)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Savings Goals Stream
    val allGoals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    // Monthly Budget Limit
    private val _monthlyBudget = MutableStateFlow(8000.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    // Category Budget Limits Map
    private val _categoryBudgets = MutableStateFlow<Map<String, Double>>(
        ExpenseCategory.Categories.filter { !it.isIncome }.associate { it.name to it.defaultMonthlyBudget }
    )
    val categoryBudgets: StateFlow<Map<String, Double>> = _categoryBudgets.asStateFlow()

    // Active Bottom Navigation Tab (0: 账单明细, 1: 预算与图表, 2: 攒钱愿望, 3: AI 顾问)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Snackbar Event Flow
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // Gemini Smart Quick Add State
    private val _isParsingAi = MutableStateFlow(false)
    val isParsingAi: StateFlow<Boolean> = _isParsingAi.asStateFlow()

    private val _parsedExpenseResult = MutableStateFlow<ParsedExpense?>(null)
    val parsedExpenseResult: StateFlow<ParsedExpense?> = _parsedExpenseResult.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    // AI Advisor Chat History
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "您好！我是您的 Gemini AI 理财顾问。我可以为您制定攒钱目标、分析消费风险、解答税务理财疑问或提供省钱攻略。随时向我提问吧！"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Monthly AI Financial Report State
    private val _monthlyReport = MutableStateFlow<String?>(null)
    val monthlyReport: StateFlow<String?> = _monthlyReport.asStateFlow()

    private val _isReportLoading = MutableStateFlow(false)
    val isReportLoading: StateFlow<Boolean> = _isReportLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshExchangeRates()
        }
    }

    fun refreshExchangeRates() {
        viewModelScope.launch {
            _isLoadingRates.value = true
            currencyService.fetchLiveRates("CNY")
                .onSuccess { ratesMap ->
                    _exchangeRates.value = ratesMap
                    _isLoadingRates.value = false
                    _snackbarEvent.emit("🌐 联网更新：国际实时外汇汇率已更新！")
                }
                .onFailure {
                    _isLoadingRates.value = false
                }
        }
    }

    fun fetchOnlineInsight() {
        viewModelScope.launch {
            _isGeneratingInsight.value = true
            _snackbarEvent.emit("🤖 正在发起联网大模型深度诊断请求...")
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
                    _onlineInsightText.value = text
                    _isGeneratingInsight.value = false
                    _snackbarEvent.emit("✨ 联网 AI 深度理财诊断已完成！")
                }
                .onFailure {
                    _isGeneratingInsight.value = false
                }
        }
    }

    fun addInvestment(item: InvestmentItem) {
        _investments.value = _investments.value + item
        viewModelScope.launch { _snackbarEvent.emit("📈 已添加持仓: ${item.name}") }
    }

    fun deleteInvestment(id: String) {
        _investments.value = _investments.value.filterNot { it.id == id }
        viewModelScope.launch { _snackbarEvent.emit("🗑️ 已删除持仓项目") }
    }

    fun refreshMarketQuotes() {
        viewModelScope.launch {
            _isLoadingMarket.value = true
            _snackbarEvent.emit("💹 正在联网同步最新公募基金/证券估值行情...")

            val updatedList = _investments.value.map { item ->
                val result = marketService.fetchFundQuote(item.code, item.principal, item.currentValue)
                result.getOrNull()?.let { quote ->
                    item.copy(currentValue = quote.estimatedValue)
                } ?: item
            }

            _investments.value = updatedList
            _isLoadingMarket.value = false
            _snackbarEvent.emit("✅ 证券理财持仓估值已完成实时行情更新！")
        }
    }

    fun addLotteryRecord(record: LotteryRecord) {
        _lotteryRecords.value = listOf(record) + _lotteryRecords.value
        viewModelScope.launch { _snackbarEvent.emit("⚽ 已记录彩票注单: ${record.title}") }
    }

    fun deleteLotteryRecord(id: String) {
        _lotteryRecords.value = _lotteryRecords.value.filterNot { it.id == id }
        viewModelScope.launch { _snackbarEvent.emit("🗑️ 已删除彩票记账项") }
    }

    fun updateLotteryStatus(id: String, status: LotteryStatus, winAmount: Double) {
        _lotteryRecords.value = _lotteryRecords.value.map { rec ->
            if (rec.id == id) rec.copy(status = status, winAmount = winAmount) else rec
        }
        viewModelScope.launch { _snackbarEvent.emit("🎯 彩票单状态已更新为: ${status.label}") }
    }

    fun checkLotteryLiveResults() {
        viewModelScope.launch {
            if (_lotteryRecords.value.isEmpty()) {
                _snackbarEvent.emit("💡 暂无待核对的足彩或彩票注单，请先添加")
                return@launch
            }

            _isCheckingLottery.value = true
            _snackbarEvent.emit("⚽ 正在发起联网/AI开奖与比赛完场比分对账...")

            marketService.checkLotteryLiveResults(_lotteryRecords.value, geminiService)
                .onSuccess { results ->
                    val resultMap = results.associateBy { it.recordId }
                    _lotteryRecords.value = _lotteryRecords.value.map { rec ->
                        val res = resultMap[rec.id]
                        if (res != null) {
                            rec.copy(status = res.status, winAmount = res.winAmount)
                        } else rec
                    }
                    _isCheckingLottery.value = false
                    val wonCount = results.count { it.status == LotteryStatus.WON }
                    _snackbarEvent.emit("🎯 联网开奖核对完成！其中 $wonCount 笔命中派彩")
                }
                .onFailure { err ->
                    _isCheckingLottery.value = false
                    _snackbarEvent.emit("⚠️ 联网开奖核对遇到异常: ${err.localizedMessage}")
                }
        }
    }

    fun setLedger(ledger: String) {
        _selectedLedger.value = ledger
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
    }

    fun setCategoryBudget(category: String, budget: Double) {
        _categoryBudgets.value = _categoryBudgets.value.toMutableMap().apply {
            put(category, budget)
        }
        viewModelScope.launch {
            _snackbarEvent.emit("已设置【$category】月度预算限额为 ￥${budget.toInt()}")
        }
    }

    // Expense Operations
    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.insertExpense(expense)

            if (expense.type == TransactionType.EXPENSE.name) {
                val currentCatExpense = allExpenses.value
                    .filter { it.category == expense.category && it.type == TransactionType.EXPENSE.name }
                    .sumOf { it.amount } + expense.amount
                val catBudget = categoryBudgets.value[expense.category] ?: 1000.0

                if (catBudget > 0) {
                    if (currentCatExpense >= catBudget) {
                        _snackbarEvent.emit("🚨 预警：【${expense.category}】支出已超出预算限额 (已用 ￥${String.format("%.1f", currentCatExpense)} / 预算 ￥${catBudget.toInt()})！")
                    } else if (currentCatExpense >= catBudget * 0.8) {
                        val pct = (currentCatExpense / catBudget * 100).toInt()
                        _snackbarEvent.emit("⚠️ 预警提醒：【${expense.category}】支出已达预算的 ${pct}% (￥${String.format("%.1f", currentCatExpense)} / ￥${catBudget.toInt()})！")
                    } else {
                        _snackbarEvent.emit("✅ 已记录账单：${expense.title} ￥${expense.amount}")
                    }
                } else {
                    _snackbarEvent.emit("✅ 已记录账单：${expense.title} ￥${expense.amount}")
                }
            } else {
                _snackbarEvent.emit("✅ 已记录账单：${expense.title} ￥${expense.amount}")
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _snackbarEvent.emit("🗑️ 账单项已成功删除")
        }
    }

    // Savings Goals Operations
    fun addGoal(title: String, targetAmount: Double, targetDateMillis: Long) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    targetDateMillis = targetDateMillis
                )
            )
            _snackbarEvent.emit("🎯 已成功建立攒钱愿望：$title")
        }
    }

    fun depositToGoal(goal: SavingsGoalEntity, depositAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + depositAmount)
            repository.updateGoal(updated)
            _snackbarEvent.emit("💰 成功向【${goal.title}】存入 ￥$depositAmount！")
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _snackbarEvent.emit("🗑️ 攒钱愿望已删除")
        }
    }

    // Gemini AI Smart Parsing
    fun parseExpenseWithAi(input: String) {
        viewModelScope.launch {
            _isParsingAi.value = true
            _aiErrorMessage.value = null
            _parsedExpenseResult.value = null
            _snackbarEvent.emit("🤖 AI 正在智能识别分析您的账单...")

            geminiService.parseExpenseFromText(input)
                .onSuccess { parsed ->
                    _parsedExpenseResult.value = parsed
                    _isParsingAi.value = false
                    _snackbarEvent.emit("✨ AI 账单拆解完毕！")
                }
                .onFailure { err ->
                    _aiErrorMessage.value = err.message ?: "解析失败"
                    _isParsingAi.value = false
                    _snackbarEvent.emit("⚠️ 解析异常: ${err.localizedMessage}")
                }
        }
    }

    fun parseExpenseImageWithAi(base64Image: String, mimeType: String = "image/jpeg") {
        viewModelScope.launch {
            _isParsingAi.value = true
            _aiErrorMessage.value = null
            _parsedExpenseResult.value = null
            _snackbarEvent.emit("📸 正在扫描识别发票小票图片...")

            geminiService.parseReceiptImage(base64Image, mimeType)
                .onSuccess { parsed ->
                    _parsedExpenseResult.value = parsed
                    _isParsingAi.value = false
                    _snackbarEvent.emit("✨ 发票图片 OCR 扫描完成！")
                }
                .onFailure { err ->
                    _aiErrorMessage.value = err.message ?: "发票识别失败"
                    _isParsingAi.value = false
                    _snackbarEvent.emit("⚠️ 小票识别失败: ${err.localizedMessage}")
                }
        }
    }

    fun clearAiParsedResult() {
        _parsedExpenseResult.value = null
        _aiErrorMessage.value = null
    }

    // Gemini AI Chat
    fun sendChatMessage(question: String) {
        val currentMsgs = _chatMessages.value.toMutableList()
        currentMsgs.add(ChatMessage(sender = "USER", text = question))
        _chatMessages.value = currentMsgs

        viewModelScope.launch {
            _isAiThinking.value = true
            val list = allExpenses.value
            val totalIncome = list.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val totalExpense = list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val breakdown = list.filter { it.type == TransactionType.EXPENSE.name }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { exp -> exp.amount } }
                .entries.joinToString { "${it.key}: ￥${it.value}" }
            val recent = list.take(5).joinToString { "${it.title}(￥${it.amount})" }

            geminiService.generateFinancialAdvice(
                userQuestion = question,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                categoryBreakdown = breakdown,
                recentExpenses = recent
            ).onSuccess { reply ->
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(sender = "AI", text = reply))
                _chatMessages.value = updated
                _isAiThinking.value = false
                _snackbarEvent.emit("💡 理财顾问已有解答！")
            }.onFailure { err ->
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(sender = "AI", text = "回复生成失败：${err.localizedMessage}。请检查设置中的 Gemini API Key。"))
                _chatMessages.value = updated
                _isAiThinking.value = false
                _snackbarEvent.emit("⚠️ AI 理财问答发生异常")
            }
        }
    }

    // Gemini Monthly Report
    fun generateMonthlyReport() {
        viewModelScope.launch {
            _isReportLoading.value = true
            _snackbarEvent.emit("📊 正在开启大模型算力生成月度财务审计报告...")
            val list = allExpenses.value
            val income = list.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val expense = list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val summary = list.filter { it.type == TransactionType.EXPENSE.name }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .entries.joinToString { "${it.key}: ￥${it.value}" }

            geminiService.generateMonthlyReport("本月", income, expense, summary)
                .onSuccess { report ->
                    _monthlyReport.value = report
                    _isReportLoading.value = false
                    _snackbarEvent.emit("✅ 月度财务健康诊断报告已成功生成！")
                }
                .onFailure { err ->
                    _monthlyReport.value = "报告生成失败：${err.localizedMessage}。请确保关联 Gemini API Key。"
                    _isReportLoading.value = false
                    _snackbarEvent.emit("⚠️ 报告生成发生异常")
                }
        }
    }

    fun dismissReport() {
        _monthlyReport.value = null
    }
}
