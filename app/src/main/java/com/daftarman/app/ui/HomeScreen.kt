package com.daftarman.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daftarman.app.MainViewModel
import com.daftarman.app.data.TYPE_EXP
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.util.Jalali
import com.daftarman.app.util.balanceOf
import com.daftarman.app.util.dueInfo
import com.daftarman.app.util.fa
import com.daftarman.app.util.money
import com.daftarman.app.util.nextDue

@Composable
fun HomeScreen(vm: MainViewModel, onOpen: (UiSheet) -> Unit, onTab: (AppTab) -> Unit) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()

    val monthTx = vm.monthTxns(txns)
    val income = monthTx.filter { it.type == TYPE_INC }.sumOf { it.amount }
    val expense = monthTx.filter { it.type == TYPE_EXP }.sumOf { it.amount }
    val total = accounts.sumOf { balanceOf(it, txns) }
    val byCat = monthTx.filter { it.type == TYPE_EXP }
        .groupBy { it.categoryId }
        .map { (id, list) -> id to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
    val upcoming = installments
        .mapNotNull { i -> nextDue(i, txns)?.let { i to it } }
        .sortedBy { it.second }
        .take(3)
    val monthName = Jalali.monthNames[vm.monthNo - 1]
    val today = Jalali.today()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            ScreenHeader("دفتر من", "امروز ${Jalali.weekdayName(today)}، ${Jalali.format(today)}")
        }
        item {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Ink)
                    .ledgerLines(30.dp, 0.055f)
                    .padding(22.dp)
            ) {
                Text("موجودی کل حساب‌ها", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(money(total), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "تومان",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.18f))
                )
                Row(Modifier.padding(top = 12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("درآمد $monthName", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                        Text(money(income), color = Color(0xFF6EE0B8), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("هزینه $monthName", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                        Text(money(expense), color = Color(0xFFFF8FA1), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            }
        }
        item { MonthBar(vm) }
        item {
            AppCard {
                Text("هزینه‌ها به تفکیک دسته", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                if (byCat.isEmpty()) {
                    EmptyNote("در $monthName هنوز هزینه‌ای ثبت نشده.\nبا دکمه + اولین هزینه را اضافه کن.")
                } else {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Donut(
                            slices = byCat.map { (id, v) ->
                                val c = cats.firstOrNull { it.id == id }
                                (v.toFloat() / expense.toFloat()) to Color(c?.color ?: 0xFF9AA9AEL)
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("هزینه ماه", fontSize = 12.sp, color = Muted)
                                Text(money(expense), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                Text("تومان", fontSize = 12.sp, color = Muted)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    byCat.forEach { (id, v) ->
                        val c = cats.firstOrNull { it.id == id }
                        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(c?.color ?: 0xFF9AA9AEL))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("${c?.icon ?: "❔"} ${c?.name ?: "بدون دسته"}", Modifier.weight(1f), fontSize = 13.5.sp)
                            Text(money(v), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text(
                                "${(v * 100 / expense).toInt().fa()}٪",
                                color = Muted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(44.dp)
                            )
                        }
                    }
                }
            }
        }
        if (upcoming.isNotEmpty()) {
            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("اقساط پیش‌رو", Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        TextButton(onClick = { onTab(AppTab.Installments) }) { Text("همه اقساط", color = Muted, fontSize = 12.5.sp) }
                    }
                    upcoming.forEach { (inst, day) ->
                        val info = dueInfo(day)
                        val infoColor = when (info.level) { 2 -> Expense; 1 -> Color(0xFFB7791F); else -> Muted }
                        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconBadge("🔁", Gold)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(inst.title, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                                Text("${info.text} — ${Jalali.format(day, short = true)}", color = infoColor, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(money(inst.amount), fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
                                SmallButton("پرداخت", onClick = { onOpen(UiSheet.Pay(inst.id)) }, container = Gold, content = Ink)
                            }
                        }
                    }
                }
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("آخرین تراکنش‌ها", Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    TextButton(onClick = { onTab(AppTab.Transactions) }) { Text("همه", color = Muted, fontSize = 12.5.sp) }
                }
                if (txns.isEmpty()) {
                    EmptyNote("هنوز تراکنشی ثبت نشده.")
                } else {
                    txns.take(5).forEach { t ->
                        TxRow(
                            t,
                            cats.firstOrNull { it.id == t.categoryId },
                            accounts.firstOrNull { it.id == t.accountId }
                        ) { onOpen(UiSheet.Tx(t.id)) }
                    }
                }
            }
        }
    }
}
