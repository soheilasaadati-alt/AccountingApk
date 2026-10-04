package com.daftarman.app.util

import java.util.Locale

fun String.toFa(): String {
    val sb = StringBuilder(length)
    for (c in this) sb.append(if (c in '0'..'9') '۰' + (c - '0') else c)
    return sb.toString()
}

fun Int.fa(): String = toString().toFa()

/** عدد نامنفی با جداکننده هزارگان و ارقام فارسی */
fun faNum(n: Long): String = String.format(Locale.US, "%,d", n).replace(',', '٬').toFa()

fun money(n: Long): String = if (n < 0) "\u2066−${faNum(-n)}\u2069" else faNum(n)

fun signedMoney(isIncome: Boolean, n: Long): String =
    "\u2066${if (isIncome) "+" else "−"}${faNum(n)}\u2069"

/** هر ورودی (ارقام فارسی، عربی یا لاتین) را به عدد تبدیل می‌کند */
fun parseAmount(s: String): Long {
    val sb = StringBuilder()
    for (c in s) {
        when (c) {
            in '0'..'9' -> sb.append(c)
            in '۰'..'۹' -> sb.append('0' + (c - '۰'))
            in '٠'..'٩' -> sb.append('0' + (c - '٠'))
        }
    }
    return sb.toString().take(15).toLongOrNull() ?: 0L
}

fun formatInput(s: String): String {
    val n = parseAmount(s)
    return if (n == 0L) "" else faNum(n)
}
