package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_positions WHERE status = 'OPEN' ORDER BY openTime DESC")
    fun getOpenTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trade_positions WHERE status != 'OPEN' ORDER BY closeTime DESC")
    fun getClosedTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trade_positions ORDER BY openTime DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trade_positions WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: String): TradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Query("DELETE FROM trade_positions WHERE id = :id")
    suspend fun deleteTrade(id: String)

    @Query("DELETE FROM trade_positions WHERE isDemo = 1")
    suspend fun clearDemoTrades()
}
