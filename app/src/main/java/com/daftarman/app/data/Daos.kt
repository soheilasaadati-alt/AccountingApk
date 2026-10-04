package com.daftarman.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id")
    fun all(): Flow<List<Account>>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id")
    fun all(): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT id FROM categories WHERE isSystem = 1 LIMIT 1")
    suspend fun systemId(): Long?

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}

@Dao
interface TxnDao {
    @Query("SELECT * FROM transactions ORDER BY epochDay DESC, createdAt DESC")
    fun all(): Flow<List<Txn>>

    @Insert
    suspend fun insert(txn: Txn): Long

    @Update
    suspend fun update(txn: Txn)

    @Delete
    suspend fun delete(txn: Txn)
}

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installments ORDER BY id")
    fun all(): Flow<List<Installment>>

    @Insert
    suspend fun insert(item: Installment): Long

    @Update
    suspend fun update(item: Installment)

    @Delete
    suspend fun delete(item: Installment)
}
