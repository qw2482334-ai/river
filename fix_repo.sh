sed -i 's/private val savingsGoalDao: SavingsGoalDao/private val savingsGoalDao: SavingsGoalDao,\n    private val investmentDao: InvestmentDao,\n    private val lotteryDao: LotteryDao/' app/src/main/java/com/example/data/ExpenseRepository.kt

cat << 'INNER_EOF' >> app/src/main/java/com/example/data/ExpenseRepository.kt

    fun getInvestments(userId: Long): Flow<List<InvestmentItem>> = investmentDao.getInvestments(userId)
    suspend fun insertInvestment(item: InvestmentItem) = investmentDao.insertInvestment(item)
    suspend fun updateInvestment(item: InvestmentItem) = investmentDao.updateInvestment(item)
    suspend fun deleteInvestment(id: String, userId: Long) = investmentDao.deleteInvestment(id, userId)

    fun getLotteries(userId: Long): Flow<List<LotteryRecord>> = lotteryDao.getLotteries(userId)
    suspend fun insertLottery(item: LotteryRecord) = lotteryDao.insertLottery(item)
    suspend fun updateLottery(item: LotteryRecord) = lotteryDao.updateLottery(item)
    suspend fun deleteLottery(id: String, userId: Long) = lotteryDao.deleteLottery(id, userId)
INNER_EOF
