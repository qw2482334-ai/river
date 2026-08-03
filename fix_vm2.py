import re

with open('app/src/main/java/com/example/ui/ExpenseViewModel.kt', 'r') as f:
    text = f.read()

# Fix sendChatMessage
text = text.replace(
"""        _chatMessages.value = currentMsgs
            _isAiThinking.value = true""", 
"""        _chatMessages.value = currentMsgs
        viewModelScope.launch {
            _isAiThinking.value = true""")

text = text.replace(
""".onFailure { err ->
                val updated = _chatMessages.value.toMutableList()""",
""".onFailure { err ->
                val updated = _chatMessages.value.toMutableList()""")

# Fix refreshMarketQuotes
text = text.replace(
"""    fun refreshMarketQuotes() {
    }""",
"""    fun refreshMarketQuotes() {
        viewModelScope.launch {
            _isLoadingMarket.value = true
            _snackbarEvent.emit("💹 正在联网同步最新公募基金/证券估值行情...")
            val currentInvestments = investments.value
            currentInvestments.forEach { item ->
                val result = marketService.fetchFundQuote(item.code, item.principal, item.currentValue)
                result.getOrNull()?.let { quote ->
                    repository.updateInvestment(item.copy(currentValue = quote.estimatedValue))
                }
            }
            _isLoadingMarket.value = false
            _snackbarEvent.emit("✅ 证券理财持仓估值已完成实时行情更新！")
        }
    }""")

with open('app/src/main/java/com/example/ui/ExpenseViewModel.kt', 'w') as f:
    f.write(text)
