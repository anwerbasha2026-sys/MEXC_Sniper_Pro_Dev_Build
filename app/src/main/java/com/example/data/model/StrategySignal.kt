package com.example.data.model

enum class StrategyState(val labelArabic: String) {
    INSIDE_RANGE("داخل الصندوق"),
    BREAKOUT_HIGH("اختراق علوي (ترقب مصيدة)"),
    BREAKOUT_LOW("كسر سفلي (ترقب مصيدة)"),
    SELL_TRAP_CONFIRMED("مصيدة بيع مؤكدة 🔴"),
    BUY_TRAP_CONFIRMED("مصيدة شراء مؤكدة 🟢")
}

enum class SignalSide {
    BUY,
    SELL,
    NONE
}

data class CoinStrategyAnalysis(
    val symbol: String,
    val currentPrice: Double,
    val priceChangePercent: Double,
    val boxHigh: Double,
    val boxLow: Double,
    val state: StrategyState,
    val signalSide: SignalSide,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskReward: Double = 2.0,
    val breakoutHighPrice: Double = 0.0,
    val breakoutLowPrice: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val notes: String = ""
)
