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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingMode
import com.example.data.model.UserSettings
import com.example.ui.components.StrategyGuideDialog
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandBlue
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
fun SettingsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val mexcStatus by viewModel.mexcConnectionStatus.collectAsState()

    var showGuideDialog by remember { mutableStateOf(false) }

    var currentMode by remember(userSettings) { mutableStateOf(userSettings.tradingMode) }
    var apiKeyText by remember(userSettings) { mutableStateOf(userSettings.mexcApiKey) }
    var apiSecretText by remember(userSettings) { mutableStateOf(userSettings.mexcApiSecret) }
    var scannedCoinsCount by remember(userSettings) { mutableFloatStateOf(userSettings.scannedCoinsCount.toFloat()) }
    var referenceTf by remember(userSettings) { mutableStateOf(userSettings.referenceTimeframe) }
    var entryTf by remember(userSettings) { mutableStateOf(userSettings.entryTimeframe) }
    var orderAmountText by remember(userSettings) { mutableStateOf(userSettings.orderAmountUsdt.toInt().toString()) }
    var maxTradesCount by remember(userSettings) { mutableFloatStateOf(userSettings.maxConcurrentTrades.toFloat()) }
    var rrRatio by remember(userSettings) { mutableFloatStateOf(userSettings.riskRewardRatio.toFloat()) }
    var autoTradeEnabled by remember(userSettings) { mutableStateOf(userSettings.autoTradeEnabled) }
    var autoBreakeven by remember(userSettings) { mutableStateOf(userSettings.autoBreakevenAt1R) }

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
        // Page Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "لوحة التحكم والإعدادات",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "إعدادات الربط الحقيقي، استراتيجية السيولة، وإدارة المخاطر",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = { showGuideDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BrandGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "دليل الاستراتيجية", color = BrandGold, fontSize = 11.sp)
                }
            }
        }

        // Section 1: Trading Mode & Demo Wallet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. وضع التداول (تجريبي أم حقيقي)",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

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
                                    if (currentMode == TradingMode.DEMO) DemoBadgeBg else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { currentMode = TradingMode.DEMO }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تداول تجريبي (أسعار حقيقية)",
                                color = if (currentMode == TradingMode.DEMO) DemoBadgeText else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (currentMode == TradingMode.LIVE) LiveBadgeBg else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { currentMode = TradingMode.LIVE }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تداول حقيقي على MEXC",
                                color = if (currentMode == TradingMode.LIVE) LiveBadgeText else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (currentMode == TradingMode.DEMO) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رصيد الحساب التجريبي الحالي: ${String.format(Locale.US, "%,.0f", userSettings.demoBalance)} USDT",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.resetDemoBalance(10000.0) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold.copy(alpha = 0.2f))
                            ) {
                                Text(text = "إعادة ضبط (10,000$)", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: MEXC API Real Integration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "2. الربط الحقيقي بمنصة ميكس (MEXC API)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "Spot API v3", color = BrandCyan, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("MEXC API Key", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiSecretText,
                        onValueChange = { apiSecretText = it },
                        label = { Text("MEXC Secret Key", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("api_secret_input"),
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                // Save first then test
                                val temp = userSettings.copy(mexcApiKey = apiKeyText, mexcApiSecret = apiSecretText)
                                viewModel.saveSettings(temp)
                                viewModel.testMexcApiConnection()
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text(text = "فحص الاتصال الفعلي بـ MEXC", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الحالة: $mexcStatus",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Section 3: Strategy Settings (Scanned coins, Timeframes, Trade size, Max trades)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. إعدادات استراتيجية مصيدة السيولة (Liquidity Trap)",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scanned Coins Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "عدد العملات التي يتم فحصها:", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${scannedCoinsCount.toInt()} عملات",
                            color = BrandCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = scannedCoinsCount,
                        onValueChange = { scannedCoinsCount = it },
                        valueRange = 3f..15f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reference Timeframe (4H / 1H / 1D)
                    Text(text = "فريم النطاق المرجعي (صندوق السيولة):", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("4h" to "4 ساعات (الموصى به)", "1h" to "ساعة واحدة", "1d" to "يومي").forEach { (tf, lbl) ->
                            val isSel = referenceTf == tf
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) BrandGold.copy(alpha = 0.2f) else DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) BrandGold else DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { referenceTf = tf }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = lbl, color = if (isSel) BrandGold else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Entry Timeframe (15m / 5m / 30m / 1h)
                    Text(text = "فريم رصد وتأكيد الدخول:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("15m" to "15 دقيقة (المثالي)", "5m" to "5 دقائق", "30m" to "30 دقيقة").forEach { (tf, lbl) ->
                            val isSel = entryTf == tf
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) BrandCyan.copy(alpha = 0.2f) else DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) BrandCyan else DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { entryTf = tf }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = lbl, color = if (isSel) BrandCyan else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trade Order Amount
                    Text(text = "مبلغ الصفقة الافتراضي (USDT):", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = orderAmountText,
                        onValueChange = { orderAmountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("USDT", color = BrandCyan, fontSize = 12.sp, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Max Concurrent Trades
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الحد الأقصى للصفقات المتزامنة:", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${maxTradesCount.toInt()} صفقات",
                            color = BrandCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = maxTradesCount,
                        onValueChange = { maxTradesCount = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Risk / Reward Ratio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "نسبة العائد للمخاطرة (R:R):", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "1 : ${String.format(Locale.US, "%.1f", rrRatio)}",
                            color = BullGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = rrRatio,
                        onValueChange = { rrRatio = it },
                        valueRange = 1.0f..4.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = BullGreen, activeTrackColor = BullGreen)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto Trading Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "التنفيذ التلقائي للصفقات (Auto-Trading)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "فتح الصفقة آلياً فور تأكيد مصيدة السيولة",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoTradeEnabled,
                            onCheckedChange = { autoTradeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BullGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto Breakeven at 1R
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تأمين الدخول (Breakeven) وحجز 50% عند 1R",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "كما هو موصى به تماماً في دليل الاستراتيجية",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoBreakeven,
                            onCheckedChange = { autoBreakeven = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandCyan
                            )
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = userSettings.copy(
                        tradingMode = currentMode,
                        mexcApiKey = apiKeyText.trim(),
                        mexcApiSecret = apiSecretText.trim(),
                        scannedCoinsCount = scannedCoinsCount.toInt(),
                        referenceTimeframe = referenceTf,
                        entryTimeframe = entryTf,
                        orderAmountUsdt = orderAmountText.toDoubleOrNull() ?: 100.0,
                        maxConcurrentTrades = maxTradesCount.toInt(),
                        riskRewardRatio = rrRatio.toDouble(),
                        autoTradeEnabled = autoTradeEnabled,
                        autoBreakevenAt1R = autoBreakeven
                    )
                    viewModel.saveSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حفظ وتطبيق كافة الإعدادات",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
