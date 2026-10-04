package com.daftarman.app.data

object DefaultData {
    val categories = listOf(
        Category(name = "خوراک", icon = "🛒", color = 0xFFE8833AL, type = TYPE_EXP),
        Category(name = "مسکن و اجاره", icon = "🏠", color = 0xFF5B8DEFL, type = TYPE_EXP),
        Category(name = "حمل‌ونقل", icon = "🚗", color = 0xFF7C6BD6L, type = TYPE_EXP),
        Category(name = "قبض‌ها", icon = "💡", color = 0xFFE0B431L, type = TYPE_EXP),
        Category(name = "سلامت", icon = "💊", color = 0xFFE5566DL, type = TYPE_EXP),
        Category(name = "تفریح", icon = "🎬", color = 0xFFC45BAAL, type = TYPE_EXP),
        Category(name = "آموزش", icon = "📚", color = 0xFF2BA7A0L, type = TYPE_EXP),
        Category(name = "پوشاک", icon = "👕", color = 0xFF8A9A5BL, type = TYPE_EXP),
        // دسته سیستمی: پرداخت اقساط به‌صورت خودکار در این دسته ثبت می‌شود
        Category(name = "اقساط", icon = "🔁", color = 0xFF12303AL, type = TYPE_EXP, isSystem = true),
        Category(name = "سایر", icon = "🧾", color = 0xFF8B9BA1L, type = TYPE_EXP),
        Category(name = "حقوق", icon = "💼", color = 0xFF1F9D78L, type = TYPE_INC),
        Category(name = "کار آزاد", icon = "💻", color = 0xFF3AA8D8L, type = TYPE_INC),
        Category(name = "هدیه", icon = "🎁", color = 0xFFD27AA6L, type = TYPE_INC),
        Category(name = "سایر درآمدها", icon = "💰", color = 0xFF8B9BA1L, type = TYPE_INC)
    )
}
