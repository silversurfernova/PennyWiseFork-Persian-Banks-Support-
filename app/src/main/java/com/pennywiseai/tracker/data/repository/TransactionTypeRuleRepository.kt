package com.pennywiseai.tracker.data.repository

import com.pennywiseai.tracker.data.database.dao.TransactionTypeRuleDao
import com.pennywiseai.tracker.data.database.entity.TransactionType
import com.pennywiseai.tracker.data.database.entity.TransactionTypeRuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user-taught transaction-type classification rules (see
 * [TransactionTypeRuleEntity]).
 */
@Singleton
class TransactionTypeRuleRepository @Inject constructor(
    private val dao: TransactionTypeRuleDao
) {
    fun getAll(): Flow<List<TransactionTypeRuleEntity>> = dao.getAll()

    suspend fun teach(bankName: String, rawTypeLabel: String, type: TransactionType) {
        dao.upsert(
            TransactionTypeRuleEntity(
                bankName = bankName,
                rawTypeLabel = rawTypeLabel,
                transactionType = type
            )
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)

    /**
     * A (bankName, rawTypeLabel) -> type lookup, loaded once per SMS scan —
     * mirrors the merchant-mapping cache pattern in OptimizedSmsReaderWorker.
     */
    suspend fun loadCache(): Map<Pair<String, String>, TransactionType> =
        dao.getAllOnce().associate { (it.bankName to it.rawTypeLabel) to it.transactionType }
}
