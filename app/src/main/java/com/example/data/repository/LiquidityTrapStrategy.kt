package com.example.data.repository

import com.example.data.model.CoinStrategyAnalysis
import com.example.data.model.KlineCandle
import com.example.data.model.SignalSide
import com.example.data.model.StrategyState

object LiquidityTrapStrategy {

    /**
     * Analyzes 4H reference candles and 15m entry candles according to the Liquidity Trap Strategy.
     */
    fun analyze(
        symbol: String,
        currentPrice: Double,
        priceChange24h: Double,
        candles4h: List<KlineCandle>,
        candles15m: List<KlineCandle>,
        riskRewardRatio: Double = 2.0
    ): CoinStrategyAnalysis {
        if (candles4h.isEmpty() || candles15m.size < 3) {
            return CoinStrategyAnalysis(
                symbol = symbol,
                currentPrice = currentPrice,
                priceChangePercent = priceChange24h,
                boxHigh = currentPrice * 1.02,
                boxLow = currentPrice * 0.98,
                state = StrategyState.INSIDE_RANGE,
                signalSide = SignalSide.NONE,
                stopLoss = 0.0,
                takeProfit = 0.0,
                riskReward = riskRewardRatio,
                notes = "جاري تجميع بيانات الشموع من MEXC..."
            )
        }

        // Reference 4H candle: The first 4H candle of current session or the most recent completed 4H candle
        val refCandle = if (candles4h.size >= 2) candles4h[candles4h.size - 2] else candles4h.last()
        val boxHigh = refCandle.high
        val boxLow = refCandle.low

        // Analyze 15m candles
        // Look at the last few candles (excluding open candle or considering the latest closed candles)
        val closedCandles = if (candles15m.size > 1) candles15m.subList(0, candles15m.size - 1) else candles15m
        val recentCandles = closedCandles.takeLast(12) // Last ~3 hours of 15m candles

        var hadBreakoutAbove = false
        var highestAbove = boxHigh
        var breakoutAboveIndex = -1

        var hadBreakoutBelow = false
        var lowestBelow = boxLow
        var breakoutBelowIndex = -1

        // Scan from oldest in recent window to newest
        for (i in recentCandles.indices) {
            val c = recentCandles[i]
            // Breakout above box: body closes above boxHigh
            if (c.close > boxHigh) {
                hadBreakoutAbove = true
                breakoutAboveIndex = i
                if (c.high > highestAbove) highestAbove = c.high
            }
            // Breakout below box: body closes below boxLow
            if (c.close < boxLow) {
                hadBreakoutBelow = true
                breakoutBelowIndex = i
                if (c.low < lowestBelow) lowestBelow = c.low
            }
        }

        val lastClosedCandle = closedCandles.last()

        // Check for Sell Trap (Liquidity Grab at the Highs):
        // 1. Breakout happened above boxHigh
        // 2. Latest closed candle closed back inside the box (close < boxHigh)
        if (hadBreakoutAbove && breakoutAboveIndex >= 0 && lastClosedCandle.close < boxHigh && lastClosedCandle.openTime > recentCandles[breakoutAboveIndex].openTime) {
            val stopLoss = highestAbove * 1.002 // Slightly above highest wick
            val risk = stopLoss - currentPrice
            val takeProfit = if (risk > 0) currentPrice - (risk * riskRewardRatio) else currentPrice * 0.96

            return CoinStrategyAnalysis(
                symbol = symbol,
                currentPrice = currentPrice,
                priceChangePercent = priceChange24h,
                boxHigh = boxHigh,
                boxLow = boxLow,
                state = StrategyState.SELL_TRAP_CONFIRMED,
                signalSide = SignalSide.SELL,
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                riskReward = riskRewardRatio,
                breakoutHighPrice = highestAbove,
                notes = "تم رصد امتصاص سيولة الشراء وكسر كاذب للقمة - إشارة بيع R:R 1:2"
            )
        }

        // Check for Buy Trap (Liquidity Grab at the Lows):
        // 1. Breakout happened below boxLow
        // 2. Latest closed candle closed back inside the box (close > boxLow)
        if (hadBreakoutBelow && breakoutBelowIndex >= 0 && lastClosedCandle.close > boxLow && lastClosedCandle.openTime > recentCandles[breakoutBelowIndex].openTime) {
            val stopLoss = lowestBelow * 0.998 // Slightly below lowest wick
            val risk = currentPrice - stopLoss
            val takeProfit = if (risk > 0) currentPrice + (risk * riskRewardRatio) else currentPrice * 1.04

            return CoinStrategyAnalysis(
                symbol = symbol,
                currentPrice = currentPrice,
                priceChangePercent = priceChange24h,
                boxHigh = boxHigh,
                boxLow = boxLow,
                state = StrategyState.BUY_TRAP_CONFIRMED,
                signalSide = SignalSide.BUY,
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                riskReward = riskRewardRatio,
                breakoutLowPrice = lowestBelow,
                notes = "تم رصد امتصاص سيولة البيع وكسر كاذب للقاع - إشارة شراء R:R 1:2"
            )
        }

        // If currently above boxHigh
        if (currentPrice > boxHigh) {
            return CoinStrategyAnalysis(
                symbol = symbol,
                currentPrice = currentPrice,
                priceChangePercent = priceChange24h,
                boxHigh = boxHigh,
                boxLow = boxLow,
                state = StrategyState.BREAKOUT_HIGH,
                signalSide = SignalSide.NONE,
                stopLoss = highestAbove,
                takeProfit = 0.0,
                riskReward = riskRewardRatio,
                breakoutHighPrice = highestAbove,
                notes = "السعر أعلى الصندوق - بانتظار إغلاق شمعة 15د داخل النطاق لتأكيد مصيدة البيع"
            )
        }

        // If currently below boxLow
        if (currentPrice < boxLow) {
            return CoinStrategyAnalysis(
                symbol = symbol,
                currentPrice = currentPrice,
                priceChangePercent = priceChange24h,
                boxHigh = boxHigh,
                boxLow = boxLow,
                state = StrategyState.BREAKOUT_LOW,
                signalSide = SignalSide.NONE,
                stopLoss = lowestBelow,
                takeProfit = 0.0,
                riskReward = riskRewardRatio,
                breakoutLowPrice = lowestBelow,
                notes = "السعر أدنى الصندوق - بانتظار إغلاق شمعة 15د داخل النطاق لتأكيد مصيدة الشراء"
            )
        }

        // Inside Range
        return CoinStrategyAnalysis(
            symbol = symbol,
            currentPrice = currentPrice,
            priceChangePercent = priceChange24h,
            boxHigh = boxHigh,
            boxLow = boxLow,
            state = StrategyState.INSIDE_RANGE,
            signalSide = SignalSide.NONE,
            stopLoss = 0.0,
            takeProfit = 0.0,
            riskReward = riskRewardRatio,
            notes = "السعر يتحرك داخل صندوق التوازن الأولي لليوم"
        )
    }
}
