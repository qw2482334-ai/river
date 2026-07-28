package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val savingsGoalDao: SavingsGoalDao
) {

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    fun getExpensesByMonth(monthPrefix: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByMonth(monthPrefix)
    }

    suspend fun insert(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun insertExpenses(expenses: List<ExpenseEntity>) {
        expenseDao.insertExpenses(expenses)
    }

    suspend fun delete(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteById(id: Int) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun deleteAllExpenses() {
        expenseDao.deleteAllExpenses()
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

    suspend fun checkAndSeedInitialData() {
        if (expenseDao.getExpenseCount() == 0) {
            val today = LocalDate.now()
            val yearMonth = String.format("%04d-%02d", today.year, today.monthValue)
            val prevMonth = if (today.monthValue > 1) {
                String.format("%04d-%02d", today.year, today.monthValue - 1)
            } else {
                String.format("%04d-12", today.year - 1)
            }

            val seedList = listOf(
                ExpenseEntity(
                    amount = 38.5,
                    category = "餐饮",
                    date = "$yearMonth-${String.format("%02d", minOf(today.dayOfMonth, 25))}",
                    note = "午餐精选商务套餐",
                    type = "支出",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 12000.0,
                    category = "工资",
                    date = "$yearMonth-05",
                    note = "公司发薪日全额工资",
                    type = "收入",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 18.0,
                    category = "交通",
                    date = "$yearMonth-${String.format("%02d", minOf(today.dayOfMonth, 25))}",
                    note = "地铁出行与共享单车",
                    type = "支出",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 68.0,
                    category = "娱乐",
                    date = "$yearMonth-${String.format("%02d", maxOf(today.dayOfMonth - 2, 1))}",
                    note = "周末电影票与爆米花",
                    type = "支出",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 159.0,
                    category = "购物",
                    date = "$yearMonth-${String.format("%02d", maxOf(today.dayOfMonth - 3, 1))}",
                    note = "夏季纯棉T恤与生活用品",
                    type = "支出",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 2200.0,
                    category = "住房",
                    date = "$yearMonth-01",
                    note = "月度房租与物业管理费",
                    type = "支出",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 1500.0,
                    category = "兼职",
                    date = "$yearMonth-12",
                    note = "自由职业设计项目尾款",
                    type = "收入",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 45.0,
                    category = "餐饮",
                    date = "$yearMonth-${String.format("%02d", maxOf(today.dayOfMonth - 5, 1))}",
                    note = "朋友聚会饮品咖啡",
                    type = "支出",
                    ledger = "日常账本"
                ),
                ExpenseEntity(
                    amount = 850.0,
                    category = "交通",
                    date = "$yearMonth-10",
                    note = "出差高铁票与酒店费用",
                    type = "支出",
                    ledger = "工作报销"
                )
            )
            expenseDao.insertExpenses(seedList)
        }

        if (savingsGoalDao.getGoalCount() == 0) {
            val initialGoals = listOf(
                SavingsGoalEntity(
                    title = "换购旗舰手机",
                    targetAmount = 6999.0,
                    currentAmount = 4500.0,
                    emoji = "📱",
                    targetDate = "2026-10-01"
                ),
                SavingsGoalEntity(
                    title = "海岛年终度假基金",
                    targetAmount = 8000.0,
                    currentAmount = 3200.0,
                    emoji = "🏖️",
                    targetDate = "2026-12-25"
                ),
                SavingsGoalEntity(
                    title = "应急备用金储备",
                    targetAmount = 20000.0,
                    currentAmount = 15000.0,
                    emoji = "🛡️",
                    targetDate = "2026-12-31"
                )
            )
            savingsGoalDao.insertGoals(initialGoals)
        }
    }
}
