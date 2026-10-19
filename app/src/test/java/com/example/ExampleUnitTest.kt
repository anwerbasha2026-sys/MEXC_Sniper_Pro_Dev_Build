package com.example

import com.example.data.model.KlineCandle
import com.example.data.model.SignalSide
import com.example.data.model.StrategyState
import com.example.data.remote.MexcSignatureHelper
import com.example.data.repository.LiquidityTrapStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHmacSha256SignatureGeneration() {
        val queryString = "symbol=BTCUSDT&side=BUY&type=MARKET&quoteOrderQty=100&timestamp=1680000000000"
        val secretKey = "test_secret_key"
        val signature = MexcSignatureHelper.createHmacSha256(queryString, secretKey)
        assertNotNull(signature)
        assertTrue(signature.isNotEmpty())
        assertEquals(64, signature.length) // SHA256 hex string is 64 characters
    }

    @Test
    fun testLiquidityTrapStrategySellTrapDetection() {
        // Reference 4H candle: High = 65000, Low = 64000
        val ref4h = KlineCandle(
            openTime = 1000L,
            open = 64200.0,
            high = 65000.0,
            low = 64000.0,
            close = 64800.0,
            volume = 100.0,
            closeTime = 15000L
        )

        // 15m candle 1: inside box
        val c1 = KlineCandle(1000L, 64500.0, 64700.0, 64400.0, 64600.0, 10.0, 1900L)
        // 15m candle 2: breaks out ABOVE 65000 (close = 65200, high = 65350)
        val c2 = KlineCandle(2000L, 64700.0, 65350.0, 64700.0, 65200.0, 15.0, 2900L)
        // 15m candle 3: closes back INSIDE the box below 65000 (close = 64850) -> SELL TRAP CONFIRMED!
        val c3 = KlineCandle(3000L, 65100.0, 65150.0, 64800.0, 64850.0, 20.0, 3900L)
        // 15m candle 4: currently open candle
        val c4 = KlineCandle(4000L, 64850.0, 64900.0, 64750.0, 64820.0, 5.0, 4900L)

        val analysis = LiquidityTrapStrategy.analyze(
            symbol = "BTCUSDT",
            currentPrice = 64820.0,
            priceChange24h = 1.5,
            candles4h = listOf(ref4h, ref4h),
            candles15m = listOf(c1, c2, c3, c4),
            riskRewardRatio = 2.0
        )

        assertEquals(StrategyState.SELL_TRAP_CONFIRMED, analysis.state)
        assertEquals(SignalSide.SELL, analysis.signalSide)
        assertTrue(analysis.stopLoss > 64820.0) // Stop loss above entry
        assertTrue(analysis.takeProfit < 64820.0) // Take profit below entry for sell
        assertEquals(2.0, analysis.riskReward, 0.01)
    }

    @Test
    fun testLiquidityTrapStrategyBuyTrapDetection() {
        // Reference 4H candle: High = 65000, Low = 64000
        val ref4h = KlineCandle(
            openTime = 1000L,
            open = 64200.0,
            high = 65000.0,
            low = 64000.0,
            close = 64800.0,
            volume = 100.0,
            closeTime = 15000L
        )

        // 15m candle 1: inside box
        val c1 = KlineCandle(1000L, 64500.0, 64700.0, 64400.0, 64600.0, 10.0, 1900L)
        // 15m candle 2: breaks down BELOW 64000 (close = 63800, low = 63600)
        val c2 = KlineCandle(2000L, 64200.0, 64200.0, 63600.0, 63800.0, 15.0, 2900L)
        // 15m candle 3: closes back UP INSIDE the box above 64000 (close = 64150) -> BUY TRAP CONFIRMED!
        val c3 = KlineCandle(3000L, 63850.0, 64200.0, 63800.0, 64150.0, 20.0, 3900L)
        // 15m candle 4: currently open candle
        val c4 = KlineCandle(4000L, 64150.0, 64250.0, 64100.0, 64200.0, 5.0, 4900L)

        val analysis = LiquidityTrapStrategy.analyze(
            symbol = "BTCUSDT",
            currentPrice = 64200.0,
            priceChange24h = -0.8,
            candles4h = listOf(ref4h, ref4h),
            candles15m = listOf(c1, c2, c3, c4),
            riskRewardRatio = 2.0
        )

        assertEquals(StrategyState.BUY_TRAP_CONFIRMED, analysis.state)
        assertEquals(SignalSide.BUY, analysis.signalSide)
        assertTrue(analysis.stopLoss < 64200.0) // Stop loss below entry
        assertTrue(analysis.takeProfit > 64200.0) // Take profit above entry for buy
    }
}
