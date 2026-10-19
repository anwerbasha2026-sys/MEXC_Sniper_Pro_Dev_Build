package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoinStrategyAnalysis
import com.example.data.model.SignalSide
import com.example.data.model.StrategyState
import com.example.data.model.TradeSide
import com.example.data.model.TradingMode
import com.example.ui.components.StrategyBadge
import com.example.ui.components.StrategyGuideDialog
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
fun DashboardScreen(
    viewModel: TradingViewModel,
    onNavigateToChart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val tickers by viewModel.tickers.collectAsState()
    val scannerResults by viewModel.scannerResults.collectAsState()
    val openTrades by viewModel.openTrades.collectAsState()
    val mexcStatus by viewModel.mexcConnectionStatus.collectAsState()

    var showGuideDialog by remember { mutableStateOf(false) }

    val isDemo = userSettings.tradingMode == TradingMode.DEMO
    val balance = if (isDemo) userSettings.demoBalance else 0.0
    val totalUnrealizedPnl = openTrades.sumOf { it.pnlUsdt }

    // Find any active confirmed signals
    val confirmedSignals = scannerResults.filter {
        it.state == StrategyState.BUY_TRAP_CONFIRMED || it.state == StrategyState.SELL_TRAP_CONFIRMED
    }

    if (showGuideDialog) {
        StrategyGuideDialog(onDismiss = { showGuideDialog = false })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar: Mode Switcher & MEXC Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trading Mode Toggle Pill
                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(20.dp))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isDemo) DemoBadgeBg else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.toggleTradingMode(TradingMode.DEMO) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("mode_demo_button")
                    ) {
                        Text(
                            text = "تجريبي",
                            color = if (isDemo) DemoBadgeText else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (!isDemo) LiveBadgeBg else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.toggleTradingMode(TradingMode.LIVE) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("mode_live_button")
                    ) {
                        Text(
                            text = "حقيقي (MEXC)",
                            color = if (!isDemo) LiveBadgeText else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Guide & Refresh Buttons
                IconButton(
                    onClick = { showGuideDialog = true },
                    modifier = Modifier.testTag("guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "دليل الاستراتيجية",
                        tint = BrandGold
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshAll() },
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = BrandCyan
                    )
                }
            }
        }

        // MEXC Status Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp)
                        .background(if (mexcStatus.contains("🟢")) BullGreen else BrandGold, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mexcStatus,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Account / Wallet Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isDemo) "رصيد المحفظة الافتراضية (USDT)" else "رصيد حساب MEXC الحقيقي",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%,.2f", balance)} USDT",
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(if (isDemo) DemoBadgeBg else LiveBadgeBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDemo) "تداول تجريبي" else "MEXC LIVE",
                                color = if (isDemo) DemoBadgeText else LiveBadgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "الصفقات المفتوحة", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${openTrades.size} صفقات",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "الأرباح/الخسائر المباشرة", color = TextMuted, fontSize = 11.sp)
                            val pnlColor = if (totalUnrealizedPnl >= 0) BullGreen else BearRed
                            Text(
                                text = "${if (totalUnrealizedPnl >= 0) "+" else ""}${String.format(Locale.US, "%.2f", totalUnrealizedPnl)}$",
                                color = pnlColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "الحد الأقصى", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${userSettings.maxConcurrentTrades} صفقات",
                                color = BrandCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Signal Alert (If any coin triggered confirmed Buy or Sell trap)
        if (confirmedSignals.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    confirmedSignals.forEach { sig ->
                        val isBuy = sig.signalSide == SignalSide.BUY
                        val signalColor = if (isBuy) BullGreen else BearRed

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_signal_${sig.symbol}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(signalColor))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🚨 إشارة مصيدة سيولة مؤكدة!",
                                        color = signalColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    StrategyBadge(state = sig.state)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sig.symbol,
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "السعر: ${String.format(Locale.US, "%.2f", sig.currentPrice)}",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (isBuy) "شراء 🟢" else "بيع 🔴",
                                        color = signalColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sig.notes,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onNavigateToChart(sig.symbol) },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "عرض الشارت", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.executeInstantOrder(
                                                symbol = sig.symbol,
                                                side = if (isBuy) TradeSide.BUY else TradeSide.SELL,
                                                amountUsdt = userSettings.orderAmountUsdt,
                                                customSl = sig.stopLoss,
                                                customTp = sig.takeProfit
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = signalColor)
                                    ) {
                                        Text(
                                            text = "تنفيذ فوري (${userSettings.orderAmountUsdt}$)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Scanned Coins Watchlist
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رادار العملات واستراتيجية السيولة (${scannerResults.size} أزواج)",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "فريم 4س / 15د",
                    color = BrandGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Scanned Coins List
        if (scannerResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(DarkSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جاري فحص العملات واحتساب نطاقات السيولة من MEXC...",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(scannerResults) { item ->
                CoinScannerCard(
                    analysis = item,
                    onClick = { onNavigateToChart(item.symbol) }
                )
            }
        }
    }
}

@Composable
private fun CoinScannerCard(
    analysis: CoinStrategyAnalysis,
    onClick: () -> Unit
) {
    val isPositive = analysis.priceChangePercent >= 0
    val changeColor = if (isPositive) BullGreen else BearRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("coin_card_${analysis.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = analysis.symbol,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.2f", analysis.currentPrice)} USDT",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    StrategyBadge(state = analysis.state)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", analysis.priceChangePercent)}%",
                        color = changeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4H Box boundaries
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "قمة الصندوق 4س: ${String.format(Locale.US, "%.2f", analysis.boxHigh)}",
                    color = BrandGold,
                    fontSize = 10.sp
                )
                Text(
                    text = "قاع الصندوق 4س: ${String.format(Locale.US, "%.2f", analysis.boxLow)}",
                    color = BrandGold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
