package com.daftarman.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daftarman.app.MainViewModel
import com.daftarman.app.data.TYPE_EXP
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.util.Jalali
import com.daftarman.app.util.money

@Composable
fun TransactionsScreen(vm: MainViewModel, onOpen: (UiSheet) -> Unit) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()

    // اگر حساب فیلتر‌شده حذف شده باشد، فیلتر برداشته می‌شود
    val accFilter = vm.accountFilter?.takeIf { id -> accounts.any { it.id == id } }
    val filtered = vm.monthTxns(txns)
        .filter { vm.typeFilter == "ALL" || it.type == vm.typeFilter }
        .filter { accFilter == null || it.accountId == accFilter }
    val income = filtered.filter { it.type == TYPE_INC }.sumOf { it.amount }
    val expense = filtered.filter { it.type == TYPE_EXP }.sumOf { it.amount }
    val groups = filtered.groupBy { it.epochDay }.toSortedMap(compareByDescending<Long> { it })

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
        item { ScreenHeader("تراکنش‌ها") }
        item { MonthBar(vm) }
        item {
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppChip("همه", vm.typeFilter == "ALL") { vm.typeFilter = "ALL" }
                AppChip("هزینه‌ها", vm.typeFilter == TYPE_EXP) { vm.typeFilter = TYPE_EXP }
                AppChip("درآمد و واریزی", vm.typeFilter == TYPE_INC) { vm.typeFilter = TYPE_INC }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                DropdownField(
                    label = "حساب",
                    options = listOf<Pair<Long?, String>>(null to "همه حساب‌ها") + accounts.map { it.id to it.name },
                    selected = accFilter,
                    onSelect = { vm.accountFilter = it }
                )
            }
        }
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
                        Text("واریزی و درآمد", fontSize = 12.sp, color = Muted)
                        Text(money(income), fontWeight = FontWeight.ExtraBold, color = Income, fontSize = 16.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Paper)
                            .padding(12.dp)
                    ) {
                        Text("هزینه", fontSize = 12.sp, color = Muted)
                        Text(money(expense), fontWeight = FontWeight.ExtraBold, color = Expense, fontSize = 16.sp)
                    }
                }
            }
        }
        if (groups.isEmpty()) {
            item { EmptyNote("تراکنشی برای این فیلتر پیدا نشد.") }
        } else {
            groups.forEach { (day, list) ->
                item(key = "h$day") {
                    Text(
                        "${Jalali.weekdayName(day)}، ${Jalali.format(day, short = true)}",
                        color = Muted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 2.dp)
                    )
                }
                item(key = "c$day") {
                    AppCard {
                        list.forEach { t ->
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
}
