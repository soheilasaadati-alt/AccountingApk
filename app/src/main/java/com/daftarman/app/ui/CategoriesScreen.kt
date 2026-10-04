package com.daftarman.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daftarman.app.MainViewModel
import com.daftarman.app.data.TYPE_EXP
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.util.fa

@Composable
fun CategoriesScreen(vm: MainViewModel, onOpen: (UiSheet) -> Unit) {
    val cats by vm.categories.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
        item { ScreenHeader("دسته‌ها", "هر گروه هزینه یا درآمدی که می‌خواهی بساز") }
        listOf(TYPE_EXP to "گروه‌های هزینه", TYPE_INC to "گروه‌های درآمد").forEach { (type, title) ->
            item(key = "t$type") {
                Row(
                    Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, Modifier.weight(1f), color = Muted, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    SmallButton("+ گروه جدید", { onOpen(UiSheet.Category(null, type)) }, container = Color(0xFFE4ECEB), content = Ink)
                }
            }
            item(key = "l$type") {
                AppCard {
                    cats.filter { it.type == type }.forEach { c ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(UiSheet.Category(c.id, c.type)) }
                                .padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconBadge(c.icon, Color(c.color))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.name, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                                Text(
                                    if (c.isSystem) "دسته سیستمی (اقساط)"
                                    else "${txns.count { it.categoryId == c.id }.fa()} تراکنش",
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Muted)
                        }
                    }
                }
            }
        }
        item {
            AppCard {
                Text("پشتیبان‌گیری", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(
                    "اطلاعات در دیتابیس خود برنامه روی همین گوشی ذخیره می‌شود. برای محافظت در برابر گم شدن گوشی، " +
                        "پشتیبان‌گیری گوگل (Settings > Google > Backup) را روشن نگه دار.",
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        item {
            AppCard {
                Text("درباره برنامه", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(
                    "دفتر من — حسابداری شخصی",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    "طراحی و توسعه: سهیلا سعادتی",
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
