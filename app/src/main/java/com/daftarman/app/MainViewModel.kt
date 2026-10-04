package com.daftarman.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daftarman.app.data.*
import com.daftarman.app.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)

    val accounts: StateFlow<List<Account>> =
        db.accountDao().all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories: StateFlow<List<Category>> =
        db.categoryDao().all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val txns: StateFlow<List<Txn>> =
        db.txnDao().all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val installments: StateFlow<List<Installment>> =
        db.installmentDao().all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ماه انتخاب‌شده (شمسی) و فیلترهای صفحه تراکنش‌ها
    private val todayJ = Jalali.fromEpochDay(Jalali.today())
    var year by mutableIntStateOf(todayJ.first)
        private set
    var monthNo by mutableIntStateOf(todayJ.second)
        private set
    var typeFilter by mutableStateOf("ALL")
    var accountFilter by mutableStateOf<Long?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) { seedIfEmpty() }
    }

    private suspend fun seedIfEmpty() {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(DefaultData.categories)
            if (db.accountDao().count() == 0) {
                db.accountDao().insert(Account(name = "حساب اصلی", color = 0xFF12303AL))
            }
        }
    }

    fun prevMonth() {
        if (monthNo == 1) { monthNo = 12; year-- } else monthNo--
    }

    fun nextMonth() {
        if (monthNo == 12) { monthNo = 1; year++ } else monthNo++
    }

    fun monthTxns(all: List<Txn>): List<Txn> {
        val r = monthRange(year, monthNo)
        return all.filter { it.epochDay in r }
    }

    private fun io(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { block() }
    }

    // تراکنش‌ها
    fun saveTxn(t: Txn) = io { if (t.id == 0L) db.txnDao().insert(t) else db.txnDao().update(t) }
    fun deleteTxn(t: Txn) = io { db.txnDao().delete(t) }

    // حساب‌ها
    fun saveAccount(a: Account) = io { if (a.id == 0L) db.accountDao().insert(a) else db.accountDao().update(a) }
    fun deleteAccount(a: Account) = io { db.accountDao().delete(a) }

    // دسته‌ها
    fun saveCategory(c: Category) = io { if (c.id == 0L) db.categoryDao().insert(c) else db.categoryDao().update(c) }
    fun deleteCategory(c: Category) = io { db.categoryDao().delete(c) }

    // اقساط
    fun saveInstallment(i: Installment) =
        io { if (i.id == 0L) db.installmentDao().insert(i) else db.installmentDao().update(i) }

    fun deleteInstallment(i: Installment) = io { db.installmentDao().delete(i) }

    /** پرداخت یک قسط: یک هزینه در دسته «اقساط» ثبت می‌کند و به همان قسط وصل می‌شود. */
    fun payInstallment(item: Installment, amount: Long, accountId: Long, day: Long) = io {
        val catId = db.categoryDao().systemId() ?: db.categoryDao().insert(
            Category(name = "اقساط", icon = "🔁", color = 0xFF12303AL, type = TYPE_EXP, isSystem = true)
        )
        val n = paidCount(item, txns.value) + 1
        db.txnDao().insert(
            Txn(
                type = TYPE_EXP, amount = amount, categoryId = catId, accountId = accountId,
                epochDay = day, note = "قسط ${n.fa()} ${item.title}", installmentId = item.id
            )
        )
    }
}
