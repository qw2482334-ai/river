package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    fun getExpensesByLedger(ledgerName: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByLedger(ledgerName)
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

    suspend fun seedInitialDataIfEmpty() {
        val existingExpenses = allExpenses.first()
        if (existingExpenses.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L

            val initialExpenses = listOf(
                ExpenseEntity(
                    title = "七月份基本工资",
                    amount = 18500.0,
                    type = TransactionType.INCOME.name,
                    category = "工资",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 1,
                    note = "公司代扣社保后实发"
                ),
                ExpenseEntity(
                    title = "星巴克生椰拿铁与三明治",
                    amount = 48.0,
                    type = TransactionType.EXPENSE.name,
                    category = "餐饮",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 1,
                    note = "工作日早午餐"
                ),
                ExpenseEntity(
                    title = "地铁交通卡充值",
                    amount = 200.0,
                    type = TransactionType.EXPENSE.name,
                    category = "交通",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 2,
                    note = "自动扣款续费"
                ),
                ExpenseEntity(
                    title = "山姆会员店周末大采购",
                    amount = 688.50,
                    type = TransactionType.EXPENSE.name,
                    category = "购物",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 3,
                    note = "购买生鲜牛肉、牛奶和水果"
                ),
                ExpenseEntity(
                    title = "精装公寓房租与物业费",
                    amount = 3200.0,
                    type = TransactionType.EXPENSE.name,
                    category = "居住",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 5,
                    note = "月度例行缴费"
                ),
                ExpenseEntity(
                    title = "朋友聚餐与看电影",
                    amount = 320.0,
                    type = TransactionType.EXPENSE.name,
                    category = "娱乐",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 6,
                    note = "周末IMAX观影"
                ),
                ExpenseEntity(
                    title = "理财基金季度分红",
                    amount = 1200.0,
                    type = TransactionType.INCOME.name,
                    category = "理财",
                    ledgerName = "日常账本",
                    dateMillis = now - dayMs * 8,
                    note = "稳健型基金派息"
                ),
                ExpenseEntity(
                    title = "三亚度假酒店预订",
                    amount = 2400.0,
                    type = TransactionType.EXPENSE.name,
                    category = "娱乐",
                    ledgerName = "旅游专项",
                    dateMillis = now - dayMs * 10,
                    note = "夏日度假定金"
                ),
                ExpenseEntity(
                    title = "往返机票两张",
                    amount = 1860.0,
                    type = TransactionType.EXPENSE.name,
                    category = "交通",
                    ledgerName = "旅游专项",
                    dateMillis = now - dayMs * 12,
                    note = "早鸟特价机票"
                )
            )

            for (expense in initialExpenses) {
                expenseDao.insertExpense(expense)
            }
        }

        val existingGoals = allGoals.first()
        if (existingGoals.isEmpty()) {
            val now = System.currentTimeMillis()
            val monthMs = 30 * 24 * 3600 * 1000L

            val initialGoals = listOf(
                SavingsGoalEntity(
                    title = "更换最新款 M3 Max MacBook Pro",
                    targetAmount = 21999.0,
                    currentAmount = 14500.0,
                    category = "数码",
                    targetDateMillis = now + monthMs * 2,
                    iconName = "laptop",
                    note = "提升开发效率与剪辑速度"
                ),
                SavingsGoalEntity(
                    title = "日本京都关西红叶双人自由行",
                    targetAmount = 18000.0,
                    currentAmount = 8200.0,
                    category = "旅游",
                    targetDateMillis = now + monthMs * 4,
                    iconName = "flight",
                    note = "预算包括温泉酒店与赏枫门票"
                ),
                SavingsGoalEntity(
                    title = "6个月紧急六位数备用金储备",
                    targetAmount = 50000.0,
                    currentAmount = 35000.0,
                    category = "储蓄",
                    targetDateMillis = now + monthMs * 6,
                    iconName = "shield",
                    note = "存入高利息大额存单"
                )
            )

            for (goal in initialGoals) {
                savingsGoalDao.insertGoal(goal)
            }
        }
    }
}
