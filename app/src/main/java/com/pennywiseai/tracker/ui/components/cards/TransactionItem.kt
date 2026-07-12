package com.pennywiseai.tracker.ui.components.cards

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pennywiseai.tracker.data.contacts.LocalMerchantDisplay
import com.pennywiseai.tracker.data.database.entity.ProfileEntity
import com.pennywiseai.tracker.data.database.entity.TransactionEntity
import com.pennywiseai.tracker.data.database.entity.TransactionType
import com.pennywiseai.tracker.ui.LocalNavAnimatedVisibilityScope
import com.pennywiseai.tracker.ui.LocalSharedTransitionScope
import com.pennywiseai.tracker.ui.sharedElementIcon
import com.pennywiseai.tracker.ui.components.BrandIcon
import com.pennywiseai.tracker.ui.theme.*
import com.pennywiseai.tracker.utils.CurrencyFormatter
import com.pennywiseai.tracker.utils.DateFormatter
import com.pennywiseai.tracker.utils.formatAmount
import java.math.BigDecimal

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    convertedAmount: BigDecimal? = null,
    displayCurrency: String? = null,
    showTypeLabel: Boolean = true,
    listItemPosition: ListItemPosition = ListItemPosition.Single,
    profileAccountKeys: Map<Long, Set<String>> = emptyMap(),
    onClick: () -> Unit = {},
    /** Optional long-press handler — used for bulk-edit selection entry. */
    onLongClick: (() -> Unit)? = null,
    /** Overrides the row's container colour (e.g. for selected state). */
    containerColor: androidx.compose.ui.graphics.Color? = null,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()
    val amountColor = remember(transaction.transactionType, isDark) {
        when (transaction.transactionType) {
            TransactionType.INCOME -> if (!isDark) income_light else income_dark
            TransactionType.EXPENSE -> if (!isDark) expense_light else expense_dark
            TransactionType.CREDIT -> if (!isDark) credit_light else credit_dark
            TransactionType.TRANSFER -> if (!isDark) transfer_light else transfer_dark
            TransactionType.INVESTMENT -> if (!isDark) investment_light else investment_dark
        }
    }

    // Bottom-right of the card always shows date + time together (e.g.
    // "12 Jul · 8:00 PM") — always, regardless of which date-group section
    // it's in, so a card is legible on its own without scrolling up to a
    // sticky header for context.
    val dateTimeText = remember(transaction.dateTime, DateFormatter.useJalaliCalendar) {
        DateFormatter.formatDayMonthTime(transaction.dateTime)
    }

    val isEffectivelyBusiness = remember(transaction, profileAccountKeys) {
        val effectiveProfileId = transaction.profileId ?: run {
            if (transaction.bankName != null && transaction.accountNumber != null) {
                val key = "${transaction.bankName}_${transaction.accountNumber}"
                profileAccountKeys.entries.firstOrNull { (_, keys) -> keys.contains(key) }?.key
            } else null
        }
        effectiveProfileId == ProfileEntity.BUSINESS_ID
    }

    // User-written description, kept as its own row (truncated to one line)
    // rather than folded into a joined subtitle string — a longer note
    // shouldn't crowd out the category/time/bank info sitting next to it. (#383)
    val description = transaction.description?.takeIf { it.isNotBlank() }

    val categoryLabel = transaction.category.takeIf {
        it.isNotBlank() && !it.equals("Uncategorized", ignoreCase = true)
    }

    // "Bank ••1234" — which account/card the transaction posted against.
    val bankLabel = remember(transaction.bankName, transaction.accountNumber) {
        val bank = transaction.bankName?.takeIf { it.isNotBlank() }
        val last4 = transaction.accountNumber?.takeIf { it.isNotBlank() }
        when {
            bank != null && last4 != null -> "$bank ••$last4"
            bank != null -> bank
            else -> null
        }
    }

    // Secondary status badges — same set the old single-line subtitle carried,
    // appended after category/date on the left rather than crowding the
    // right-side amount/bank/time column.
    val statusBadges = remember(transaction, showTypeLabel, isEffectivelyBusiness) {
        buildList {
            if (showTypeLabel) {
                when (transaction.transactionType) {
                    TransactionType.CREDIT -> add("Credit")
                    TransactionType.TRANSFER -> {
                        if (transferTitleOverride(transaction) == null) add("Transfer")
                    }
                    TransactionType.INVESTMENT -> add("Investment")
                    else -> {}
                }
            }
            if (transaction.isRecurring) add("Recurring")
            if (isEffectivelyBusiness) add("Business")
            // Mark rows the user excluded from analytics so it's visible in the
            // list which ones are skipped by spending stats (#451).
            if (transaction.excludedFromAnalytics) add("Excluded")
        }
    }

    val categoryLine = remember(categoryLabel, statusBadges) {
        (listOfNotNull(categoryLabel) + statusBadges).joinToString(" · ")
    }

    val amountPrefix = remember(transaction.transactionType) {
        when (transaction.transactionType) {
            TransactionType.INCOME -> "+"
            TransactionType.EXPENSE, TransactionType.CREDIT, TransactionType.INVESTMENT -> "-"
            TransactionType.TRANSFER -> ""
        }
    }

    val formattedAmount = if (convertedAmount != null && displayCurrency != null) {
        CurrencyFormatter.formatCurrency(convertedAmount, displayCurrency)
    } else {
        transaction.formatAmount()
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val merchantDisplay = LocalMerchantDisplay.current

    // For a paired self-transfer row, the event ("Transfer → 9999" /
    // "Transfer from 1234") is more informative than the merchant name (often
    // the user's own contact name), and stops the two legs from looking like
    // duplicate rows in the list. Falls back to merchant otherwise.
    val transferTitle = transferTitleOverride(transaction)
    val title = transferTitle ?: merchantDisplay(transaction.merchantName) ?: transaction.merchantName

    PennyWiseCardV2(
        modifier = modifier.fillMaxWidth(),
        shape = listItemPosition.toShape(),
        contentPadding = 14.dp,
        containerColor = containerColor,
        border = androidx.compose.foundation.BorderStroke(0.dp, androidx.compose.ui.graphics.Color.Transparent),
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onClick()
        },
        onLongClick = onLongClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            run {
                val iconModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        sharedElementIcon(
                            key = "brand_icon_${transaction.id}",
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else {
                    Modifier
                }
                BrandIcon(
                    merchantName = transaction.merchantName,
                    modifier = iconModifier,
                    size = Dimensions.Icon.list,
                    showBackground = true,
                    category = transaction.category
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))

            // Left side: merchant, category (+ badges), and — the field allowed
            // to truncate — the user's own description (up to 3 lines, since
            // it's the one place worth spending the card's spare width/height on).
            // Category/badges get no maxLines/ellipsis: short enough in practice
            // that they should just wrap rather than silently lose information.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = categoryLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Right side: amount on top, bank/card in the middle, clock time at
            // the bottom — a fixed vertical order so it always reads the same way.
            Column(horizontalAlignment = Alignment.End) {
                if (convertedAmount != null && displayCurrency != null) {
                    Text(
                        text = "$amountPrefix${CurrencyFormatter.formatCurrency(convertedAmount, displayCurrency)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "(${transaction.formatAmount()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                } else {
                    Text(
                        text = "$amountPrefix$formattedAmount",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        textAlign = TextAlign.End
                    )
                }
                if (bankLabel != null) {
                    Text(
                        text = bankLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.End
                    )
                }
                // Slightly smaller than labelSmall — this now carries date + time
                // together ("12 Jul · 8:00 PM"), so it needs a touch more room to
                // stay on one line without crowding the bank label above it.
                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * For TRANSFER rows that have `fromAccount` and `toAccount` populated,
 * synthesise a title that describes the event (which leg + the other
 * account's last-4) rather than the merchant. Returns null for any other
 * row, in which case the default merchant-as-title rendering wins.
 */
private fun transferTitleOverride(transaction: TransactionEntity): String? {
    if (transaction.transactionType != TransactionType.TRANSFER) return null
    val mine = transaction.accountNumber
    val from = transaction.fromAccount
    val to = transaction.toAccount
    return when {
        from != null && to != null && mine == from -> "Transfer → ${to.takeLast(4)}"
        from != null && to != null && mine == to -> "Transfer from ${from.takeLast(4)}"
        to != null && mine != to -> "Transfer → ${to.takeLast(4)}"
        from != null && mine != from -> "Transfer from ${from.takeLast(4)}"
        else -> null
    }
}
