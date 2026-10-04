package com.daftarman.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
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
import com.daftarman.app.data.TYPE_EXP
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.util.balanceOf
import com.daftarman.app.util.money

@Composable
fun AccountsScreen(vm: MainViewModel, onOpen: (UiSheet) -> Unit, onTab: (AppTab) -> Unit) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()
    val total = accounts.sumOf { balanceOf(it, txns) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            ScreenHeader(
                "حساب‌های بانکی",
                subtitle = "مجموع موجودی: ${money(total)} تومان",
                action = { SmallButton("+ حساب جدید", { onOpen(UiSheet.Account(null)) }) }
            )
        }
        if (accounts.isEmpty()) {
            item { EmptyNote("هنوز حسابی تعریف نکرده‌ای.") }
        }
        accounts.forEach { a ->
            item(key = a.id) {
                val ins = txns.filter { it.accountId == a.id && it.type == TYPE_INC }.sumOf { it.amount }
                val outs = txns.filter { it.accountId == a.id && it.type == TYPE_EXP }.sumOf { it.amount }
                Column(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(a.color))
                        .ledgerLines(26.dp, 0.07f)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(a.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (a.bank.isNotBlank()) {
                                Text(a.bank, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                            }
                        }
                        GlassButton("ویرایش") { onOpen(UiSheet.Account(a.id)) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(money(balanceOf(a, txns)), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(6.dp))
                        Text("تومان", color = Color.White.copy(alpha = 0.8f), fontSize = 12.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("واریزی‌ها: ${money(ins)}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.5.sp)
                            Text("برداشت‌ها: ${money(outs)}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.5.sp)
                        }
                        GlassButton("تراکنش‌ها") {
                            vm.accountFilter = a.id
                            vm.typeFilter = "ALL"
                            onTab(AppTab.Transactions)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassButton(text: String, onClick: () -> Unit) {
    SmallButton(text, onClick, container = Color.White.copy(alpha = 0.2f), content = Color.White)
}
