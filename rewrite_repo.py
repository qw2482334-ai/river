code = """package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val investmentDao: InvestmentDao,
    private val lotteryDao: LotteryDao
) {
    fun getAllExpenses(userId: Long): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses(userId)
    fun getAllGoals(userId: Long): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals(userId)
    fun getExpensesByLedger(userId: Long, ledgerName: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByLedger(userId, ledgerName)
    }
    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }
    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }
    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }
    suspend fun insertGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insertGoal(goal)
    }
    suspend fun updateGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }
    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoal(goal)
    }
    suspend fun deleteGoalById(id: Long) {
        savingsGoalDao.deleteGoalById(id)
    }

    fun getInvestments(userId: Long): Flow<List<InvestmentItem>> = investmentDao.getInvestments(userId)
    suspend fun insertInvestment(item: InvestmentItem) = investmentDao.insertInvestment(item)
    suspend fun updateInvestment(item: InvestmentItem) = investmentDao.updateInvestment(item)
    suspend fun deleteInvestment(id: String, userId: Long) = investmentDao.deleteInvestment(id, userId)

    fun getLotteries(userId: Long): Flow<List<LotteryRecord>> = lotteryDao.getLotteries(userId)
    suspend fun insertLottery(item: LotteryRecord) = lotteryDao.insertLottery(item)
    suspend fun updateLottery(item: LotteryRecord) = lotteryDao.updateLottery(item)
    suspend fun deleteLottery(id: String, userId: Long) = lotteryDao.deleteLottery(id, userId)

    suspend fun seedInitialDataIfEmpty(userId: Long) {
        val existingExpenses = getAllExpenses(userId).first()
        if (existingExpenses.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L
            val initialExpenses = listOf(
                ExpenseEntity(userId = userId, title = "基本工资", amount = 18500.0, type = TransactionType.INCOME.name, category = "工资", ledgerName = "日常账本", dateMillis = now - dayMs * 1, note = "实发"),
                ExpenseEntity(userId = userId, title = "星巴克", amount = 48.0, type = TransactionType.EXPENSE.name, category = "餐饮", ledgerName = "日常账本", dateMillis = now - dayMs * 1, note = "早午餐")
            )
            for (expense in initialExpenses) {
                expenseDao.insertExpense(expense)
            }
        }
        val existingGoals = getAllGoals(userId).first()
        if (existingGoals.isEmpty()) {
            val now = System.currentTimeMillis()
            val monthMs = 30 * 24 * 3600 * 1000L
            val initialGoals = listOf(
                SavingsGoalEntity(userId = userId, title = "备用金", targetAmount = 50000.0, currentAmount = 35000.0, category = "储蓄", targetDateMillis = now + monthMs * 6, iconName = "shield", note = "存入")
            )
            for (goal in initialGoals) {
                savingsGoalDao.insertGoal(goal)
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/data/ExpenseRepository.kt', 'w') as f:
    f.write(code)
