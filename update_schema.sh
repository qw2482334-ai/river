sed -i 's/val id: Long = 0,/val id: Long = 0,\n    val userId: Long = 0,/' app/src/main/java/com/example/data/ExpenseEntity.kt
sed -i 's/val id: Long = 0,/val id: Long = 0,\n    val userId: Long = 0,/' app/src/main/java/com/example/data/SavingsGoalEntity.kt

sed -i 's/SELECT \* FROM expenses/SELECT \* FROM expenses WHERE userId = :userId/' app/src/main/java/com/example/data/ExpenseDao.kt
sed -i 's/fun getAllExpenses()/fun getAllExpenses(userId: Long)/' app/src/main/java/com/example/data/ExpenseDao.kt
sed -i 's/fun getExpensesByLedger(ledgerName: String)/fun getExpensesByLedger(userId: Long, ledgerName: String)/' app/src/main/java/com/example/data/ExpenseDao.kt

sed -i 's/SELECT \* FROM savings_goals/SELECT \* FROM savings_goals WHERE userId = :userId/' app/src/main/java/com/example/data/SavingsGoalDao.kt
sed -i 's/fun getAllGoals()/fun getAllGoals(userId: Long)/' app/src/main/java/com/example/data/SavingsGoalDao.kt
