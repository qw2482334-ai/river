sed -i 's/entities = \[ExpenseEntity::class, SavingsGoalEntity::class\]/entities = \[ExpenseEntity::class, SavingsGoalEntity::class, UserEntity::class\]/' app/src/main/java/com/example/data/AppDatabase.kt
sed -i 's/version = 1,/version = 2,/' app/src/main/java/com/example/data/AppDatabase.kt
sed -i '/abstract fun savingsGoalDao(): SavingsGoalDao/a \    abstract fun userDao(): UserDao' app/src/main/java/com/example/data/AppDatabase.kt
