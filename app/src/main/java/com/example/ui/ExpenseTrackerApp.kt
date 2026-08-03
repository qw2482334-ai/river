package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.SavingsGoalEntity
import com.example.data.TransactionType
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    viewModel: ExpenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLogout: () -> Unit = {}
) {
    val selectedLedger by viewModel.selectedLedger.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle()
    val isLoadingRates by viewModel.isLoadingRates.collectAsStateWithLifecycle()
    val onlineInsightText by viewModel.onlineInsightText.collectAsStateWithLifecycle()
    val isGeneratingInsight by viewModel.isGeneratingInsight.collectAsStateWithLifecycle()
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    val isLoadingMarket by viewModel.isLoadingMarket.collectAsStateWithLifecycle()
    val lotteryRecords by viewModel.lotteryRecords.collectAsStateWithLifecycle()
    val isCheckingLottery by viewModel.isCheckingLottery.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    val isParsingAi by viewModel.isParsingAi.collectAsStateWithLifecycle()
    val parsedExpenseResult by viewModel.parsedExpenseResult.collectAsStateWithLifecycle()
    val isNetworkOnline by viewModel.isNetworkOnline.collectAsStateWithLifecycle()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    val monthlyReport by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val isReportLoading by viewModel.isReportLoading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Dialog state toggles
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showSmartAiDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAiSettingsDialog by remember { mutableStateOf(false) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    var showInvestmentAnalytics by remember { mutableStateOf(false) }
    var showLotteryAnalytics by remember { mutableStateOf(false) }
    var showFinancialHealthDialog by remember { mutableStateOf(false) }
    var showInvestmentCalculator by remember { mutableStateOf(false) }

    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    // Filtered Expenses
    val filteredExpenses = remember(allExpenses, searchQuery, categoryFilter, typeFilter) {
        allExpenses.filter { exp ->
            val matchQuery = searchQuery.isBlank() ||
                    exp.title.contains(searchQuery, ignoreCase = true) ||
                    exp.note.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true)
            val matchCat = categoryFilter == null || exp.category == categoryFilter
            val matchType = typeFilter == null || exp.type == typeFilter
            matchQuery && matchCat && matchType
        }
    }

    val totalIncome = remember(filteredExpenses) {
        filteredExpenses.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredExpenses) {
        filteredExpenses.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "开销追踪",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "river_wzh",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = if (isNetworkOnline) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isNetworkOnline) "🌐 联网正常" else "🔴 离线模式",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNetworkOnline) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "智能记账 · 资产管理 · 理财基金 · 开发者: river_wzh",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // AI Smart Quick Voice/Text Logging Button
                    IconButton(
                        onClick = { showSmartAiDialog = true },
                        modifier = Modifier.testTag("ai_smart_add_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI 语音极速记账",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Monthly AI Financial Report Button
                    IconButton(
                        onClick = {
                            viewModel.generateMonthlyReport()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "AI 财务报告",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Export CSV Data
                    IconButton(
                        onClick = { showExportSheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "导出账单",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // User Guide / Manual Button
                    IconButton(
                        onClick = { showUserGuideDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "软件使用说明与操作指南",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Logout Button
                    IconButton(
                        onClick = { onLogout() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "退出登录",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    // AI Engine & Network Settings
                    IconButton(
                        onClick = { showAiSettingsDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI 网络配置",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("明细") },
                    modifier = Modifier.testTag("nav_tab_details")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                    label = { Text("理财/足彩") },
                    modifier = Modifier.testTag("nav_tab_investments")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
                    label = { Text("图表预算") },
                    modifier = Modifier.testTag("nav_tab_charts")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    icon = { Icon(Icons.Default.Savings, contentDescription = null) },
                    label = { Text("攒钱心愿") },
                    modifier = Modifier.testTag("nav_tab_goals")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { viewModel.setActiveTab(4) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("AI 智算") },
                    modifier = Modifier.testTag("nav_tab_advisor")
                )
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showSmartAiDialog = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.testTag("ai_voice_quick_fab")
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "AI 语音极速记账")
                }

                FloatingActionButton(
                    onClick = { showAddExpenseSheet = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_expense_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "手动记一笔")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ledger Chips Top Selector
            LedgerSelectorBar(
                ledgers = viewModel.ledgers,
                selectedLedger = selectedLedger,
                onLedgerSelected = { viewModel.setLedger(it) }
            )

            // Main Tab Content View
            when (activeTab) {
                0 -> {
                    // TAB 0: 账单明细 & 全景概览
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            NetWorthOverviewCard(
                                expenses = allExpenses,
                                savingsGoals = allGoals,
                                investments = investments,
                                lotteryRecords = lotteryRecords,
                                onOpenHealthDialog = { showFinancialHealthDialog = true }
                            )
                        }

                        item {
                            // Quick Feature Shortcut Bar
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚡ 特色理财工具直达",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        FilterChip(
                                            selected = false,
                                            onClick = { viewModel.setActiveTab(1) },
                                            label = { Text("📈 证券理财") }
                                        )
                                        FilterChip(
                                            selected = false,
                                            onClick = { viewModel.setActiveTab(1) },
                                            label = { Text("⚽ 足彩彩票") }
                                        )
                                        FilterChip(
                                            selected = false,
                                            onClick = { viewModel.setActiveTab(1) },
                                            label = { Text("🌐 汇率换算") }
                                        )
                                        FilterChip(
                                            selected = false,
                                            onClick = { viewModel.setActiveTab(4) },
                                            label = { Text("🤖 AI 智算") }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            BudgetOverviewCard(
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                monthlyBudget = monthlyBudget,
                                onBudgetClick = { showSetBudgetDialog = true }
                            )
                        }

                        item {
                            SearchBarAndFilter(
                                searchQuery = searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                selectedCategoryFilter = categoryFilter,
                                onCategoryFilterSelect = { viewModel.setCategoryFilter(it) },
                                selectedTypeFilter = typeFilter,
                                onTypeFilterSelect = { viewModel.setTypeFilter(it) }
                            )
                        }

                        if (filteredExpenses.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Inbox,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "暂无相关账单记录",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = filteredExpenses,
                                key = { it.id }
                            ) { exp ->
                                ExpenseItemCard(
                                    expense = exp,
                                    onDeleteClick = { viewModel.deleteExpense(exp) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: 证券理财、彩票足彩、实时汇率
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            NetWorthOverviewCard(
                                expenses = allExpenses,
                                savingsGoals = allGoals,
                                investments = investments,
                                lotteryRecords = lotteryRecords,
                                onOpenHealthDialog = { showFinancialHealthDialog = true }
                            )
                        }

                        item {
                            InvestmentTrackerCard(
                                investments = investments,
                                onAddInvestment = { viewModel.addInvestment(it) },
                                onDeleteInvestment = { viewModel.deleteInvestment(it) },
                                onRefreshMarketQuotes = { viewModel.refreshMarketQuotes() },
                                isLoadingMarket = isLoadingMarket,
                                onOpenAnalytics = { showInvestmentAnalytics = true },
                                onOpenCalculator = { showInvestmentCalculator = true }
                            )
                        }

                        item {
                            LotteryTrackerCard(
                                lotteryRecords = lotteryRecords,
                                isCheckingLottery = isCheckingLottery,
                                onAddRecord = { viewModel.addLotteryRecord(it) },
                                onDeleteRecord = { viewModel.deleteLotteryRecord(it) },
                                onUpdateRecordStatus = { id, st, win -> viewModel.updateLotteryStatus(id, st, win) },
                                onCheckLiveResults = { viewModel.checkLotteryLiveResults() },
                                onOpenAnalytics = { showLotteryAnalytics = true }
                            )
                        }

                        item {
                            CurrencyConverterCard(
                                rates = exchangeRates,
                                isLoadingRates = isLoadingRates,
                                onRefreshRates = { viewModel.refreshExchangeRates() }
                            )
                        }
                    }
                }

                2 -> {
                    // TAB 2: 图表分析与分类预算
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            MonthlyChartCard(expenses = allExpenses)
                        }

                        item {
                            BudgetOverviewCard(
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                monthlyBudget = monthlyBudget,
                                onBudgetClick = { showSetBudgetDialog = true }
                            )
                        }

                        item {
                            CategoryBudgetSection(
                                expenses = allExpenses,
                                categoryBudgets = categoryBudgets,
                                onSetCategoryBudget = { cat, budget ->
                                    viewModel.setCategoryBudget(cat, budget)
                                }
                            )
                        }
                    }
                }

                3 -> {
                    // TAB 3: 攒钱愿望单
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "攒钱愿望单",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "立下一个小目标，积累财富复利",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { showAddGoalDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("新建愿望")
                                }
                            }
                        }

                        if (allGoals.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(32.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("还没有立下攒钱愿望，快点击上方新建吧！")
                                    }
                                }
                            }
                        } else {
                            val totalWishTarget = allGoals.sumOf { it.targetAmount }
                            val totalWishSaved = allGoals.sumOf { it.currentAmount }
                            val totalProgress = if (totalWishTarget > 0) (totalWishSaved / totalWishTarget).toFloat().coerceIn(0f, 1f) else 0f

                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "心愿池总览",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${(totalProgress * 100).toInt()}% 总达成",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        LinearProgressIndicator(
                                            progress = { totalProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "已攒总额: ￥${String.format("%.2f", totalWishSaved)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "目标总计: ￥${String.format("%.2f", totalWishTarget)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }

                            items(allGoals, key = { it.id }) { goal ->
                                SavingsGoalCard(
                                    goal = goal,
                                    onAddDepositClick = { selectedGoalForDeposit = goal },
                                    onDeleteClick = { viewModel.deleteGoal(goal) }
                                )
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: AI 智算与理财顾问
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            AiFinancialInsightCard(
                                expenses = allExpenses,
                                monthlyBudget = monthlyBudget,
                                isGeneratingInsight = isGeneratingInsight,
                                onlineInsightText = onlineInsightText,
                                onFetchOnlineInsight = { viewModel.fetchOnlineInsight() }
                            )
                        }

                        item {
                            AiAdvisorChatCard(
                                chatMessages = chatMessages,
                                onSendMessage = { viewModel.sendChatMessage(it) },
                                isThinking = isAiThinking
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs & BottomSheets ---

    // 1. Add Expense Bottom Sheet
    if (showAddExpenseSheet) {
        AddExpenseBottomSheet(
            ledgers = viewModel.ledgers,
            currentLedger = selectedLedger,
            onDismiss = { showAddExpenseSheet = false },
            onSaveExpense = { viewModel.addExpense(it) }
        )
    }

    // 2. Smart AI Quick Add Dialog
    if (showSmartAiDialog) {
        SmartAiAddDialog(
            onDismiss = {
                viewModel.clearAiParsedResult()
                showSmartAiDialog = false
            },
            onParseText = { viewModel.parseExpenseWithAi(it) },
            onParseImage = { base64, mime -> viewModel.parseExpenseImageWithAi(base64, mime) },
            onConfirmAdd = { viewModel.addExpense(it) },
            isParsing = isParsingAi,
            parsedResult = parsedExpenseResult,
            errorMessage = aiErrorMessage
        )
    }

    // 3. Export CSV Data Sheet
    if (showExportSheet) {
        ExportDataBottomSheet(
            expenses = allExpenses,
            onDismiss = { showExportSheet = false }
        )
    }

    // 4. Monthly AI Financial Report Dialog
    if (monthlyReport != null || isReportLoading) {
        AiFinancialReportDialog(
            reportText = monthlyReport ?: "",
            isLoading = isReportLoading,
            onDismiss = { viewModel.dismissReport() }
        )
    }

    // 5. Set Budget Limit Dialog
    if (showSetBudgetDialog) {
        var budgetInput by remember { mutableStateOf(monthlyBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("设置月度总预算与分类额度", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    Text(
                        text = "设置月度总预算金额及各分类专属预算限额：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("月度总预算 (元)") },
                        prefix = { Text("￥ ") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "常用分类快捷预算：",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val categories = ExpenseCategory.Categories.filter { !it.isIncome }
                        items(categories, key = { it.name }) { cat ->
                            var catBudgetInput by remember(cat.name, categoryBudgets) {
                                mutableStateOf((categoryBudgets[cat.name] ?: cat.defaultMonthlyBudget).toInt().toString())
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        tint = cat.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                OutlinedTextField(
                                    value = catBudgetInput,
                                    onValueChange = {
                                        catBudgetInput = it
                                        val newVal = it.toDoubleOrNull()
                                        if (newVal != null && newVal >= 0) {
                                            viewModel.setCategoryBudget(cat.name, newVal)
                                        }
                                    },
                                    prefix = { Text("￥", style = MaterialTheme.typography.labelSmall) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(110.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newB = budgetInput.toDoubleOrNull() ?: monthlyBudget
                        viewModel.setMonthlyBudget(newB)
                        showSetBudgetDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("保存设置")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSetBudgetDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("完成")
                }
            }
        )
    }

    // 6. Deposit to Savings Goal Dialog
    selectedGoalForDeposit?.let { goal ->
        var depositText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedGoalForDeposit = null },
            title = { Text("存入攒钱基金：${goal.title}", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = depositText,
                    onValueChange = { depositText = it },
                    label = { Text("本次存入金额 (元)") },
                    prefix = { Text("￥ ") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val dep = depositText.toDoubleOrNull() ?: 0.0
                    if (dep > 0) {
                        viewModel.depositToGoal(goal, dep)
                        selectedGoalForDeposit = null
                    }
                }) {
                    Text("确认存入")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForDeposit = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 7. Add New Savings Goal Dialog
    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var targetAmountText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("新建攒钱愿望", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("愿望名称 (如: 购买数码设备/旅行包)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it },
                        label = { Text("目标蓄水金额 (元)") },
                        prefix = { Text("￥ ") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val target = targetAmountText.toDoubleOrNull() ?: 0.0
                    if (goalTitle.isNotBlank() && target > 0) {
                        viewModel.addGoal(
                            title = goalTitle,
                            targetAmount = target,
                            targetDateMillis = System.currentTimeMillis() + 90L * 24 * 3600 * 1000
                        )
                        showAddGoalDialog = false
                    }
                }) {
                    Text("立下愿望")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 8. AI Engine & Network Settings Dialog
    if (showAiSettingsDialog) {
        AiSettingsDialog(
            aiConfigManager = viewModel.aiConfigManager,
            geminiService = viewModel.geminiService,
            onDismiss = { showAiSettingsDialog = false }
        )
    }

    // 9. User Guide & Software Operation Manual Dialog
    if (showUserGuideDialog) {
        UserGuideDialog(
            onDismissRequest = { showUserGuideDialog = false },
            onOpenAiSettings = { showAiSettingsDialog = true },
            onOpenSmartAdd = { showSmartAiDialog = true }
        )
    }

    // 10. Monthly AI Financial Report Dialog
    if (isReportLoading || monthlyReport != null) {
        AiFinancialReportDialog(
            reportText = monthlyReport ?: "",
            isLoading = isReportLoading,
            onDismiss = { viewModel.dismissReport() }
        )
    }

    // 11. Investment Analytics & Annualized Return Dialog
    if (showInvestmentAnalytics) {
        InvestmentAnalyticsDialog(
            investments = investments,
            onDismissRequest = { showInvestmentAnalytics = false },
            onAskAiAdvisor = { prompt ->
                viewModel.sendChatMessage(prompt)
                viewModel.setActiveTab(4)
            }
        )
    }

    // 12. Lottery Analytics & Kelly Criterion Dialog
    if (showLotteryAnalytics) {
        LotteryAnalyticsDialog(
            lotteryRecords = lotteryRecords,
            onDismissRequest = { showLotteryAnalytics = false },
            onAskAiAdvisor = { prompt ->
                viewModel.sendChatMessage(prompt)
                viewModel.setActiveTab(4)
            }
        )
    }

    // 13. Financial Health & Emergency Safety Dialog
    if (showFinancialHealthDialog) {
        FinancialHealthDialog(
            expenses = allExpenses,
            savingsGoals = allGoals,
            investments = investments,
            onDismissRequest = { showFinancialHealthDialog = false },
            onAskAiAdvisor = { prompt ->
                viewModel.sendChatMessage(prompt)
                viewModel.setActiveTab(4)
            }
        )
    }

    // 14. Investment Return Calculator Dialog
    if (showInvestmentCalculator) {
        InvestmentCalculatorDialog(
            onDismissRequest = { showInvestmentCalculator = false },
            onSaveToInvestments = { newItem ->
                viewModel.addInvestment(newItem)
            }
        )
    }
}
