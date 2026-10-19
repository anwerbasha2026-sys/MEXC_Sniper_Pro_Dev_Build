package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KlineCandle
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun CandlestickChartView(
    candles: List<KlineCandle>,
    referenceBox: Pair<Double, Double>?,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "جاري تحميل بيانات الشموع من منصة MEXC...",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
        return
    }

    // Determine min and max price across candles and reference box
    val maxCandlePrice = candles.maxOfOrNull { it.high } ?: 100.0
    val minCandlePrice = candles.minOfOrNull { it.low } ?: 0.0

    val boxHigh = referenceBox?.first ?: maxCandlePrice
    val boxLow = referenceBox?.second ?: minCandlePrice

    val highestPrice = maxOf(maxCandlePrice, boxHigh) * 1.002
    val lowestPrice = minOf(minCandlePrice, boxLow) * 0.998
    val priceRange = if (highestPrice - lowestPrice > 0) highestPrice - lowestPrice else 1.0

    Column(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        // Legend row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(8.dp)
                    .background(BrandGold.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "نطاق 4س المرجعي: ${String.format(Locale.US, "%.2f", boxHigh)} - ${String.format(Locale.US, "%.2f", boxLow)}",
                color = BrandGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "السعر الحالي: ${String.format(Locale.US, "%.2f", currentPrice)}",
                color = BullGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val candleCount = minOf(candles.size, 45)
            val displayCandles = candles.takeLast(candleCount)

            fun getY(price: Double): Float {
                val ratio = (price - lowestPrice) / priceRange
                return (height - (ratio * height)).toFloat()
            }

            // Draw Reference Box Area (4H Box)
            val boxTopY = getY(boxHigh).coerceIn(0f, height)
            val boxBottomY = getY(boxLow).coerceIn(0f, height)
            val boxHeight = maxOf(2f, boxBottomY - boxTopY)

            // Shaded box representing liquidity reference zone
            drawRect(
                color = BrandGold.copy(alpha = 0.12f),
                topLeft = Offset(0f, boxTopY),
                size = Size(width, boxHeight)
            )

            // Upper limit line (boxHigh)
            drawLine(
                color = BrandGold,
                start = Offset(0f, boxTopY),
                end = Offset(width, boxTopY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )

            // Lower limit line (boxLow)
            drawLine(
                color = BrandGold,
                start = Offset(0f, boxBottomY),
                end = Offset(width, boxBottomY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )

            // Draw Grid Lines
            val gridSteps = 4
            for (i in 1 until gridSteps) {
                val y = height * (i.toFloat() / gridSteps)
                drawLine(
                    color = DarkCardBorder.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw Candlesticks
            val slotWidth = width / candleCount
            val candleBodyWidth = maxOf(2f, slotWidth * 0.7f)

            displayCandles.forEachIndexed { index, candle ->
                val centerX = (index * slotWidth) + (slotWidth / 2f)
                val openY = getY(candle.open)
                val closeY = getY(candle.close)
                val highY = getY(candle.high)
                val lowY = getY(candle.low)

                val isGreen = candle.isBullish
                val candleColor = if (isGreen) BullGreen else BearRed

                // Draw Wick (thin vertical line)
                drawLine(
                    color = candleColor,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, lowY),
                    strokeWidth = 1.5f
                )

                // Draw Candle Body (rectangle)
                val bodyTop = minOf(openY, closeY)
                val bodyBottom = maxOf(openY, closeY)
                val bodyHeight = maxOf(2f, bodyBottom - bodyTop)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(centerX - (candleBodyWidth / 2f), bodyTop),
                    size = Size(candleBodyWidth, bodyHeight)
                )
            }

            // Draw Current Price Line
            val currentPriceY = getY(currentPrice).coerceIn(0f, height)
            drawLine(
                color = BullGreen,
                start = Offset(0f, currentPriceY),
                end = Offset(width, currentPriceY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        }
    }
}
