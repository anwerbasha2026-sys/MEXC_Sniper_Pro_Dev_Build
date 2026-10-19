package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradePosition
import com.example.data.model.TradeSide
import com.example.data.model.TradeStatus
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DemoBadgeBg
import com.example.ui.theme.DemoBadgeText
import com.example.ui.theme.LiveBadgeBg
import com.example.ui.theme.LiveBadgeText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradeCard(
    trade: TradePosition,
    onCloseTrade: (String) -> Unit,
    onTakeHalfProfit: (String) -> Unit,
    onMoveBreakeven: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOpen = trade.status == TradeStatus.OPEN
    val isProfit = trade.pnlUsdt >= 0
    val pnlColor = if (isProfit) BullGreen else BearRed
    val sideColor = if (trade.side == TradeSide.BUY) BullGreen else BearRed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("trade_card_${trade.id}")
    ) {
        // Header Row: Symbol, Side, Mode badge, and Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trade.symbol,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(sideColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = trade.side.arabicLabel,
                    color = sideColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(if (trade.isDemo) DemoBadgeBg else LiveBadgeBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (trade.isDemo) "تجريبي" else "ميكس حقيقي",
                    color = if (trade.isDemo) DemoBadgeText else LiveBadgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            if (!isOpen) {
                Text(
                    text = trade.status.arabicLabel,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            } else {
                Text(
                    text = if (isProfit) "+${String.format(Locale.US, "%.2f", trade.pnlUsdt)}$" else "${String.format(Locale.US, "%.2f", trade.pnlUsdt)}$",
                    color = pnlColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price details grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "سعر الدخول", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = String.format(Locale.US, "%.2f", trade.entryPrice),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column {
                Text(text = "السعر الحالي", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = String.format(Locale.US, "%.2f", trade.currentPrice),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column {
                Text(text = "وقف الخسارة (SL)", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = String.format(Locale.US, "%.2f", trade.stopLoss),
                    color = BearRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column {
                Text(text = "الهدف (TP 2R)", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = String.format(Locale.US, "%.2f", trade.takeProfit),
                    color = BullGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom stats: Investment amount & PnL %
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المبلغ: ${String.format(Locale.US, "%.1f", trade.amountUsdt)} USDT",
                color = TextSecondary,
                fontSize = 11.sp
            )
            if (trade.isHalfClosed) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "• محجوز 50%", color = BrandGold, fontSize = 11.sp)
            }
            if (trade.isBreakeven) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "• مؤمنة (BE)", color = BrandCyan, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", trade.pnlPercent)}%",
                color = pnlColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Action Buttons for Open Trades
        if (isOpen) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!trade.isHalfClosed) {
                    OutlinedButton(
                        onClick = { onTakeHalfProfit(trade.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGold)
                    ) {
                        Text(text = "حجز 50% + تأمين", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!trade.isBreakeven) {
                    OutlinedButton(
                        onClick = { onMoveBreakeven(trade.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandCyan)
                    ) {
                        Text(text = "نقل الوقف للدخول", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { onCloseTrade(trade.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BearRed.copy(alpha = 0.85f))
                ) {
                    Text(text = "إغلاق فوري", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            // Time closed
            trade.closeTime?.let { closeTime ->
                val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(closeTime))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "وقت الإغلاق: $dateStr",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
