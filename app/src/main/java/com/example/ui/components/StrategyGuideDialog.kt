package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BearRed
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StrategyGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandGold.copy(alpha = 0.5f)))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(BrandGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = "📊", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "استراتيجية مصيدة السيولة",
                            color = BrandGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Liquidity Trap Trading Strategy (MEXC)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkCardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Stats overview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "نطاق المرجع", color = TextMuted, fontSize = 11.sp)
                        Text(text = "4 ساعات", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "فريم الدخول", color = TextMuted, fontSize = 11.sp)
                        Text(text = "15 دقيقة", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "العائد / المخاطرة", color = TextMuted, fontSize = 11.sp)
                        Text(text = "2 : 1", color = BullGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 1
                StepItem(
                    number = "1",
                    title = "تحديد النطاق (صندوق 4س)",
                    description = "يتم تحديد أعلى قمة (HIGH) وأدنى قاع (LOW) لأول شمعة 4 ساعات في اليوم. هذا النطاق يمثل صندوق التوازن الأولي للسوق ومناطق تجمع أوامر السيولة ووقف الخسائر."
                )

                // Step 2
                StepItem(
                    number = "2",
                    title = "رصد الكسر أو الاختراق (15 دقيقة)",
                    description = "ننتقل لفريم 15د لمراقبة السلوك. عند اختراق أعلى الصندوق أو كسر أدناه بشمعة كاملة تغلق بجسمها خارج النطاق، يدخل متداولو الاختراق وتتفعل أوامر الوقف."
                )

                // Step 3
                StepItem(
                    number = "3",
                    title = "تأكيد مصيدة السيولة والدخول الفوري",
                    description = "إذا عجز السعر عن الاستمرار وعادت شمعة 15د للإغلاق مجدداً داخل الصندوق، فهذا تأكيد لامتصاص السيولة وفشل الكسر:\n• عودة من الأعلى -> إشارة بيع فورية 🔴\n• عودة من الأسفل -> إشارة شراء فورية 🟢"
                )

                // Step 4
                StepItem(
                    number = "4",
                    title = "إدارة الصفقة ووقف الخسارة (2R)",
                    description = "• وقف الخسارة: أعلى قمة أو أدنى قاع تم تسجيله أثناء الكسر الكاذب.\n• الهدف (Take Profit): ضعف وقف الخسارة (2R).\n• تأمين الدخول: حجز 50% أرباح ونقل الوقف لنقطة الدخول (Breakeven) عند تحقيق 1R."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Text(text = "فهمت الاستراتيجية", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    number: String,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(BrandCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = "الخطوة $number", color = BrandCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
    }
}
