package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddExpenseBottomSheet
import com.example.ui.components.AiAdvisorChatCard
import com.example.ui.components.AiConfigDialog
import com.example.ui.components.AiFinancialReportDialog
import com.example.ui.components.AiInsightsCard
import com.example.ui.components.BudgetOverviewCard
import com.example.ui.components.CategoryBudgetCard
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.ExportDataBottomSheet
import com.example.ui.components.LedgerSelectorBar
import com.example.ui.components.MonthPicker
import com.example.ui.components.MonthlyChartCard
import com.example.ui.components.RecurringBillsCard
import com.example.ui.components.SavingsGoalCard
import com.example.ui.components.SearchBarAndFilter
import com.example.ui.components.SmartAiAddDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedLedger by viewModel.selectedLedger.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val financialReport by viewModel.financialReport.collectAsStateWithLifecycle()
    val isReportLoading by viewModel.isReportLoading.collectAsStateWithLifecycle()
    val reasoningPlan by viewModel.reasoningPlan.collectAsStateWithLifecycle()
    val isReasoningLoading by viewModel.isReasoningLoading.collectAsStateWithLifecycle()

    val aiConfig by viewModel.aiConfig.collectAsStateWithLifecycle()
    val recurringBills by viewModel.recurringBills.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Savings Wishlist, 2: Transactions, 3: AI Advisor
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var showSmartAiDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showAiConfigDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$selectedLedger • $selectedMonth",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Active Provider Badge
                                Surface(
                                    onClick = { showAiConfigDialog = true },
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = aiConfig.providerType.displayName.split(" ").firstOrNull() ?: "AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "智能随手记账",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // AI Model Config Button
                        IconButton(onClick = { showAiConfigDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "大模型 & 国内 API 配置",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Export Data Button
                        IconButton(onClick = { showExportSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "导出账单",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Smart AI Button
                        IconButton(onClick = { showSmartAiDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI 语音记账",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Multi-Ledger Selection Chips
                LedgerSelectorBar(
                    availableLedgers = viewModel.availableLedgers,
                    selectedLedger = selectedLedger,
                    onLedgerSelected = { viewModel.setSelectedLedger(it) }
                )

                // Month Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthPicker(
                        selectedMonth = selectedMonth,
                        onMonthSelected = { viewModel.setSelectedMonth(it) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("图表分析", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("攒钱愿望", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("明细 (${summary.totalRecords})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI 顾问", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showSmartAiDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI极速记账")
                }

                ExtendedFloatingActionButton(
                    onClick = { showAddBottomSheet = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("记一笔", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> OverviewTabContent(
                    summary = summary,
                    aiInsights = aiInsights,
                    isAiLoading = isAiLoading,
                    chatMessages = chatMessages,
                    recurringBills = recurringBills,
                    onAddRecurringBill = { viewModel.addRecurringBill(it) },
                    onDeleteRecurringBill = { viewModel.deleteRecurringBill(it) },
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onRefreshInsights = { viewModel.refreshAiInsights() },
                    onGenerateReport = { viewModel.generateFinancialReport() },
                    onBudgetUpdated = { viewModel.setMonthlyBudget(it) },
                    onUpdateCategoryBudget = { title, limit -> viewModel.setCategoryBudget(title, limit) }
                )
                1 -> SavingsWishlistTabContent(
                    goals = savingsGoals,
                    onAddGoal = { title, target, emoji, date -> viewModel.addSavingsGoal(title, target, emoji, date) },
                    onDeposit = { goal, deposit -> viewModel.depositToSavingsGoal(goal, deposit) },
                    onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                    onGenerateReasoningPlan = { viewModel.generateReasoningPlan(it) }
                )
                2 -> TransactionsTabContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedCategoryFilter = selectedCategoryFilter,
                    onCategoryFilterChange = { viewModel.setSelectedCategoryFilter(it) },
                    expensesList = filteredExpenses,
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                3 -> Box(modifier = Modifier.padding(16.dp)) {
                    Column {
                        // AI Model Status Header
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { showAiConfigDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "当前 AI 引擎：${aiConfig.providerType.displayName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "模型: ${aiConfig.getEffectiveModelName()} · 点击切换国内 DeepSeek/硅基流动/Qwen",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AiAdvisorChatCard(
                            chatMessages = chatMessages,
                            isAiLoading = isAiLoading,
                            onSendMessage = { viewModel.sendChatMessage(it) }
                        )
                    }
                }
            }
        }
    }

    // Manual Add Expense Sheet
    if (showAddBottomSheet) {
        AddExpenseBottomSheet(
            sheetState = sheetState,
            defaultDate = viewModel.todayDate,
            currentLedger = selectedLedger,
            onDismiss = { showAddBottomSheet = false },
            onAddExpense = { amount, category, date, note, type, ledger ->
                viewModel.addExpense(amount, category, date, note, type, ledger) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showAddBottomSheet = false
                        }
                    }
                }
            }
        )
    }

    // Smart AI Voice/Text/Receipt Image Entry Dialog
    if (showSmartAiDialog) {
        SmartAiAddDialog(
            isAiLoading = isAiLoading,
            onParseText = { text, callback -> viewModel.parseSmartVoiceText(text, callback) },
            onParseReceiptImage = { base64, callback -> viewModel.parseReceiptImage(base64, callback) },
            onConfirmExpense = { parsed ->
                viewModel.addExpense(
                    amount = parsed.amount,
                    type = parsed.type,
                    category = parsed.category,
                    ledger = selectedLedger,
                    note = parsed.note,
                    date = parsed.date
                )
            },
            onDismiss = { showSmartAiDialog = false }
        )
    }

    // AI Model Config Center Dialog
    if (showAiConfigDialog) {
        AiConfigDialog(
            currentConfig = aiConfig,
            onSaveConfig = { viewModel.saveAiConfig(it) },
            onTestConnection = { config, callback -> viewModel.testAiConnection(config, callback) },
            onDismiss = { showAiConfigDialog = false }
        )
    }

    // Financial Health Report Dialog
    if (financialReport != null || isReportLoading) {
        AiFinancialReportDialog(
            reportText = financialReport,
            isReportLoading = isReportLoading,
            onDismiss = { viewModel.clearFinancialReport() }
        )
    }

    // AI Deep Thinking / Reasoning Mode Dialog
    if (reasoningPlan != null || isReasoningLoading) {
        com.example.ui.components.AiDeepReasoningDialog(
            reasoningPlanText = reasoningPlan,
            isReasoningLoading = isReasoningLoading,
            onDismiss = { viewModel.clearReasoningPlan() }
        )
    }

    // Export Data Bottom Sheet
    if (showExportSheet) {
        ExportDataBottomSheet(
            csvContent = viewModel.exportDataAsCsv(),
            onDismiss = { showExportSheet = false }
        )
    }
}

@Composable
private fun OverviewTabContent(
    summary: MonthlySummary,
    aiInsights: List<String>,
    isAiLoading: Boolean,
    chatMessages: List<ChatMessage>,
    recurringBills: List<com.example.ui.components.RecurringBillItem>,
    onAddRecurringBill: (com.example.ui.components.RecurringBillItem) -> Unit,
    onDeleteRecurringBill: (com.example.ui.components.RecurringBillItem) -> Unit,
    onSendMessage: (String) -> Unit,
    onRefreshInsights: () -> Unit,
    onGenerateReport: () -> Unit,
    onBudgetUpdated: (Double) -> Unit,
    onUpdateCategoryBudget: (String, Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Income / Expense / Budget Overview
        item {
            BudgetOverviewCard(
                summary = summary,
                onBudgetUpdated = onBudgetUpdated
            )
        }

        // Monthly Category Chart Card
        item {
            MonthlyChartCard(summary = summary)
        }

        // Periodic Fixed Subscriptions & Recurring Bills
        item {
            RecurringBillsCard(
                bills = recurringBills,
                onAddBill = onAddRecurringBill,
                onDeleteBill = onDeleteRecurringBill
            )
        }

        // AI Personal Financial Insights Card
        item {
            AiInsightsCard(
                aiInsights = aiInsights,
                isAiLoading = isAiLoading,
                onRefreshInsights = onRefreshInsights,
                onGenerateReport = onGenerateReport
            )
        }

        // AI 1v1 Advisor Interactive Chat
        item {
            AiAdvisorChatCard(
                chatMessages = chatMessages,
                isAiLoading = isAiLoading,
                onSendMessage = onSendMessage
            )
        }

        // Category Budget Alerts
        item {
            CategoryBudgetCard(
                summary = summary,
                onUpdateCategoryBudget = onUpdateCategoryBudget
            )
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun SavingsWishlistTabContent(
    goals: List<com.example.data.SavingsGoalEntity>,
    onAddGoal: (String, Double, String, String) -> Unit,
    onDeposit: (com.example.data.SavingsGoalEntity, Double) -> Unit,
    onDeleteGoal: (com.example.data.SavingsGoalEntity) -> Unit,
    onGenerateReasoningPlan: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SavingsGoalCard(
                goals = goals,
                onAddGoal = onAddGoal,
                onDeposit = onDeposit,
                onDeleteGoal = onDeleteGoal,
                onGenerateReasoningPlan = onGenerateReasoningPlan
            )
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun TransactionsTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategoryFilter: com.example.ui.model.ExpenseCategory?,
    onCategoryFilterChange: (com.example.ui.model.ExpenseCategory?) -> Unit,
    expensesList: List<com.example.data.ExpenseEntity>,
    onDeleteExpense: (com.example.data.ExpenseEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        SearchBarAndFilter(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            selectedCategoryFilter = selectedCategoryFilter,
            onCategoryFilterChange = onCategoryFilterChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (expensesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔍",
                        fontSize = 42.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "未搜索到匹配的账目记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "试试切换账本、搜索词或添加新账单吧",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(
                    items = expensesList,
                    key = { it.id }
                ) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onDelete = onDeleteExpense
                    )
                }
            }
        }
    }
}
