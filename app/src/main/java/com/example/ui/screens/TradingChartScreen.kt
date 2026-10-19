package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StrategyState
import com.example.data.model.TradeSide
import com.example.data.model.TradingMode
import com.example.ui.components.CandlestickChartView
import com.example.ui.components.StrategyBadge
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DemoBadgeBg
import com.example.ui.theme.DemoBadgeText
import com.example.ui.theme.LiveBadgeBg
import com.example.ui.theme.LiveBadgeText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun TradingChartScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSymbol by viewModel.selectedSymbol.collectAsState()
    val selectedInterval by viewModel.selectedInterval.collectAsState()
    val chartCandles by viewModel.chartCandles.collectAsState()
    val referenceBox by viewModel.referenceBox.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()
    val tickers by viewModel.tickers.collectAsState()
    val scannerResults by viewModel.scannerResults.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val ticker = tickers[selectedSymbol]
    val currentPrice = ticker?.lastPriceDouble ?: (chartCandles.lastOrNull()?.close ?: 0.0)
    val priceChange = ticker?.changePercentDouble ?: 0.0
    val analysis = scannerResults.firstOrNull { it.symbol == selectedSymbol }

    var tradeSide by remember { mutableStateOf(TradeSide.BUY) }
    var orderAmountText by remember { mutableStateOf(userSettings.orderAmountUsdt.toInt().toString()) }

    val amountDouble = orderAmountText.toDoubleOrNull() ?: 100.0

    // Auto-calculate SL and TP based on strategy 1:2 R:R
    val defaultRisk = if (currentPrice > 0) currentPrice * 0.015 else 100.0
    val calculatedSl = if (tradeSide == TradeSide.BUY) {
        analysis?.stopLoss?.takeIf { it > 0 && it < currentPrice } ?: (currentPrice - defaultRisk)
    } else {
        analysis?.stopLoss?.takeIf { it > 0 && it > currentPrice } ?: (currentPrice + defaultRisk)
    }

    val riskDiff = if (tradeSide == TradeSide.BUY) (currentPrice - calculatedSl) else (calculatedSl - currentPrice)
    val calculatedTp = if (tradeSide == TradeSide.BUY) {
        currentPrice + (riskDiff * userSettings.riskRewardRatio)
    } else {
        currentPrice - (riskDiff * userSettings.riskRewardRatio)
    }

    val isDemo = userSettings.tradingMode == TradingMode.DEMO
    val isProfit = priceChange >= 0
    val pnlColor = if (isProfit) BullGreen else BearRed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Pairs Horizontal Selector
        val availableCoins = userSettings.selectedCoins
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableCoins) { coin ->
                val isSelected = coin == selectedSymbol
                val coinTicker = tickers[coin]
                val coinPrice = coinTicker?.lastPriceDouble ?: 0.0

                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) BrandCyan.copy(alpha = 0.2f) else DarkSurface,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) BrandCyan else DarkCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectSymbol(coin) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("pair_chip_$coin")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = coin,
                            color = if (isSelected) BrandCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (coinPrice > 0) {
                            Text(
                                text = String.format(Locale.US, "%.2f", coinPrice),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Ticker Header & Timeframe Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedSymbol,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,.2f", currentPrice)} USDT",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .background(pnlColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", priceChange)}%",
                            color = pnlColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    analysis?.let {
                        StrategyBadge(state = it.state)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timeframe buttons
                val intervals = listOf("5m", "15m", "1h", "4h")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    intervals.forEach { tf ->
                        val isSel = tf == selectedInterval
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSel) BrandCyan else DarkSurfaceElevated,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectInterval(tf) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tf == "15m") "15د (دخول)" else if (tf == "4h") "4س (نطاق)" else tf,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chart Component with Overlay Box
        Box(modifier = Modifier.fillMaxWidth()) {
            CandlestickChartView(
                candles = chartCandles,
                referenceBox = referenceBox,
                currentPrice = currentPrice,
                modifier = Modifier.fillMaxWidth()
            )

            if (isChartLoading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Instant Order Placement Panel
        Card(
            modifier = Modifier.fillMaxWidth().testTag("order_panel"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Side Selector (Buy vs Sell)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (tradeSide == TradeSide.BUY) BullGreen else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { tradeSide = TradeSide.BUY }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "شراء فوري (BUY / LONG)",
                            color = if (tradeSide == TradeSide.BUY) Color.Black else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (tradeSide == TradeSide.SELL) BearRed else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { tradeSide = TradeSide.SELL }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بيع فوري (SELL / SHORT)",
                            color = if (tradeSide == TradeSide.SELL) Color.White else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                Text(text = "مبلغ الصفقة (USDT)", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = orderAmountText,
                    onValueChange = { orderAmountText = it },
                    modifier = Modifier.fillMaxWidth().testTag("order_amount_input"),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    trailingIcon = {
                        Text(text = "USDT", color = BrandCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick amount chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("25", "50", "100", "250", "500").forEach { quickVal ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .clickable { orderAmountText = quickVal }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$$quickVal", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Strategy Calculation (SL & TP 1:2 R:R)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "وقف الخسارة التلقائي (SL)", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = String.format(Locale.US, "%.2f", calculatedSl),
                            color = BearRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(text = "الهدف التلقائي (TP 2R)", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = String.format(Locale.US, "%.2f", calculatedTp),
                            color = BullGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(text = "نسبة R:R", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "1 : ${userSettings.riskRewardRatio}",
                            color = BrandGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Execution Button
                val buttonColor = if (tradeSide == TradeSide.BUY) BullGreen else BearRed
                Button(
                    onClick = {
                        viewModel.executeInstantOrder(
                            symbol = selectedSymbol,
                            side = tradeSide,
                            amountUsdt = amountDouble,
                            customSl = calculatedSl,
                            customTp = calculatedTp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("execute_order_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    val modeLabel = if (isDemo) "تداول تجريبي" else "تنفيذ مباشر على MEXC"
                    Text(
                        text = "تنفيذ أمر ${tradeSide.arabicLabel} فوراً ($modeLabel)",
                        color = if (tradeSide == TradeSide.BUY) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
