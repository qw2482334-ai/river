sed -i '/fun addInvestment(item: InvestmentItem) {/,/}/c \    fun addInvestment(item: InvestmentItem) {\n        viewModelScope.launch {\n            repository.insertInvestment(item.copy(userId = _currentUserId.value))\n            _snackbarEvent.emit("📈 已添加持仓: ${item.name}")\n        }\n    }' app/src/main/java/com/example/ui/ExpenseViewModel.kt

sed -i '/fun deleteInvestment(id: String) {/,/}/c \    fun deleteInvestment(id: String) {\n        viewModelScope.launch {\n            repository.deleteInvestment(id, _currentUserId.value)\n            _snackbarEvent.emit("已移除该笔理财资产")\n        }\n    }' app/src/main/java/com/example/ui/ExpenseViewModel.kt

sed -i '/fun addLotteryRecord(item: LotteryRecord) {/,/}/c \    fun addLotteryRecord(item: LotteryRecord) {\n        viewModelScope.launch {\n            repository.insertLottery(item.copy(userId = _currentUserId.value))\n            _snackbarEvent.emit("⚽ 成功记录一笔【${item.type.label}】")\n        }\n    }' app/src/main/java/com/example/ui/ExpenseViewModel.kt

sed -i '/fun deleteLotteryRecord(id: String) {/,/}/c \    fun deleteLotteryRecord(id: String) {\n        viewModelScope.launch {\n            repository.deleteLottery(id, _currentUserId.value)\n            _snackbarEvent.emit("已删除该笔彩票记录")\n        }\n    }' app/src/main/java/com/example/ui/ExpenseViewModel.kt
