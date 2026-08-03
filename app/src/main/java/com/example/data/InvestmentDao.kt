package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments WHERE userId = :userId")
    fun getInvestments(userId: Long): Flow<List<InvestmentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(item: InvestmentItem)

    @Update
    suspend fun updateInvestment(item: InvestmentItem)

    @Query("DELETE FROM investments WHERE id = :id AND userId = :userId")
    suspend fun deleteInvestment(id: String, userId: Long)
}

@Dao
interface LotteryDao {
    @Query("SELECT * FROM lotteries WHERE userId = :userId ORDER BY date DESC")
    fun getLotteries(userId: Long): Flow<List<LotteryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLottery(item: LotteryRecord)

    @Update
    suspend fun updateLottery(item: LotteryRecord)

    @Query("DELETE FROM lotteries WHERE id = :id AND userId = :userId")
    suspend fun deleteLottery(id: String, userId: Long)
}
