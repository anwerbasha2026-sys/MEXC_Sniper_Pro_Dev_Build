package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TradePosition
import com.example.data.model.TradeSide
import com.example.data.model.TradeStatus

@Entity(tableName = "trade_positions")
data class TradeEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val side: String, // BUY, SELL
    val isDemo: Boolean,
    val entryPrice: Double,
    val currentPrice: Double,
    val amountUsdt: Double,
    val quantity: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val isHalfClosed: Boolean,
    val isBreakeven: Boolean,
    val status: String, // OPEN, CLOSED_MANUAL, CLOSED_TP, CLOSED_SL
    val openTime: Long,
    val closeTime: Long?,
    val pnlUsdt: Double,
    val pnlPercent: Double,
    val executionOrderId: String?
) {
    fun toDomain(): TradePosition {
        return TradePosition(
            id = id,
            symbol = symbol,
            side = if (side == "BUY") TradeSide.BUY else TradeSide.SELL,
            isDemo = isDemo,
            entryPrice = entryPrice,
            currentPrice = currentPrice,
            amountUsdt = amountUsdt,
            quantity = quantity,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            isHalfClosed = isHalfClosed,
            isBreakeven = isBreakeven,
            status = when (status) {
                "CLOSED_TP" -> TradeStatus.CLOSED_TP
                "CLOSED_SL" -> TradeStatus.CLOSED_SL
                "CLOSED_MANUAL" -> TradeStatus.CLOSED_MANUAL
                else -> TradeStatus.OPEN
            },
            openTime = openTime,
            closeTime = closeTime,
            pnlUsdt = pnlUsdt,
            pnlPercent = pnlPercent,
            executionOrderId = executionOrderId
        )
    }

    companion object {
        fun fromDomain(domain: TradePosition): TradeEntity {
            return TradeEntity(
                id = domain.id,
                symbol = domain.symbol,
                side = domain.side.name,
                isDemo = domain.isDemo,
                entryPrice = domain.entryPrice,
                currentPrice = domain.currentPrice,
                amountUsdt = domain.amountUsdt,
                quantity = domain.quantity,
                stopLoss = domain.stopLoss,
                takeProfit = domain.takeProfit,
                isHalfClosed = domain.isHalfClosed,
                isBreakeven = domain.isBreakeven,
                status = domain.status.name,
                openTime = domain.openTime,
                closeTime = domain.closeTime,
                pnlUsdt = domain.pnlUsdt,
                pnlPercent = domain.pnlPercent,
                executionOrderId = domain.executionOrderId
            )
        }
    }
}
