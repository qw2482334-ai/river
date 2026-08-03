sed -i 's/val allExpenses: StateFlow<List<ExpenseEntity>> = _selectedLedger.flatMapLatest { ledger ->/private val _currentUserId = kotlinx.coroutines.flow.MutableStateFlow(0L)\n\n    fun setUserId(id: Long) {\n        _currentUserId.value = id\n        kotlinx.coroutines.GlobalScope.launch { repository.seedInitialDataIfEmpty(id) }\n    }\n\n    val allExpenses: StateFlow<List<ExpenseEntity>> = kotlinx.coroutines.flow.combine(_currentUserId, _selectedLedger) { uid, ledger -> Pair(uid, ledger) }.flatMapLatest { (uid, ledger) ->/' app/src/main/java/com/example/ui/ExpenseViewModel.kt

sed -i 's/if (ledger == "全部账本") repository.allExpenses/if (ledger == "全部账本") repository.getAllExpenses(uid)/' app/src/main/java/com/example/ui/ExpenseViewModel.kt

sed -i 's/else repository.getExpensesByLedger(ledger)/else repository.getExpensesByLedger(uid, ledger)/' app/src/main/java/com/example/ui/ExpenseViewModel.kt

sed -i 's/val allGoals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals/val allGoals: StateFlow<List<SavingsGoalEntity>> = _currentUserId.flatMapLatest { uid -> repository.getAllGoals(uid) }/' app/src/main/java/com/example/ui/ExpenseViewModel.kt
