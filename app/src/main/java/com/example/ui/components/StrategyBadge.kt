package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StrategyState
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BullGreen
import com.example.ui.theme.TextSecondary

@Composable
fun StrategyBadge(
    state: StrategyState,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (state) {
        StrategyState.BUY_TRAP_CONFIRMED -> Triple(
            BullGreen.copy(alpha = 0.2f),
            BullGreen,
            BullGreen
        )
        StrategyState.SELL_TRAP_CONFIRMED -> Triple(
            BearRed.copy(alpha = 0.2f),
            BearRed,
            BearRed
        )
        StrategyState.BREAKOUT_HIGH,
        StrategyState.BREAKOUT_LOW -> Triple(
            BrandGold.copy(alpha = 0.15f),
            BrandGold,
            BrandGold
        )
        StrategyState.INSIDE_RANGE -> Triple(
            BrandCyan.copy(alpha = 0.1f),
            BrandCyan.copy(alpha = 0.5f),
            BrandCyan
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = state.labelArabic,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
