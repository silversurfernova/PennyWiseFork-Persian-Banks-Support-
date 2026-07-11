package com.pennywiseai.tracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pennywiseai.tracker.data.database.entity.TransactionTypeRuleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user-taught transaction-type classification rules (see
 * [TransactionTypeRuleEntity]).
 */
@Dao
interface TransactionTypeRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: TransactionTypeRuleEntity): Long

    @Query("SELECT * FROM transaction_type_rules ORDER BY created_at DESC")
    fun getAll(): Flow<List<TransactionTypeRuleEntity>>

    /** Loaded once per SMS scan into an in-memory cache — see OptimizedSmsReaderWorker. */
    @Query("SELECT * FROM transaction_type_rules")
    suspend fun getAllOnce(): List<TransactionTypeRuleEntity>

    @Query("SELECT * FROM transaction_type_rules WHERE bank_name = :bankName AND raw_type_label = :rawTypeLabel LIMIT 1")
    suspend fun find(bankName: String, rawTypeLabel: String): TransactionTypeRuleEntity?

    @Query("DELETE FROM transaction_type_rules WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM transaction_type_rules")
    suspend fun deleteAll()
}
