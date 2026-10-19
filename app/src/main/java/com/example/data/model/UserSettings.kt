package com.example.data.model

enum class TradingMode(val labelArabic: String) {
    DEMO("تداول تجريبي (أسعار حقيقية)"),
    LIVE("تداول حقيقي (منصة ميكس)")
}

data class UserSettings(
    val tradingMode: TradingMode = TradingMode.DEMO,
    val demoBalance: Double = 10000.0,
    val mexcApiKey: String = "",
    val mexcApiSecret: String = "",
    val scannedCoinsCount: Int = 10,
    val referenceTimeframe: String = "4h",
    val entryTimeframe: String = "15m",
    val orderAmountUsdt: Double = 100.0,
    val maxConcurrentTrades: Int = 3,
    val riskRewardRatio: Double = 2.0,
    val autoTradeEnabled: Boolean = false,
    val autoBreakevenAt1R: Boolean = true,
    val halfProfitAt1R: Boolean = true,
    val selectedCoins: List<String> = listOf(
        "BTCUSDT",
        "ETHUSDT",
        "SOLUSDT",
        "XRPUSDT",
        "DOGEUSDT",
        "BNBUSDT",
        "ADAUSDT",
        "AVAXUSDT",
        "SUIUSDT",
        "NEARUSDT"
    )
)
