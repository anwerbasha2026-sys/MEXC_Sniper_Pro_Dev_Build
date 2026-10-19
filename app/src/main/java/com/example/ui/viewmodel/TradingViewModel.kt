package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CoinStrategyAnalysis
import com.example.data.model.KlineCandle
import com.example.data.model.Mexc24HrTicker
import com.example.data.model.TradePosition
import com.example.data.model.TradeSide
import com.example.data.model.TradingMode
import com.example.data.model.UserSettings
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TradingRepository(application.applicationContext)

    val userSettings: StateFlow<UserSettings> = repository.userSettings
    val tickers: StateFlow<Map<String, Mexc24HrTicker>> = repository.tickers
    val scannerResults: StateFlow<List<CoinStrategyAnalysis>> = repository.scannerResults
    val mexcConnectionStatus: StateFlow<String> = repository.mexcConnectionStatus

    val openTrades: StateFlow<List<TradePosition>> = repository.openTradesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val closedTrades: StateFlow<List<TradePosition>> = repository.closedTradesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedSymbol = MutableStateFlow("BTCUSDT")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    private val _selectedInterval = MutableStateFlow("15m")
    val selectedInterval: StateFlow<String> = _selectedInterval.asStateFlow()

    private val _chartCandles = MutableStateFlow<List<KlineCandle>>(emptyList())
    val chartCandles: StateFlow<List<KlineCandle>> = _chartCandles.asStateFlow()

    private val _referenceBox = MutableStateFlow<Pair<Double, Double>?>(null)
    val referenceBox: StateFlow<Pair<Double, Double>?> = _referenceBox.asStateFlow()

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private var pollingJob: Job? = null
    private var chartJob: Job? = null

    init {
        startPolling()
        loadChartData(_selectedSymbol.value, _selectedInterval.value)
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            // Initial checks
            repository.testMexcConnection()
            var tickCount = 0
            while (isActive) {
                try {
                    repository.fetchMarketTickers()
                    // Every ~12 seconds (every 4 loops of 3s), re-run strategy scanner
                    if (tickCount % 4 == 0) {
                        repository.runScanner()
                    }
                    tickCount++
                } catch (e: Exception) {
                    // Ignore transient network errors
                }
                delay(3000)
            }
        }
    }

    fun selectSymbol(symbol: String) {
        if (_selectedSymbol.value == symbol) return
        _selectedSymbol.value = symbol
        loadChartData(symbol, _selectedInterval.value)
    }

    fun selectInterval(interval: String) {
        if (_selectedInterval.value == interval) return
        _selectedInterval.value = interval
        loadChartData(_selectedSymbol.value, interval)
    }

    fun refreshAll() {
        viewModelScope.launch {
            repository.fetchMarketTickers()
            repository.runScanner()
            loadChartData(_selectedSymbol.value, _selectedInterval.value)
            _userMessage.emit("تم تحديث أسعار السوق وفحص الاستراتيجية بنجاح 🔄")
        }
    }

    fun loadChartData(symbol: String, interval: String) {
        chartJob?.cancel()
        chartJob = viewModelScope.launch {
            _isChartLoading.value = true
            try {
                val candles = repository.fetchKlines(symbol, interval, 80)
                _chartCandles.value = candles

                // Fetch 4H reference candle for the Liquidity Box
                val candles4h = repository.fetchKlines(symbol, "4h", 10)
                if (candles4h.isNotEmpty()) {
                    val refCandle = if (candles4h.size >= 2) candles4h[candles4h.size - 2] else candles4h.last()
                    _referenceBox.value = Pair(refCandle.high, refCandle.low)
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isChartLoading.value = false
            }
        }
    }

    fun toggleTradingMode(mode: TradingMode) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(tradingMode = mode))
            _userMessage.emit("تم التبديل إلى: ${mode.labelArabic}")
        }
    }

    fun executeInstantOrder(
        symbol: String,
        side: TradeSide,
        amountUsdt: Double,
        customSl: Double? = null,
        customTp: Double? = null
    ) {
        viewModelScope.launch {
            val result = repository.placeInstantTrade(symbol, side, amountUsdt, customSl, customTp)
            result.onSuccess { pos ->
                val modeLabel = if (pos.isDemo) "تجريبي" else "حقيقي MEXC"
                _userMessage.emit("تم تنفيذ صفقة ${pos.side.arabicLabel} على $symbol بقيمة $amountUsdt$ بنجاح ($modeLabel) 🚀")
            }.onFailure { err ->
                _userMessage.emit("تعذر تنفيذ الصفقة: ${err.message}")
            }
        }
    }

    fun closePosition(tradeId: String) {
        viewModelScope.launch {
            val res = repository.closeTrade(tradeId, manualClose = true)
            res.onSuccess {
                _userMessage.emit("تم إغلاق الصفقة بنجاح وحساب الأرباح/الخسائر 💰")
            }.onFailure {
                _userMessage.emit("فشل إغلاق الصفقة: ${it.message}")
            }
        }
    }

    fun takeHalfProfit(tradeId: String) {
        viewModelScope.launch {
            val res = repository.closeHalfTradeAt1R(tradeId)
            res.onSuccess {
                _userMessage.emit("تم حجز 50% من الأرباح وتأمين الصفقة بنقل الوقف لنقطة الدخول (Breakeven) 🛡️")
            }.onFailure {
                _userMessage.emit(it.message ?: "حدث خطأ")
            }
        }
    }

    fun moveBreakeven(tradeId: String) {
        viewModelScope.launch {
            val res = repository.moveStopLossToBreakeven(tradeId)
            res.onSuccess {
                _userMessage.emit("تم نقل وقف الخسارة إلى سعر الدخول بدون مخاطرة 🛡️")
            }.onFailure {
                _userMessage.emit(it.message ?: "حدث خطأ")
            }
        }
    }

    fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _userMessage.emit("تم حفظ إعدادات التطبيق والاستراتيجية بنجاح ✅")
            repository.runScanner()
        }
    }

    fun resetDemoBalance(amount: Double) {
        viewModelScope.launch {
            repository.resetDemoBalance(amount)
            _userMessage.emit("تم تعيين رصيد الحساب التجريبي إلى $amount USDT بنجاح 💵")
        }
    }

    fun testMexcApiConnection() {
        viewModelScope.launch {
            val (ok, msg) = repository.testMexcConnection()
            _userMessage.emit(msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        chartJob?.cancel()
    }
}
