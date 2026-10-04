package com.daftarman.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daftarman.app.ui.App
import com.daftarman.app.reminder.Reminders
import com.daftarman.app.ui.DaftarTheme

/** دفتر من — طراحی و توسعه: سهیلا سعادتی */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Reminders.schedule(applicationContext)
        setContent {
            DaftarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val vm: MainViewModel = viewModel()
                    App(vm)
                }
            }
        }
    }
}
