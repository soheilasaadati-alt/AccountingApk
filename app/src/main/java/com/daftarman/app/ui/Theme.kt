package com.daftarman.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val Ink = Color(0xFF12303A)
val Paper = Color(0xFFEEF3F2)
val Line = Color(0xFFE1E8E7)
val Muted = Color(0xFF6B7F86)
val Income = Color(0xFF1F9D78)
val Expense = Color(0xFFD9455F)
val Gold = Color(0xFFE3A92E)
val Track = Color(0xFFE3EAE9)

val Palette = listOf(
    0xFFE8833AL, 0xFF5B8DEFL, 0xFF7C6BD6L, 0xFFE0B431L, 0xFFE5566DL, 0xFFC45BAAL,
    0xFF2BA7A0L, 0xFF8A9A5BL, 0xFF1F9D78L, 0xFF3AA8D8L, 0xFF12303AL, 0xFF8B9BA1L
)

val EmojiSet = listOf(
    "🛒", "🍽️", "☕", "🏠", "🚗", "⛽", "💡", "📱", "🌐", "💊", "🩺", "🎬", "🎮", "📚", "🎓",
    "👕", "👶", "🐾", "✈️", "🎁", "💼", "💻", "💰", "🏦", "🧾", "🔧", "🧴", "🏋️", "🎂", "🚕"
)

/**
 * برای استفاده از فونت وزیرمتن: فایل‌های Vazirmatn-Regular.ttf و Vazirmatn-Bold.ttf را
 * با نام‌های vazirmatn_regular و vazirmatn_bold در app/src/main/res/font بگذار و به‌جای
 * FontFamily.Default این را بنویس:
 *   FontFamily(Font(R.font.vazirmatn_regular), Font(R.font.vazirmatn_bold, FontWeight.Bold))
 */
val AppFont: FontFamily = FontFamily.Default

private fun Typography.withFont(f: FontFamily) = copy(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f)
)

private val scheme = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE4ECEB),
    onSurfaceVariant = Muted,
    outline = Line,
    error = Expense
)

@Composable
fun DaftarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography().withFont(AppFont),
        content = content
    )
}
