with open('app/src/main/java/com/example/ui/ExpenseViewModel.kt', 'r') as f:
    text = f.read()

insight_impl = """
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
"""

text = text.replace("    fun fetchOnlineInsight() {}", insight_impl.strip())

rates_impl = """
    fun refreshExchangeRates() {
        viewModelScope.launch {
            _snackbarEvent.emit("💱 正在更新实时汇率...")
            // Simulated update
            _exchangeRates.value = mapOf("CNY" to 1.0, "USD" to 0.138, "EUR" to 0.128, "JPY" to 21.2, "HKD" to 1.08, "GBP" to 0.109, "KRW" to 190.5)
            _snackbarEvent.emit("✅ 汇率已更新！")
        }
    }
"""
text = text.replace("    fun refreshExchangeRates() {}", rates_impl.strip())

with open('app/src/main/java/com/example/ui/ExpenseViewModel.kt', 'w') as f:
    f.write(text)
