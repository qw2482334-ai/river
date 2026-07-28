package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiConfig
import com.example.data.AiConfigManager
import com.example.data.AppDatabase
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.GeminiService
import com.example.data.ParsedExpense
import com.example.data.SavingsGoalEntity
import com.example.ui.components.RecurringBillItem
import com.example.ui.model.ExpenseCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MonthlySummary(
    val selectedMonth: String = "",
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netBalance: Double = 0.0,
    val categoryTotals: Map<ExpenseCategory, Double> = emptyMap(),
    val dailyAverage: Double = 0.0,
    val topCategory: ExpenseCategory? = null,
    val topCategoryAmount: Double = 0.0,
    val totalRecords: Int = 0,
    val budget: Double = 5000.0,
    val categoryBudgets: Map<String, Double> = emptyMap()
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val aiConfigManager = AiConfigManager(application)

    private val _aiConfig = MutableStateFlow(aiConfigManager.getAiConfig())
    val aiConfig: StateFlow<AiConfig> = _aiConfig.asStateFlow()

    val todayDate: String = LocalDate.now().toString()
    val currentYearMonth: String = String.format("%04d-%02d", LocalDate.now().year, LocalDate.now().monthValue)

    val availableLedgers = listOf("全部账本", "日常账本", "家庭账本", "旅行出差", "工作报销")

    private val _selectedMonth = MutableStateFlow(currentYearMonth)
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedLedger = MutableStateFlow("日常账本")
    val selectedLedger: StateFlow<String> = _selectedLedger.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("全部") // "全部", "支出", "收入"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(5000.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiInsights = MutableStateFlow<List<String>>(emptyList())
    val aiInsights: StateFlow<List<String>> = _aiInsights.asStateFlow()

    private val _recurringBills = MutableStateFlow<List<RecurringBillItem>>(
        listOf(
            RecurringBillItem(title = "住房租金按揭", amount = 2800.0, dayOfMonth = 15, category = "住房", cycle = "每月"),
            RecurringBillItem(title = "千兆宽带+手机套餐", amount = 129.0, dayOfMonth = 1, category = "其它", cycle = "每月"),
            RecurringBillItem(title = "iCloud + 软件订阅", amount = 21.0, dayOfMonth = 8, category = "学习", cycle = "每月")
        )
    )
    val recurringBills: StateFlow<List<RecurringBillItem>> = _recurringBills.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "👋 你好！我是你的 AI 私人理财顾问。你可以向我咨询任何关于理财规划、省钱技巧、资产配置或本月开销分析的问题！系统已支持国内 DeepSeek、硅基流动等 API 无缝切换哦！")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _categoryBudgets = MutableStateFlow<Map<String, Double>>(
        mapOf(
            "餐饮" to 1500.0,
            "交通" to 500.0,
            "购物" to 1000.0,
            "娱乐" to 600.0,
            "住房" to 2500.0
        )
    )
    val categoryBudgets: StateFlow<Map<String, Double>> = _categoryBudgets.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao(), database.savingsGoalDao())

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            refreshAiInsights()
        }
    }

    fun saveAiConfig(config: AiConfig) {
        aiConfigManager.saveAiConfig(config)
        _aiConfig.value = config
        refreshAiInsights()
    }

    fun testAiConnection(config: AiConfig, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = GeminiService.testApiConnection(config)
            onResult(result.first, result.second)
        }
    }

    fun addRecurringBill(bill: RecurringBillItem) {
        val current = _recurringBills.value.toMutableList()
        current.add(bill)
        _recurringBills.value = current
    }

    fun deleteRecurringBill(bill: RecurringBillItem) {
        val current = _recurringBills.value.toMutableList()
        current.remove(bill)
        _recurringBills.value = current
    }

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyExpenses: StateFlow<List<ExpenseEntity>> = _selectedMonth
        .flatMapLatest { month ->
            repository.getExpensesByMonth(month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        monthlyExpenses,
        _selectedLedger,
        _selectedTypeFilter,
        _searchQuery,
        _selectedCategoryFilter
    ) { expenses, ledger, typeFilter, query, categoryFilter ->
        expenses.filter { expense ->
            val matchesLedger = (ledger == "全部账本" || expense.ledger == ledger)
            val matchesType = (typeFilter == "全部" || expense.type == typeFilter)
            val matchesQuery = query.isBlank() ||
                    expense.note.contains(query, ignoreCase = true) ||
                    expense.category.contains(query, ignoreCase = true) ||
                    expense.amount.toString().contains(query)
            val matchesCategory = categoryFilter == null || expense.category == categoryFilter.title

            matchesLedger && matchesType && matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val monthlySummary: StateFlow<MonthlySummary> = combine(
        monthlyExpenses,
        _selectedMonth,
        _monthlyBudget,
        _categoryBudgets
    ) { expenses, month, budget, catBudgets ->
        var totalExpense = 0.0
        var totalIncome = 0.0
        val catMap = mutableMapOf<ExpenseCategory, Double>()

        expenses.forEach { item ->
            if (item.type == "支出") {
                totalExpense += item.amount
                val cat = ExpenseCategory.fromTitle(item.category)
                catMap[cat] = (catMap[cat] ?: 0.0) + item.amount
            } else if (item.type == "收入") {
                totalIncome += item.amount
            }
        }

        val ym = try { YearMonth.parse(month) } catch (e: Exception) { YearMonth.now() }
        val daysInMonth = ym.lengthOfMonth()
        val dailyAvg = if (daysInMonth > 0) totalExpense / daysInMonth else 0.0

        val topEntry = catMap.maxByOrNull { it.value }

        MonthlySummary(
            selectedMonth = month,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            netBalance = totalIncome - totalExpense,
            categoryTotals = catMap,
            dailyAverage = dailyAvg,
            topCategory = topEntry?.key,
            topCategoryAmount = topEntry?.value ?: 0.0,
            totalRecords = expenses.size,
            budget = budget,
            categoryBudgets = catBudgets
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlySummary()
    )

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
        refreshAiInsights()
    }

    fun setSelectedLedger(ledger: String) {
        _selectedLedger.value = ledger
    }

    fun setSelectedTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategoryFilter(category: ExpenseCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
    }

    fun setCategoryBudget(categoryTitle: String, limit: Double) {
        val current = _categoryBudgets.value.toMutableMap()
        current[categoryTitle] = limit
        _categoryBudgets.value = current
    }

    fun addExpense(
        amount: Double,
        type: String,
        category: String,
        ledger: String,
        note: String,
        date: String = todayDate,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                amount = amount,
                type = type,
                category = category,
                ledger = ledger,
                note = note,
                date = date,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(expense)

            val addedMonth = date.take(7)
            if (addedMonth != _selectedMonth.value && addedMonth.length == 7) {
                _selectedMonth.value = addedMonth
            }
            refreshAiInsights()
            onSuccess()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.delete(expense)
            refreshAiInsights()
        }
    }

    fun parseSmartVoiceText(text: String, onParsed: (ParsedExpense) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val parsed = GeminiService.parseSmartExpenseText(text, todayDate, _aiConfig.value)
            _isAiLoading.value = false
            onParsed(parsed)
        }
    }

    fun refreshAiInsights() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val summary = monthlySummary.value
            val insights = GeminiService.getAiFinancialInsight(
                totalIncome = summary.totalIncome,
                totalExpense = summary.totalExpense,
                balance = summary.netBalance,
                topCategory = summary.topCategory?.title ?: "无",
                topCatAmount = summary.topCategoryAmount,
                month = _selectedMonth.value,
                aiConfig = _aiConfig.value
            )
            _aiInsights.value = insights
            _isAiLoading.value = false
        }
    }

    private val _financialReport = MutableStateFlow<String?>(null)
    val financialReport: StateFlow<String?> = _financialReport.asStateFlow()

    private val _isReportLoading = MutableStateFlow(false)
    val isReportLoading: StateFlow<Boolean> = _isReportLoading.asStateFlow()

    private val _reasoningPlan = MutableStateFlow<String?>(null)
    val reasoningPlan: StateFlow<String?> = _reasoningPlan.asStateFlow()

    private val _isReasoningLoading = MutableStateFlow(false)
    val isReasoningLoading: StateFlow<Boolean> = _isReasoningLoading.asStateFlow()

    fun generateReasoningPlan(financialGoal: String) {
        viewModelScope.launch {
            _isReasoningLoading.value = true
            val summary = monthlySummary.value
            val netSave = if (summary.netBalance > 0) summary.netBalance else 1500.0
            val plan = GeminiService.generateThinkingReasoningPlan(
                financialGoal = financialGoal,
                currentBalance = summary.netBalance,
                monthlySavingCapacity = netSave,
                aiConfig = _aiConfig.value
            )
            _reasoningPlan.value = plan
            _isReasoningLoading.value = false
        }
    }

    fun clearReasoningPlan() {
        _reasoningPlan.value = null
    }

    fun generateFinancialReport() {
        viewModelScope.launch {
            _isReportLoading.value = true
            val summary = monthlySummary.value
            val txCount = filteredExpenses.value.size
            val report = GeminiService.generateFinancialReport(
                totalIncome = summary.totalIncome,
                totalExpense = summary.totalExpense,
                budget = monthlyBudget.value,
                topCategory = summary.topCategory?.title ?: "无",
                topCatAmount = summary.topCategoryAmount,
                month = _selectedMonth.value,
                transactionCount = txCount,
                aiConfig = _aiConfig.value
            )
            _financialReport.value = report
            _isReportLoading.value = false
        }
    }

    fun parseReceiptImage(base64Image: String, onParsed: (ParsedExpense) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val currentDate = LocalDate.now().toString()
            val parsed = GeminiService.parseReceiptImage(
                base64Image = base64Image,
                currentDateStr = currentDate,
                aiConfig = _aiConfig.value
            )
            _isAiLoading.value = false
            onParsed(parsed)
        }
    }

    fun clearFinancialReport() {
        _financialReport.value = null
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage("user", userText.trim()))
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isAiLoading.value = true
            val summary = monthlySummary.value
            val aiReply = GeminiService.chatWithAdvisor(
                userQuery = userText,
                totalIncome = summary.totalIncome,
                totalExpense = summary.totalExpense,
                balance = summary.netBalance,
                topCategory = summary.topCategory?.title ?: "无",
                topCatAmount = summary.topCategoryAmount,
                month = _selectedMonth.value,
                aiConfig = _aiConfig.value
            )
            _isAiLoading.value = false

            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(ChatMessage("ai", aiReply))
            _chatMessages.value = updatedList
        }
    }

    // Savings Goals
    fun addSavingsGoal(title: String, targetAmount: Double, emoji: String = "🎯", targetDate: String = "") {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                emoji = emoji,
                targetDate = targetDate
            )
            repository.insertGoal(goal)
        }
    }

    fun depositToSavingsGoal(goal: SavingsGoalEntity, depositAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + depositAmount)
            repository.updateGoal(updated)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Export Data as CSV
    fun exportDataAsCsv(): String {
        val list = filteredExpenses.value
        val sb = StringBuilder()
        sb.append("ID,日期,类型,类别,金额,账本,备注\n")
        list.forEach { item ->
            sb.append("${item.id},${item.date},${item.type},${item.category},${item.amount},${item.ledger},\"${item.note}\"\n")
        }
        return sb.toString()
    }
}
