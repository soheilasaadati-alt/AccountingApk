package com.daftarman.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftarman.app.MainViewModel
import com.daftarman.app.data.Account
import com.daftarman.app.data.Category
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.data.Txn
import com.daftarman.app.util.Jalali
import com.daftarman.app.util.fa
import com.daftarman.app.util.formatInput
import com.daftarman.app.util.signedMoney
import com.daftarman.app.util.toFa

/** خطوط افقی ظریف شبیه دفتر حساب، روی پس‌زمینه‌های تیره */
fun Modifier.ledgerLines(spacing: Dp = 28.dp, alpha: Float = 0.06f): Modifier = this.drawBehind {
    val step = spacing.toPx()
    var y = step
    while (y < size.height) {
        drawLine(Color.White.copy(alpha = alpha), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            if (subtitle != null) Text(subtitle, fontSize = 12.5.sp, color = Muted)
        }
        action?.invoke()
    }
}

@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    container: Color = Ink,
    content: Color = Color.White
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        shape = RoundedCornerShape(13.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) { Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun IconBadge(icon: String, color: Color, size: Dp = 42.dp, fontSize: Int = 20) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) { Text(icon, fontSize = fontSize.sp) }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Ink) {
    Column(modifier) {
        Text(label, fontSize = 11.5.sp, color = Muted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun EmptyNote(text: String) {
    Text(
        text,
        color = Muted,
        fontSize = 13.5.sp,
        lineHeight = 24.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 22.dp, horizontal = 12.dp)
    )
}

@Composable
fun MonthBar(vm: MainViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Line, CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { vm.prevMonth() }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه قبل")
        }
        Text(
            "${Jalali.monthNames[vm.monthNo - 1]} ${vm.year.fa()}",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { vm.nextMonth() }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه بعد")
        }
    }
}

@Composable
fun AppChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) Ink else Color.White)
            .border(1.5.dp, if (selected) Ink else Line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.White else Ink,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RowScope.SegButton(text: String, selected: Boolean, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) color else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else Muted,
            fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp
        )
    }
}

@Composable
fun PickerButton(text: String, options: List<Pair<Int, String>>, onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Ink)
        ) { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (k, l) ->
                DropdownMenuItem(text = { Text(l) }, onClick = { onPick(k); open = false })
            }
        }
    }
}

/** انتخاب تاریخ شمسی با سه لیست کشویی (روز / ماه / سال) */
@Composable
fun JalaliDateField(epochDay: Long, onChange: (Long) -> Unit, label: String = "تاریخ") {
    val (y, m, d) = Jalali.fromEpochDay(epochDay)
    val thisYear = Jalali.fromEpochDay(Jalali.today()).first
    Column {
        Text(label, fontSize = 12.5.sp, color = Muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerButton(
                text = d.fa(),
                options = (1..31).map { it to it.fa() },
                onPick = { nd -> onChange(Jalali.toEpochDay(y, m, minOf(nd, Jalali.monthLength(y, m)))) },
                modifier = Modifier.weight(1f)
            )
            PickerButton(
                text = Jalali.monthNames[m - 1],
                options = (1..12).map { it to Jalali.monthNames[it - 1] },
                onPick = { nm -> onChange(Jalali.toEpochDay(y, nm, minOf(d, Jalali.monthLength(y, nm)))) },
                modifier = Modifier.weight(1.7f)
            )
            PickerButton(
                text = y.fa(),
                options = (minOf(thisYear - 5, y)..maxOf(thisYear + 15, y)).map { it to it.fa() },
                onPick = { ny -> onChange(Jalali.toEpochDay(ny, m, minOf(d, Jalali.monthLength(ny, m)))) },
                modifier = Modifier.weight(1.3f)
            )
        }
    }
}

@Composable
fun <T> DropdownField(label: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column {
        Text(label, fontSize = 12.5.sp, color = Muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedButton(
                onClick = { open = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Ink)
            ) {
                Text(
                    options.firstOrNull { it.first == selected }?.second ?: "انتخاب کن",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { (k, l) ->
                    DropdownMenuItem(text = { Text(l) }, onClick = { onSelect(k); open = false })
                }
            }
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )
}

/** فیلد مبلغ با جداکننده هزارگان و ارقام فارسی */
@Composable
fun AmountField(value: String, onChange: (String) -> Unit, label: String = "مبلغ (تومان)") {
    AppTextField(
        value = value,
        onChange = { onChange(formatInput(it)) },
        label = label,
        keyboardType = KeyboardType.Number
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(selected: Long, onPick: (Long) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Palette.forEach { c ->
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(c))
                    .then(if (c == selected) Modifier.border(3.dp, Ink, CircleShape) else Modifier)
                    .clickable { onPick(c) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmojiPicker(selected: String, onPick: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        EmojiSet.forEach { e ->
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier
                    .size(46.dp)
                    .clip(shape)
                    .background(Color.White)
                    .then(if (e == selected) Modifier.border(2.dp, Ink, shape) else Modifier)
                    .clickable { onPick(e) },
                contentAlignment = Alignment.Center
            ) { Text(e, fontSize = 21.sp) }
        }
    }
}

@Composable
fun ConfirmDialog(text: String, confirmLabel: String = "حذف", onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = Expense, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = Muted) } },
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = Paper) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun Donut(slices: List<Pair<Float, Color>>, modifier: Modifier = Modifier, center: @Composable () -> Unit) {
    Box(modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 26.dp.toPx()
            var start = -90f
            slices.forEach { (frac, color) ->
                val sweep = frac * 360f
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = maxOf(sweep - 2f, 0.5f),
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke)
                )
                start += sweep
            }
        }
        center()
    }
}

@Composable
fun TxRow(t: Txn, cat: Category?, acc: Account?, onClick: () -> Unit) {
    val color = Color(cat?.color ?: 0xFF9AA9AEL)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(cat?.icon ?: "❔", color)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(cat?.name ?: "بدون دسته", fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            Text(
                t.note.ifBlank { acc?.name ?: "" },
                color = Muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            val inc = t.type == TYPE_INC
            Text(
                signedMoney(inc, t.amount),
                color = if (inc) Income else Expense,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.5.sp
            )
            if (t.note.isNotBlank() && acc != null) Text(acc.name, color = Muted, fontSize = 11.5.sp)
        }
    }
}
