package com.pennywiseai.tracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * A user-taught rule mapping a bank's raw, unmapped transaction-type label
 * (e.g. Tejarat's "نوع تراکنش:" value) to a [TransactionType]. Applied going
 * forward to every SMS from [bankName] whose label matches [rawTypeLabel],
 * so a parser that can't classify a message on its own (see
 * `BankParser.isPendingClassification` in parser-core) doesn't need a code
 * change and rebuild for every new label variant — the user teaches it once.
 */
@Entity(
    tableName = "transaction_type_rules",
    indices = [
        androidx.room.Index(
            value = ["bank_name", "raw_type_label"],
            unique = true
        )
    ]
)
@Serializable
data class TransactionTypeRuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "bank_name")
    val bankName: String = "",

    @ColumnInfo(name = "raw_type_label")
    val rawTypeLabel: String = "",

    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType = TransactionType.EXPENSE,

    @ColumnInfo(name = "created_at")
    @Contextual
    val createdAt: LocalDateTime = LocalDateTime.now()
)
