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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TradeCard
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun PositionsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val openTrades by viewModel.openTrades.collectAsState()
    val closedTrades by viewModel.closedTrades.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Open, 1 = Closed

    val totalRealizedPnl = closedTrades.sumOf { it.pnlUsdt }
    val winningTrades = closedTrades.count { it.pnlUsdt > 0 }
    val winRate = if (closedTrades.isNotEmpty()) (winningTrades.toDouble() / closedTrades.size.toDouble()) * 100.0 else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab Selector Row
        item {
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
                            if (selectedTab == 0) BrandCyan else DarkSurfaceElevated,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_open_trades"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "الصفقات المفتوحة (${openTrades.size})",
                        color = if (selectedTab == 0) androidx.compose.ui.graphics.Color.Black else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selectedTab == 1) BrandCyan else DarkSurfaceElevated,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_closed_trades"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "سجل الصفقات (${closedTrades.size})",
                        color = if (selectedTab == 1) androidx.compose.ui.graphics.Color.Black else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Performance Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "إجمالي الأرباح المحققة", color = TextMuted, fontSize = 11.sp)
                        val pnlColor = if (totalRealizedPnl >= 0) BullGreen else BearRed
                        Text(
                            text = "${if (totalRealizedPnl >= 0) "+" else ""}${String.format(Locale.US, "%.2f", totalRealizedPnl)} $",
                            color = pnlColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "نسبة النجاح (Win Rate)", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", winRate)}%",
                            color = BrandGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "الصفقات المنجزة", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${closedTrades.size} صفقات",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // List Content
        if (selectedTab == 0) {
            if (openTrades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(DarkSurface, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📊", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد صفقات مفتوحة حالياً",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يمكنك فتح صفقات من شاشة الشارت أو تفعيل التنفيذ التلقائي للاستراتيجية",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(openTrades) { trade ->
                    TradeCard(
                        trade = trade,
                        onCloseTrade = { viewModel.closePosition(it) },
                        onTakeHalfProfit = { viewModel.takeHalfProfit(it) },
                        onMoveBreakeven = { viewModel.moveBreakeven(it) }
                    )
                }
            }
        } else {
            if (closedTrades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(DarkSurface, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "سجل الصفقات المنتهية فارغ",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(closedTrades) { trade ->
                    TradeCard(
                        trade = trade,
                        onCloseTrade = {},
                        onTakeHalfProfit = {},
                        onMoveBreakeven = {}
                    )
                }
            }
        }
    }
}
