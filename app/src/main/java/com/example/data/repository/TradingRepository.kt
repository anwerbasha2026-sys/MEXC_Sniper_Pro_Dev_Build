package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsPreferences
import com.example.data.local.TradeEntity
import com.example.data.model.CoinStrategyAnalysis
import com.example.data.model.KlineCandle
import com.example.data.model.Mexc24HrTicker
import com.example.data.model.SignalSide
import com.example.data.model.TradePosition
import com.example.data.model.TradeSide
import com.example.data.model.TradeStatus
import com.example.data.model.TradingMode
import com.example.data.model.UserSettings
import com.example.data.remote.MexcApiClient
import com.example.data.remote.MexcApiService
import com.example.data.remote.MexcSignatureHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class TradingRepository(
    private val context: Context,
    private val apiService: MexcApiService = MexcApiClient.apiService,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val preferences: SettingsPreferences = SettingsPreferences(context)
) {
    private val tradeDao = database.tradeDao()

    private val _tickers = MutableStateFlow<Map<String, Mexc24HrTicker>>(emptyMap())
    val tickers: StateFlow<Map<String, Mexc24HrTicker>> = _tickers.asStateFlow()

    private val _scannerResults = MutableStateFlow<List<CoinStrategyAnalysis>>(emptyList())
    val scannerResults: StateFlow<List<CoinStrategyAnalysis>> = _scannerResults.asStateFlow()

    private val _userSettings = MutableStateFlow(preferences.getSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _mexcConnectionStatus = MutableStateFlow("جاري التحقق...")
    val mexcConnectionStatus: StateFlow<String> = _mexcConnectionStatus.asStateFlow()

    val openTradesFlow: Flow<List<TradePosition>> = tradeDao.getOpenTrades().map { list ->
        list.map { it.toDomain() }
    }

    val closedTradesFlow: Flow<List<TradePosition>> = tradeDao.getClosedTrades().map { list ->
        list.map { it.toDomain() }
    }

    fun getSettings(): UserSettings = _userSettings.value

    fun updateSettings(newSettings: UserSettings) {
        preferences.saveSettings(newSettings)
        _userSettings.value = newSettings
    }

    fun resetDemoBalance(amount: Double = 10000.0) {
        val current = _userSettings.value
        val updated = current.copy(demoBalance = amount)
        updateSettings(updated)
    }

    suspend fun testMexcConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            apiService.ping()
            val time = apiService.getServerTime()
            val settings = _userSettings.value

            if (settings.mexcApiKey.isNotBlank() && settings.mexcApiSecret.isNotBlank()) {
                val timestamp = time.serverTime
                val queryString = "timestamp=$timestamp&recvWindow=10000"
                val signature = MexcSignatureHelper.createHmacSha256(queryString, settings.mexcApiSecret)
                val account = apiService.getAccount(
                    apiKey = settings.mexcApiKey,
                    timestamp = timestamp,
                    signature = signature
                )
                val canTrade = account.canTrade ?: true
                _mexcConnectionStatus.value = "متصل بحساب MEXC بنجاح 🟢"
                Pair(true, "تم الاتصال بحساب MEXC بنجاح! التداول متاح: $canTrade")
            } else {
                _mexcConnectionStatus.value = "متصل ببيانات MEXC العامة 🟢"
                Pair(true, "تم الاتصال بـ MEXC بنجاح (بيانات الأسعار والشارت متاحة). أدخل مفاتيح API للتداول الحقيقي.")
            }
        } catch (e: Exception) {
            val msg = e.message ?: "فشل الاتصال بـ MEXC"
            _mexcConnectionStatus.value = "فشل الاتصال: $msg"
            Pair(false, msg)
        }
    }

    suspend fun fetchMarketTickers() = withContext(Dispatchers.IO) {
        try {
            val list = apiService.get24HrTickers()
            val map = list.associateBy { it.symbol }
            _tickers.value = map
            _mexcConnectionStatus.value = "متصل بـ MEXC 🟢"

            // Update open positions' PnL with live market price and check TP/SL
            evaluateOpenPositions(map)
        } catch (e: Exception) {
            // Keep existing tickers or handle error silently
        }
    }

    suspend fun fetchKlines(symbol: String, interval: String, limit: Int = 100): List<KlineCandle> =
        withContext(Dispatchers.IO) {
            try {
                val raw = apiService.getKlines(symbol, interval, limit)
                raw.mapNotNull { KlineCandle.fromMexcArray(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun runScanner() = withContext(Dispatchers.IO) {
        val settings = _userSettings.value
        val coinsToScan = settings.selectedCoins.take(settings.scannedCoinsCount)
        val tickerMap = _tickers.value

        val results = mutableListOf<CoinStrategyAnalysis>()

        for (sym in coinsToScan) {
            try {
                val currentPrice = tickerMap[sym]?.lastPriceDouble ?: 0.0
                val priceChange = tickerMap[sym]?.changePercentDouble ?: 0.0
                if (currentPrice <= 0.0) continue

                val klines4h = fetchKlines(sym, settings.referenceTimeframe, 20)
                val klines15m = fetchKlines(sym, settings.entryTimeframe, 60)

                val analysis = LiquidityTrapStrategy.analyze(
                    symbol = sym,
                    currentPrice = currentPrice,
                    priceChange24h = priceChange,
                    candles4h = klines4h,
                    candles15m = klines15m,
                    riskRewardRatio = settings.riskRewardRatio
                )
                results.add(analysis)

                // If Auto-Trade is enabled, check if we should auto-enter
                if (settings.autoTradeEnabled) {
                    checkAndAutoTrade(analysis)
                }
            } catch (e: Throwable) {
                // Ignore single pair error during batch scan
            }
        }

        _scannerResults.value = results
    }

    private suspend fun checkAndAutoTrade(analysis: CoinStrategyAnalysis) {
        if (analysis.signalSide == SignalSide.NONE) return

        val currentOpen = tradeDao.getOpenTrades().firstOrNull() ?: emptyList()
        val settings = _userSettings.value
        if (currentOpen.size >= settings.maxConcurrentTrades) return

        val alreadyOpenForSymbol = currentOpen.any { it.symbol == analysis.symbol }
        if (alreadyOpenForSymbol) return

        // Auto execute!
        val side = if (analysis.signalSide == SignalSide.BUY) TradeSide.BUY else TradeSide.SELL
        placeInstantTrade(
            symbol = analysis.symbol,
            side = side,
            amountUsdt = settings.orderAmountUsdt,
            customSl = analysis.stopLoss,
            customTp = analysis.takeProfit
        )
    }

    suspend fun placeInstantTrade(
        symbol: String,
        side: TradeSide,
        amountUsdt: Double,
        customSl: Double? = null,
        customTp: Double? = null
    ): Result<TradePosition> = withContext(Dispatchers.IO) {
        val settings = _userSettings.value
        val ticker = _tickers.value[symbol]
        val currentPrice = ticker?.lastPriceDouble ?: 0.0

        if (currentPrice <= 0.0) {
            return@withContext Result.failure(Exception("لم يتم العثور على سعر السوق المباشر لـ $symbol"))
        }

        val quantity = amountUsdt / currentPrice
        val defaultRisk = currentPrice * 0.015 // 1.5% default risk
        val sl = customSl ?: if (side == TradeSide.BUY) currentPrice - defaultRisk else currentPrice + defaultRisk
        val tp = customTp ?: if (side == TradeSide.BUY) {
            currentPrice + (currentPrice - sl) * settings.riskRewardRatio
        } else {
            currentPrice - (sl - currentPrice) * settings.riskRewardRatio
        }

        if (settings.tradingMode == TradingMode.DEMO) {
            // Verify demo balance
            if (settings.demoBalance < amountUsdt) {
                return@withContext Result.failure(Exception("رصيد الحساب التجريبي غير كافٍ. يمكنك شحن الرصيد من لوحة التحكم."))
            }

            // Deduct demo balance
            val updatedBalance = settings.demoBalance - amountUsdt
            preferences.updateDemoBalance(updatedBalance)
            _userSettings.value = settings.copy(demoBalance = updatedBalance)

            val position = TradePosition(
                id = UUID.randomUUID().toString(),
                symbol = symbol,
                side = side,
                isDemo = true,
                entryPrice = currentPrice,
                currentPrice = currentPrice,
                amountUsdt = amountUsdt,
                quantity = quantity,
                stopLoss = sl,
                takeProfit = tp,
                status = TradeStatus.OPEN,
                openTime = System.currentTimeMillis()
            )

            tradeDao.insertTrade(TradeEntity.fromDomain(position))
            Result.success(position)
        } else {
            // Live Trading on MEXC
            if (settings.mexcApiKey.isBlank() || settings.mexcApiSecret.isBlank()) {
                return@withContext Result.failure(Exception("يرجى إدخال مفتاح API و Secret الخاص بـ MEXC في لوحة التحكم للتداول الحقيقي."))
            }

            try {
                val time = apiService.getServerTime()
                val timestamp = time.serverTime
                val sideStr = if (side == TradeSide.BUY) "BUY" else "SELL"
                val typeStr = "MARKET"

                val queryString = "symbol=$symbol&side=$sideStr&type=$typeStr&quoteOrderQty=$amountUsdt&timestamp=$timestamp&recvWindow=10000"
                val signature = MexcSignatureHelper.createHmacSha256(queryString, settings.mexcApiSecret)

                val response = apiService.placeOrder(
                    apiKey = settings.mexcApiKey,
                    symbol = symbol,
                    side = sideStr,
                    type = typeStr,
                    quoteOrderQty = amountUsdt,
                    timestamp = timestamp,
                    signature = signature
                )

                if (response.code != null && response.code != 200 && response.code != 0) {
                    return@withContext Result.failure(Exception("خطأ من MEXC: ${response.msg ?: "كود الخطأ: ${response.code}"}"))
                }

                val position = TradePosition(
                    id = response.orderId ?: UUID.randomUUID().toString(),
                    symbol = symbol,
                    side = side,
                    isDemo = false,
                    entryPrice = currentPrice,
                    currentPrice = currentPrice,
                    amountUsdt = amountUsdt,
                    quantity = quantity,
                    stopLoss = sl,
                    takeProfit = tp,
                    status = TradeStatus.OPEN,
                    openTime = System.currentTimeMillis(),
                    executionOrderId = response.orderId
                )

                tradeDao.insertTrade(TradeEntity.fromDomain(position))
                Result.success(position)
            } catch (e: Exception) {
                Result.failure(Exception("فشل تنفيذ الأمر على MEXC: ${e.message}"))
            }
        }
    }

    suspend fun closeTrade(tradeId: String, manualClose: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        val entity = tradeDao.getTradeById(tradeId) ?: return@withContext Result.failure(Exception("الصفقة غير موجودة"))
        val domain = entity.toDomain()
        val currentPrice = _tickers.value[domain.symbol]?.lastPriceDouble ?: domain.currentPrice

        val pnl = calculatePnl(domain.side, domain.entryPrice, currentPrice, domain.quantity)
        val pnlPercent = if (domain.amountUsdt > 0) (pnl / domain.amountUsdt) * 100.0 else 0.0

        val updated = domain.copy(
            status = if (manualClose) TradeStatus.CLOSED_MANUAL else domain.status,
            closeTime = System.currentTimeMillis(),
            currentPrice = currentPrice,
            pnlUsdt = pnl,
            pnlPercent = pnlPercent
        )

        tradeDao.updateTrade(TradeEntity.fromDomain(updated))

        // If Demo trade, return invested amount + PnL
        if (domain.isDemo) {
            val currentSettings = _userSettings.value
            val returnAmount = domain.amountUsdt + pnl
            val newBalance = maxOf(0.0, currentSettings.demoBalance + returnAmount)
            preferences.updateDemoBalance(newBalance)
            _userSettings.value = currentSettings.copy(demoBalance = newBalance)
        }

        Result.success(Unit)
    }

    suspend fun closeHalfTradeAt1R(tradeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val entity = tradeDao.getTradeById(tradeId) ?: return@withContext Result.failure(Exception("الصفقة غير موجودة"))
        val domain = entity.toDomain()
        if (domain.isHalfClosed) return@withContext Result.failure(Exception("تم بالفعل حجز 50% من هذه الصفقة مسبقاً"))

        val currentPrice = _tickers.value[domain.symbol]?.lastPriceDouble ?: domain.currentPrice
        val halfQuantity = domain.quantity / 2.0
        val halfAmount = domain.amountUsdt / 2.0
        val realizedPnlHalf = calculatePnl(domain.side, domain.entryPrice, currentPrice, halfQuantity)

        val updated = domain.copy(
            isHalfClosed = true,
            isBreakeven = true,
            stopLoss = domain.entryPrice, // Move stop loss to breakeven!
            quantity = domain.quantity - halfQuantity,
            amountUsdt = domain.amountUsdt - halfAmount
        )

        tradeDao.updateTrade(TradeEntity.fromDomain(updated))

        if (domain.isDemo) {
            val currentSettings = _userSettings.value
            val returnAmount = halfAmount + realizedPnlHalf
            val newBalance = currentSettings.demoBalance + returnAmount
            preferences.updateDemoBalance(newBalance)
            _userSettings.value = currentSettings.copy(demoBalance = newBalance)
        }

        Result.success(Unit)
    }

    suspend fun moveStopLossToBreakeven(tradeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val entity = tradeDao.getTradeById(tradeId) ?: return@withContext Result.failure(Exception("الصفقة غير موجودة"))
        val domain = entity.toDomain()

        val updated = domain.copy(
            isBreakeven = true,
            stopLoss = domain.entryPrice
        )

        tradeDao.updateTrade(TradeEntity.fromDomain(updated))
        Result.success(Unit)
    }

    private suspend fun evaluateOpenPositions(tickerMap: Map<String, Mexc24HrTicker>) {
        val openTrades = tradeDao.getOpenTrades().firstOrNull() ?: return

        for (entity in openTrades) {
            val trade = entity.toDomain()
            val liveTicker = tickerMap[trade.symbol] ?: continue
            val livePrice = liveTicker.lastPriceDouble
            if (livePrice <= 0.0) continue

            val pnl = calculatePnl(trade.side, trade.entryPrice, livePrice, trade.quantity)
            val pnlPercent = if (trade.amountUsdt > 0) (pnl / trade.amountUsdt) * 100.0 else 0.0

            var statusToSet: TradeStatus? = null

            // Check Take Profit
            if (trade.side == TradeSide.BUY && livePrice >= trade.takeProfit) {
                statusToSet = TradeStatus.CLOSED_TP
            } else if (trade.side == TradeSide.SELL && livePrice <= trade.takeProfit) {
                statusToSet = TradeStatus.CLOSED_TP
            }

            // Check Stop Loss
            if (trade.side == TradeSide.BUY && livePrice <= trade.stopLoss) {
                statusToSet = TradeStatus.CLOSED_SL
            } else if (trade.side == TradeSide.SELL && livePrice >= trade.stopLoss) {
                statusToSet = TradeStatus.CLOSED_SL
            }

            if (statusToSet != null) {
                val closedTrade = trade.copy(
                    status = statusToSet,
                    closeTime = System.currentTimeMillis(),
                    currentPrice = livePrice,
                    pnlUsdt = pnl,
                    pnlPercent = pnlPercent
                )
                tradeDao.updateTrade(TradeEntity.fromDomain(closedTrade))

                if (trade.isDemo) {
                    val currentSettings = _userSettings.value
                    val returnAmount = trade.amountUsdt + pnl
                    val newBalance = maxOf(0.0, currentSettings.demoBalance + returnAmount)
                    preferences.updateDemoBalance(newBalance)
                    _userSettings.value = currentSettings.copy(demoBalance = newBalance)
                }
            } else {
                // Update live PnL & currentPrice in DB
                val updated = trade.copy(
                    currentPrice = livePrice,
                    pnlUsdt = pnl,
                    pnlPercent = pnlPercent
                )
                tradeDao.updateTrade(TradeEntity.fromDomain(updated))
            }
        }
    }

    private fun calculatePnl(side: TradeSide, entry: Double, current: Double, qty: Double): Double {
        return if (side == TradeSide.BUY) {
            (current - entry) * qty
        } else {
            (entry - current) * qty
        }
    }
}
