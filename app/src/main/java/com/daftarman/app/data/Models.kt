package com.daftarman.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TYPE_EXP = "EXP"
const val TYPE_INC = "INC"

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bank: String = "",
    val initialBalance: Long = 0,
    val color: Long
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    val type: String,
    val isSystem: Boolean = false
)

/** یک تراکنش: هزینه (EXP) یا درآمد/واریزی (INC). تاریخ به‌صورت epochDay میلادی ذخیره می‌شود و نمایش شمسی است. */
@Entity(tableName = "transactions")
data class Txn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    val categoryId: Long,
    val accountId: Long,
    val epochDay: Long,
    val note: String = "",
    val installmentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * قسط تکراری ماهانه (بیمه، قسط کارت، شهریه و...). فقط مبلغ هر قسط و تاریخ سررسید مهم است.
 * totalCount خالی (null) یعنی قسط تا وقتی حذف نشود هر ماه تکرار می‌شود.
 * reminderDays: چند روز قبل از سررسید یادآوری بیاید (۰ = همان روز، منفی = بدون یادآوری).
 */
@Entity(tableName = "installments")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val firstDueEpochDay: Long,
    val totalCount: Int? = null,
    val reminderDays: Int = 1
)
