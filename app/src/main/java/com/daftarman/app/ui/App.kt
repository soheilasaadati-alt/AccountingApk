package com.daftarman.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftarman.app.MainViewModel

enum class AppTab(val label: String, val icon: ImageVector) {
    Home("خانه", Icons.Filled.Home),
    Transactions("تراکنش‌ها", Icons.Filled.Receipt),
    Installments("اقساط", Icons.Filled.CalendarMonth),
    Accounts("حساب‌ها", Icons.Filled.AccountBalanceWallet),
    Categories("دسته‌ها", Icons.Filled.Category)
}

/** فرم‌هایی که به‌صورت پایین‌برگه (bottom sheet) باز می‌شوند */
sealed interface UiSheet {
    data class Tx(val id: Long?) : UiSheet
    data class Installment(val id: Long?) : UiSheet
    data class Pay(val installmentId: Long) : UiSheet
    data class Account(val id: Long?) : UiSheet
    data class Category(val id: Long?, val type: String) : UiSheet
}

@Composable
fun App(vm: MainViewModel) {
    var tab by rememberSaveable { mutableStateOf(AppTab.Home) }
    var sheet by remember { mutableStateOf<UiSheet?>(null) }

    // مجوز اعلان (اندروید ۱۳ به بالا) هنگام ذخیره قسطی که یادآور دارد پرسیده می‌شود
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val askNotifications: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val open: (UiSheet) -> Unit = { sheet = it }
    val goTab: (AppTab) -> Unit = { tab = it }

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                AppTab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink,
                            selectedTextColor = Ink,
                            indicatorColor = Color(0xFFD9EAE6),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == AppTab.Home || tab == AppTab.Transactions) {
                FloatingActionButton(
                    onClick = { sheet = UiSheet.Tx(null) },
                    containerColor = Gold,
                    contentColor = Ink,
                    shape = RoundedCornerShape(20.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "تراکنش جدید") }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                AppTab.Home -> HomeScreen(vm, open, goTab)
                AppTab.Transactions -> TransactionsScreen(vm, open)
                AppTab.Installments -> InstallmentsScreen(vm, open)
                AppTab.Accounts -> AccountsScreen(vm, open, goTab)
                AppTab.Categories -> CategoriesScreen(vm, open)
            }
        }
    }

    val close: () -> Unit = { sheet = null }
    val needAccount: () -> Unit = { sheet = null; tab = AppTab.Accounts }

    when (val s = sheet) {
        is UiSheet.Tx -> TxSheet(vm, s.id, close, needAccount)
        is UiSheet.Installment -> InstallmentSheet(vm, s.id, close, askNotifications)
        is UiSheet.Pay -> PaySheet(vm, s.installmentId, close, needAccount)
        is UiSheet.Account -> AccountSheet(vm, s.id, close)
        is UiSheet.Category -> CategorySheet(vm, s.id, s.type, close)
        null -> Unit
    }
}
