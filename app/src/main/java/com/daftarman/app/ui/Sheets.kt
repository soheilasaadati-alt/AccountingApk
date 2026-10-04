package com.daftarman.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daftarman.app.MainViewModel
import com.daftarman.app.data.Account
import com.daftarman.app.data.Category
import com.daftarman.app.data.Installment
import com.daftarman.app.data.TYPE_EXP
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.data.Txn
import com.daftarman.app.util.Jalali
import com.daftarman.app.util.faNum
import com.daftarman.app.util.fa
import com.daftarman.app.util.paidCount
import com.daftarman.app.util.reminderLabel
import com.daftarman.app.util.parseAmount
import com.daftarman.app.util.toFa

private fun digitsFa(s: String): String =
    if (s.none { it.isDigit() }) "" else parseAmount(s).toString().toFa()

@Composable
private fun FormError(msg: String?) {
    msg?.let { Text(it, color = Expense, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp)) }
}

@Composable
private fun FieldGap() = Spacer(Modifier.height(12.dp))

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, color: Color = Ink, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = if (color == Gold) Ink else Color.White
        )
    ) { Text(text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp)) }
}

@Composable
private fun DangerButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Expense)
    ) { Text(text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp)) }
}

@Composable
private fun CategoryChip(c: Category, selected: Boolean, width: Dp, enabled: Boolean, onClick: () -> Unit) {
    val color = Color(c.color)
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .width(width)
            .clip(shape)
            .background(if (selected) color.copy(alpha = 0.14f) else Color.White)
            .then(if (selected) Modifier.border(1.5.dp, color, shape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(c.icon, fontSize = 22.sp)
        Text(
            c.name,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/* ------------------------------ تراکنش ------------------------------ */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TxSheet(vm: MainViewModel, id: Long?, onDismiss: () -> Unit, onNeedAccount: () -> Unit) {
    val txns by vm.txns.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()

    val existing = remember(id) { txns.firstOrNull { it.id == id } }
    var type by remember { mutableStateOf(existing?.type ?: TYPE_EXP) }
    var catId by remember { mutableStateOf(existing?.categoryId) }
    var amount by remember { mutableStateOf(existing?.let { faNum(it.amount) } ?: "") }
    var accId by remember { mutableStateOf(existing?.accountId ?: vm.accountFilter ?: accounts.firstOrNull()?.id) }
    var day by remember { mutableStateOf(existing?.epochDay ?: Jalali.today()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var askDelete by remember { mutableStateOf(false) }
    val locked = existing?.installmentId != null

    fun changeType(t: String) {
        type = t
        if (cats.none { it.id == catId && it.type == t }) catId = null
    }

    AppSheet(if (existing != null) "ویرایش تراکنش" else "تراکنش جدید", onDismiss) {
        if (accounts.isEmpty()) {
            Text("برای ثبت تراکنش اول باید یک حساب بانکی بسازی.", color = Muted)
            Spacer(Modifier.height(14.dp))
            PrimaryButton("ساخت حساب", Modifier.fillMaxWidth()) { onNeedAccount() }
        } else {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDBE5E4))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SegButton("هزینه", type == TYPE_EXP, Expense, !locked) { changeType(TYPE_EXP) }
                SegButton("درآمد / واریزی", type == TYPE_INC, Income, !locked) { changeType(TYPE_INC) }
            }
            FieldGap()
            AmountField(amount, { amount = it })
            FieldGap()
            Text("دسته‌بندی", fontSize = 12.5.sp, color = Muted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            val list = cats.filter { it.type == type && (!it.isSystem || locked) }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val w = (maxWidth - 24.dp) / 4
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    list.forEach { c -> CategoryChip(c, c.id == catId, w, !locked) { catId = c.id } }
                }
            }
            FieldGap()
            DropdownField("حساب بانکی", accounts.map { it.id to it.name }, accId) { accId = it }
            FieldGap()
            JalaliDateField(day, { day = it })
            FieldGap()
            AppTextField(note, { note = it }, "توضیحات (اختیاری)")
            FormError(error)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) DangerButton("حذف") { askDelete = true }
                PrimaryButton("ذخیره", Modifier.weight(1f)) {
                    val a = parseAmount(amount)
                    val c = catId
                    val ac = accId
                    if (a == 0L) {
                        error = "مبلغ را وارد کن"
                    } else if (c == null) {
                        error = "یک دسته‌بندی انتخاب کن"
                    } else if (ac == null) {
                        error = "یک حساب انتخاب کن"
                    } else {
                        val t = existing?.copy(
                            type = type, amount = a, categoryId = c, accountId = ac,
                            epochDay = day, note = note.trim()
                        ) ?: Txn(
                            type = type, amount = a, categoryId = c, accountId = ac,
                            epochDay = day, note = note.trim()
                        )
                        vm.saveTxn(t)
                        onDismiss()
                    }
                }
            }
        }
    }
    if (askDelete && existing != null) {
        ConfirmDialog(
            text = if (locked) "این تراکنش حذف شود؟ قسط مربوطه دوباره «پرداخت‌نشده» حساب می‌شود." else "این تراکنش حذف شود؟",
            onConfirm = { vm.deleteTxn(existing); askDelete = false; onDismiss() },
            onDismiss = { askDelete = false }
        )
    }
}

/* ------------------------------ قسط ------------------------------ */

private val ReminderChoices = listOf(-1, 0, 1, 2, 3, 7)

@Composable
fun InstallmentSheet(vm: MainViewModel, id: Long?, onDismiss: () -> Unit, onRemindPermission: () -> Unit) {
    val items by vm.installments.collectAsStateWithLifecycle()
    val existing = remember(id) { items.firstOrNull { it.id == id } }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var amount by remember { mutableStateOf(existing?.let { faNum(it.amount) } ?: "") }
    var first by remember { mutableStateOf(existing?.firstDueEpochDay ?: Jalali.today()) }
    var count by remember { mutableStateOf(existing?.totalCount?.fa() ?: "") }
    var reminder by remember { mutableStateOf(existing?.reminderDays ?: 1) }
    var error by remember { mutableStateOf<String?>(null) }
    var askDelete by remember { mutableStateOf(false) }

    AppSheet(if (existing != null) "ویرایش قسط" else "قسط جدید", onDismiss) {
        AppTextField(title, { title = it }, "عنوان (مثلاً قسط بیمه ماشین)")
        FieldGap()
        AmountField(amount, { amount = it }, "مبلغ هر قسط (تومان)")
        FieldGap()
        JalaliDateField(first, { first = it }, "تاریخ سررسید اولین قسط")
        Text(
            "از ماه بعد هر ماه در همین روز تکرار می‌شود.",
            color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)
        )
        FieldGap()
        AppTextField(count, { count = digitsFa(it) }, "تعداد اقساط (اختیاری)", KeyboardType.Number)
        Text(
            "اگر خالی بگذاری، قسط تا وقتی حذفش نکنی هر ماه تکرار می‌شود.",
            color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)
        )
        FieldGap()
        DropdownField("یادآوری سررسید", ReminderChoices.map { it to reminderLabel(it) }, reminder) { reminder = it }
        FormError(error)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (existing != null) DangerButton("حذف") { askDelete = true }
            PrimaryButton("ذخیره", Modifier.weight(1f)) {
                val amt = parseAmount(amount)
                val cnt = parseAmount(count).toInt()
                if (title.isBlank()) {
                    error = "عنوان قسط را بنویس"
                } else if (amt == 0L) {
                    error = "مبلغ قسط را وارد کن"
                } else if (count.isNotBlank() && (cnt < 1 || cnt > 600)) {
                    error = "تعداد اقساط را درست وارد کن"
                } else {
                    val total = if (count.isBlank()) null else cnt
                    val item = existing?.copy(
                        title = title.trim(), amount = amt, firstDueEpochDay = first,
                        totalCount = total, reminderDays = reminder
                    ) ?: Installment(
                        title = title.trim(), amount = amt, firstDueEpochDay = first,
                        totalCount = total, reminderDays = reminder
                    )
                    vm.saveInstallment(item)
                    if (reminder >= 0) onRemindPermission()
                    onDismiss()
                }
            }
        }
    }
    if (askDelete && existing != null) {
        ConfirmDialog(
            text = "این قسط حذف شود؟ هزینه‌هایی که برای آن ثبت کرده‌ای حذف نمی‌شوند.",
            onConfirm = { vm.deleteInstallment(existing); askDelete = false; onDismiss() },
            onDismiss = { askDelete = false }
        )
    }
}

@Composable
fun PaySheet(vm: MainViewModel, installmentId: Long, onDismiss: () -> Unit, onNeedAccount: () -> Unit) {
    val items by vm.installments.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val item = items.firstOrNull { it.id == installmentId }
    if (item == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val paid = paidCount(item, txns)
    val total = item.totalCount
    var amount by remember { mutableStateOf(faNum(item.amount)) }
    var accId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var day by remember { mutableStateOf(Jalali.today()) }
    var error by remember { mutableStateOf<String?>(null) }

    val sheetTitle = "پرداخت قسط ${(paid + 1).fa()}" + if (total != null) " از ${total.fa()}" else ""
    AppSheet(sheetTitle, onDismiss) {
        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        FieldGap()
        if (accounts.isEmpty()) {
            Text("برای پرداخت قسط اول باید یک حساب بانکی بسازی.", color = Muted)
            Spacer(Modifier.height(14.dp))
            PrimaryButton("ساخت حساب", Modifier.fillMaxWidth()) { onNeedAccount() }
        } else {
            AmountField(amount, { amount = it })
            FieldGap()
            DropdownField("پرداخت از حساب", accounts.map { it.id to it.name }, accId) { accId = it }
            FieldGap()
            JalaliDateField(day, { day = it }, "تاریخ پرداخت")
            FormError(error)
            Spacer(Modifier.height(18.dp))
            PrimaryButton("ثبت پرداخت", Modifier.fillMaxWidth(), Gold) {
                val a = parseAmount(amount)
                val ac = accId
                if (a == 0L) {
                    error = "مبلغ را وارد کن"
                } else if (ac == null) {
                    error = "یک حساب انتخاب کن"
                } else {
                    vm.payInstallment(item, a, ac, day)
                    onDismiss()
                }
            }
        }
    }
}

/* ------------------------------ حساب بانکی ------------------------------ */

@Composable
fun AccountSheet(vm: MainViewModel, id: Long?, onDismiss: () -> Unit) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()
    val existing = remember(id) { accounts.firstOrNull { it.id == id } }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var bank by remember { mutableStateOf(existing?.bank ?: "") }
    var initial by remember { mutableStateOf(existing?.let { if (it.initialBalance > 0) faNum(it.initialBalance) else "" } ?: "") }
    var color by remember { mutableStateOf(existing?.color ?: Palette[10]) }
    var error by remember { mutableStateOf<String?>(null) }
    var askDelete by remember { mutableStateOf(false) }

    AppSheet(if (existing != null) "ویرایش حساب" else "حساب جدید", onDismiss) {
        AppTextField(name, { name = it }, "نام حساب (مثلاً حقوق ملت)")
        FieldGap()
        AppTextField(bank, { bank = it }, "بانک یا توضیح (اختیاری)")
        FieldGap()
        AmountField(initial, { initial = it }, "موجودی اولیه (تومان)")
        FieldGap()
        Text("رنگ کارت", fontSize = 12.5.sp, color = Muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        ColorPicker(color) { color = it }
        FormError(error)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (existing != null) {
                DangerButton("حذف") {
                    if (txns.any { it.accountId == existing.id }) {
                        error = "این حساب تراکنش دارد؛ اول تراکنش‌هایش را حذف کن"
                    } else {
                        askDelete = true
                    }
                }
            }
            PrimaryButton("ذخیره", Modifier.weight(1f)) {
                if (name.isBlank()) {
                    error = "نام حساب را بنویس"
                } else {
                    val a = existing?.copy(
                        name = name.trim(), bank = bank.trim(), initialBalance = parseAmount(initial), color = color
                    ) ?: Account(
                        name = name.trim(), bank = bank.trim(), initialBalance = parseAmount(initial), color = color
                    )
                    vm.saveAccount(a)
                    onDismiss()
                }
            }
        }
    }
    if (askDelete && existing != null) {
        ConfirmDialog(
            text = "این حساب حذف شود؟",
            onConfirm = { vm.deleteAccount(existing); askDelete = false; onDismiss() },
            onDismiss = { askDelete = false }
        )
    }
}

/* ------------------------------ دسته (گروه هزینه/درآمد) ------------------------------ */

@Composable
fun CategorySheet(vm: MainViewModel, id: Long?, type: String, onDismiss: () -> Unit) {
    val cats by vm.categories.collectAsStateWithLifecycle()
    val txns by vm.txns.collectAsStateWithLifecycle()
    val existing = remember(id) { cats.firstOrNull { it.id == id } }
    val ty = existing?.type ?: type
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "🧾") }
    var color by remember { mutableStateOf(existing?.color ?: Palette[0]) }
    var custom by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var askDelete by remember { mutableStateOf(false) }

    val title = when {
        existing != null -> "ویرایش دسته"
        ty == TYPE_EXP -> "گروه هزینه جدید"
        else -> "گروه درآمد جدید"
    }

    AppSheet(title, onDismiss) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconBadge(icon, Color(color), size = 64.dp, fontSize = 30)
        }
        FieldGap()
        AppTextField(name, { name = it }, if (ty == TYPE_EXP) "نام گروه (مثلاً هزینه بچه‌ها)" else "نام گروه (مثلاً اجاره دریافتی)")
        FieldGap()
        Text("آیکن", fontSize = 12.5.sp, color = Muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        EmojiPicker(icon) { icon = it; custom = "" }
        Spacer(Modifier.height(8.dp))
        AppTextField(custom, { v -> custom = v; if (v.isNotBlank()) icon = v.trim().take(4) }, "یا یک ایموجی دلخواه بنویس")
        FieldGap()
        Text("رنگ", fontSize = 12.5.sp, color = Muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        ColorPicker(color) { color = it }
        FormError(error)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (existing != null && !existing.isSystem) DangerButton("حذف") { askDelete = true }
            PrimaryButton("ذخیره", Modifier.weight(1f)) {
                if (name.isBlank()) {
                    error = "نام گروه را بنویس"
                } else {
                    val c = existing?.copy(name = name.trim(), icon = icon, color = color)
                        ?: Category(name = name.trim(), icon = icon, color = color, type = ty)
                    vm.saveCategory(c)
                    onDismiss()
                }
            }
        }
    }
    if (askDelete && existing != null) {
        val n = txns.count { it.categoryId == existing.id }
        ConfirmDialog(
            text = if (n > 0) "این گروه حذف شود؟ ${n.fa()} تراکنش بدون دسته می‌شوند." else "این گروه حذف شود؟",
            onConfirm = { vm.deleteCategory(existing); askDelete = false; onDismiss() },
            onDismiss = { askDelete = false }
        )
    }
}
