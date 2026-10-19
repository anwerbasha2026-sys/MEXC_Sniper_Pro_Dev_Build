package com.example.data.model

enum class TradeSide(val arabicLabel: String) {
    BUY("شراء"),
    SELL("بيع")
}

enum class TradeStatus(val arabicLabel: String) {
    OPEN("مفتوحة"),
    CLOSED_MANUAL("إغلاق يدوي"),
    CLOSED_TP("تحقيق الهدف (TP)"),
    CLOSED_SL("وقف الخسارة (SL)")
}

data class TradePosition(
    val id: String,
    val symbol: String,
    val side: TradeSide,
    val isDemo: Boolean,
    val entryPrice: Double,
    val currentPrice: Double,
    val amountUsdt: Double,
    val quantity: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val isHalfClosed: Boolean = false,
    val isBreakeven: Boolean = false,
    val status: TradeStatus = TradeStatus.OPEN,
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null,
    val pnlUsdt: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val executionOrderId: String? = null
)
