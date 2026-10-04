package com.daftarman.app.util

import java.time.DayOfWeek
import java.time.LocalDate

/** تبدیل تاریخ میلادی ⇄ شمسی (الگوریتم استاندارد jdf) و ابزارهای نمایش. */
object Jalali {
    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun today(): Long = LocalDate.now().toEpochDay()

    fun fromEpochDay(day: Long): Triple<Int, Int, Int> {
        val d = LocalDate.ofEpochDay(day)
        val j = gregorianToJalali(d.year, d.monthValue, d.dayOfMonth)
        return Triple(j[0], j[1], j[2])
    }

    fun toEpochDay(jy: Int, jm: Int, jd: Int): Long {
        val g = jalaliToGregorian(jy, jm, jd)
        return LocalDate.of(g[0], g[1], g[2]).toEpochDay()
    }

    fun isLeap(jy: Int): Boolean {
        val g = jalaliToGregorian(jy, 12, 30)
        val back = gregorianToJalali(g[0], g[1], g[2])
        return back[0] == jy && back[1] == 12 && back[2] == 30
    }

    fun monthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeap(jy)) 30 else 29
    }

    /** n ماه شمسی به تاریخ اضافه می‌کند (روز در صورت نیاز به آخر ماه محدود می‌شود). */
    fun addMonths(day: Long, n: Int): Long {
        val (y0, m0, d0) = fromEpochDay(day)
        val t = (m0 - 1) + n
        val y = y0 + Math.floorDiv(t, 12)
        val m = Math.floorMod(t, 12) + 1
        return toEpochDay(y, m, minOf(d0, monthLength(y, m)))
    }

    fun format(day: Long, short: Boolean = false): String {
        val (y, m, d) = fromEpochDay(day)
        return "${d.fa()} ${monthNames[m - 1]}" + if (short) "" else " ${y.fa()}"
    }

    fun weekdayName(day: Long): String = when (LocalDate.ofEpochDay(day).dayOfWeek) {
        DayOfWeek.SATURDAY -> "شنبه"
        DayOfWeek.SUNDAY -> "یکشنبه"
        DayOfWeek.MONDAY -> "دوشنبه"
        DayOfWeek.TUESDAY -> "سه‌شنبه"
        DayOfWeek.WEDNESDAY -> "چهارشنبه"
        DayOfWeek.THURSDAY -> "پنجشنبه"
        DayOfWeek.FRIDAY -> "جمعه"
    }

    private fun gregorianToJalali(gyIn: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy = gyIn
        var jy: Int
        if (gy > 1600) { jy = 979; gy -= 1600 } else { jy = 0; gy -= 621 }
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd + gdm[gm - 1]
        jy += 33 * (days / 12053); days %= 12053
        jy += 4 * (days / 1461); days %= 1461
        jy += (days - 1) / 365
        if (days > 365) days = (days - 1) % 365
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + (if (days < 186) days % 31 else (days - 186) % 30)
        return intArrayOf(jy, jm, jd)
    }

    private fun jalaliToGregorian(jyIn: Int, jm: Int, jd: Int): IntArray {
        var jy = jyIn
        var gy: Int
        if (jy > 979) { gy = 1600; jy -= 979 } else { gy = 621 }
        var days = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4 + 78 + jd +
            (if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186)
        gy += 400 * (days / 146097); days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524); days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461); days %= 1461
        gy += (days - 1) / 365
        if (days > 365) days = (days - 1) % 365
        var gd = days + 1
        val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val sa = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13) {
            val v = sa[gm]
            if (gd <= v) break
            gd -= v
            gm++
        }
        return intArrayOf(gy, gm, gd)
    }
}
