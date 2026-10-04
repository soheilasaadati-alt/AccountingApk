package com.daftarman.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daftarman.app.MainViewModel
import com.daftarman.app.data.Installment
import com.daftarman.app.data.Txn
import com.daftarman.app.util.Jalali
import com.daftarman.app.util.dueInfo
import com.daftarman.app.util.fa
import com.daftarman.app.util.money
import com.daftarman.app.util.monthRange
import com.daftarman.app.util.nextDue
import com.daftarman.app.util.paidCount
import com.daftarman.app.util.reminderLabel

@Composable
fun InstallmentsScreen(vm: MainViewModel, onOpen: (UiSheet) -> Unit) {
    val items by vm.installments.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()

    val today = Jalali.fromEpochDay(Jalali.today())
    val monthEnd = monthRange(today.first, today.second).last
    val active = items.filter { nextDue(it, txns) != null }
    val monthly = active.sumOf { it.amount }
    val dueThisMonth = active.filter { (nextDue(it, txns) ?: Long.MAX_VALUE) <= monthEnd }.sumOf { it.amount }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            ScreenHeader(
                "اقساط",
                subtitle = "پرداخت‌های ماهانه مثل بیمه، کارت اعتباری یا شهریه",
                action = { SmallButton("+ قسط جدید", { onOpen(UiSheet.Installment(null)) }) }
            )
        }
        if (items.isEmpty()) {
            item {
                EmptyNote(
                    "قسطی ثبت نشده.\nهر قسط ماهانه‌ات را فقط با مبلغ و تاریخ سررسید ثبت کن؛ " +
                        "قبل از سررسید برایت یادآوری می‌آید."
                )
            }
        } else {
            item {
                AppCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Paper)
                                .padding(12.dp)
                        ) {
                            Text("مجموع اقساط ماهانه", fontSize = 12.sp, color = Muted)
                            Text(money(monthly), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Paper)
                                .padding(12.dp)
                        ) {
                            Text("پرداخت‌نشده تا پایان ${Jalali.monthNames[today.second - 1]}", fontSize = 12.sp, color = Muted)
                            Text(money(dueThisMonth), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Expense)
                        }
                    }
                }
            }
            items.forEach { i ->
                item(key = i.id) {
                    InstallmentCard(
                        i, txns,
                        onEdit = { onOpen(UiSheet.Installment(i.id)) },
                        onPay = { onOpen(UiSheet.Pay(i.id)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstallmentCard(i: Installment, txns: List<Txn>, onEdit: () -> Unit, onPay: () -> Unit) {
    val paid = paidCount(i, txns)
    val total = i.totalCount
    val due = nextDue(i, txns)
    val dayOfMonth = Jalali.fromEpochDay(i.firstDueEpochDay).third

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge("🔁", Gold)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(i.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("هر ماه، روز ${dayOfMonth.fa()}", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = onEdit) { Text("ویرایش", color = Muted) }
        }

        // اگر تعداد اقساط مشخص باشد، کارت پانچ نمایش داده می‌شود (هر مربع یک قسط)
        if (total != null) {
            Spacer(Modifier.height(12.dp))
            if (total <= 48) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(total) { n ->
                        val shape = RoundedCornerShape(5.dp)
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(shape)
                                .background(if (n < paid) Gold else Track)
                                .then(if (n == paid && due != null) Modifier.border(2.dp, Ink, shape) else Modifier)
                        )
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Track)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(minOf(paid.toFloat() / total, 1f))
                            .fillMaxHeight()
                            .background(Gold)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Paper)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            MiniStat("مبلغ هر قسط", money(i.amount), Modifier.weight(1f))
            if (total != null) {
                MiniStat("پرداخت‌شده", "${minOf(paid, total).fa()} از ${total.fa()}", Modifier.weight(1f))
                MiniStat("باقی‌مانده", money(maxOf(total - paid, 0) * i.amount), Modifier.weight(1f))
            } else {
                MiniStat("پرداخت‌شده", "${paid.fa()} قسط", Modifier.weight(1f))
                MiniStat("نوع", "بدون پایان", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(12.dp))
        if (due == null) {
            Text("✓ همه اقساط پرداخت شد", color = Income, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        } else {
            val info = dueInfo(due)
            val infoColor = when (info.level) { 2 -> Expense; 1 -> Color(0xFFB7791F); else -> Ink }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("قسط بعدی", fontSize = 12.sp, color = Muted)
                    Text(
                        "${Jalali.format(due)} (${info.text})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = infoColor
                    )
                    Text(
                        if (i.reminderDays >= 0) "🔔 یادآوری: ${reminderLabel(i.reminderDays)}" else "بدون یادآوری",
                        fontSize = 11.5.sp,
                        color = Muted
                    )
                }
                SmallButton("پرداخت قسط", onPay)
            }
        }
    }
}
