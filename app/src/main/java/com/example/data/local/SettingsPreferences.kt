package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.TradingMode
import com.example.data.model.UserSettings

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mexc_trader_settings", Context.MODE_PRIVATE)

    fun getSettings(): UserSettings {
        val modeStr = prefs.getString("trading_mode", TradingMode.DEMO.name) ?: TradingMode.DEMO.name
        val tradingMode = try {
            TradingMode.valueOf(modeStr)
        } catch (e: Exception) {
            TradingMode.DEMO
        }

        val demoBalance = prefs.getFloat("demo_balance", 10000.0f).toDouble()
        val apiKey = prefs.getString("mexc_api_key", "") ?: ""
        val secretKey = prefs.getString("mexc_api_secret", "") ?: ""
        val scannedCoinsCount = prefs.getInt("scanned_coins_count", 10)
        val refTf = prefs.getString("reference_timeframe", "4h") ?: "4h"
        val entryTf = prefs.getString("entry_timeframe", "15m") ?: "15m"
        val orderAmount = prefs.getFloat("order_amount_usdt", 100.0f).toDouble()
        val maxTrades = prefs.getInt("max_concurrent_trades", 3)
        val rrRatio = prefs.getFloat("risk_reward_ratio", 2.0f).toDouble()
        val autoTrade = prefs.getBoolean("auto_trade_enabled", false)
        val autoBe = prefs.getBoolean("auto_breakeven_1r", true)
        val halfProfit = prefs.getBoolean("half_profit_1r", true)

        val coinsString = prefs.getString("selected_coins", null)
        val coins = if (!coinsString.isNullOrBlank()) {
            coinsString.split(",").filter { it.isNotBlank() }
        } else {
            listOf(
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
        }

        return UserSettings(
            tradingMode = tradingMode,
            demoBalance = demoBalance,
            mexcApiKey = apiKey,
            mexcApiSecret = secretKey,
            scannedCoinsCount = scannedCoinsCount,
            referenceTimeframe = refTf,
            entryTimeframe = entryTf,
            orderAmountUsdt = orderAmount,
            maxConcurrentTrades = maxTrades,
            riskRewardRatio = rrRatio,
            autoTradeEnabled = autoTrade,
            autoBreakevenAt1R = autoBe,
            halfProfitAt1R = halfProfit,
            selectedCoins = coins
        )
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit()
            .putString("trading_mode", settings.tradingMode.name)
            .putFloat("demo_balance", settings.demoBalance.toFloat())
            .putString("mexc_api_key", settings.mexcApiKey)
            .putString("mexc_api_secret", settings.mexcApiSecret)
            .putInt("scanned_coins_count", settings.scannedCoinsCount)
            .putString("reference_timeframe", settings.referenceTimeframe)
            .putString("entry_timeframe", settings.entryTimeframe)
            .putFloat("order_amount_usdt", settings.orderAmountUsdt.toFloat())
            .putInt("max_concurrent_trades", settings.maxConcurrentTrades)
            .putFloat("risk_reward_ratio", settings.riskRewardRatio.toFloat())
            .putBoolean("auto_trade_enabled", settings.autoTradeEnabled)
            .putBoolean("auto_breakeven_1r", settings.autoBreakevenAt1R)
            .putBoolean("half_profit_1r", settings.halfProfitAt1R)
            .putString("selected_coins", settings.selectedCoins.joinToString(","))
            .apply()
    }

    fun updateDemoBalance(newBalance: Double) {
        prefs.edit().putFloat("demo_balance", newBalance.toFloat()).apply()
    }
}
