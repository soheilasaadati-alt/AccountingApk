package com.daftarman.app.util

import com.daftarman.app.data.Account
import com.daftarman.app.data.Installment
import com.daftarman.app.data.TYPE_INC
import com.daftarman.app.data.Txn

fun balanceOf(a: Account, txns: List<Txn>): Long =
    a.initialBalance + txns.filter { it.accountId == a.id }
        .sumOf { if (it.type == TYPE_INC) it.amount else -it.amount }

/** تعداد اقساط پرداخت‌شده = تراکنش‌هایی که برای این قسط ثبت شده */
fun paidCount(i: Installment, txns: List<Txn>): Int = txns.count { it.installmentId == i.id }

/** سررسید قسط بعدی؛ اگر همه اقساط پرداخت شده باشند null */
fun nextDue(i: Installment, txns: List<Txn>): Long? {
    val p = paidCount(i, txns)
    val total = i.totalCount
    return if (total == null || p < total) Jalali.addMonths(i.firstDueEpochDay, p) else null
}

fun reminderLabel(days: Int): String = when {
    days < 0 -> "بدون یادآوری"
    days == 0 -> "همان روز سررسید"
    else -> "${days.fa()} روز قبل از سررسید"
}

fun monthRange(y: Int, m: Int): LongRange =
    Jalali.toEpochDay(y, m, 1)..Jalali.toEpochDay(y, m, Jalali.monthLength(y, m))

/** level: 0 عادی، 1 نزدیک، 2 عقب‌افتاده */
data class DueInfo(val text: String, val level: Int)

fun dueInfo(day: Long): DueInfo {
    val n = day - Jalali.today()
    return when {
        n < 0 -> DueInfo("${(-n).toInt().fa()} روز تأخیر", 2)
        n == 0L -> DueInfo("امروز", 1)
        else -> DueInfo("${n.toInt().fa()} روز دیگر", if (n <= 7) 1 else 0)
    }
}
