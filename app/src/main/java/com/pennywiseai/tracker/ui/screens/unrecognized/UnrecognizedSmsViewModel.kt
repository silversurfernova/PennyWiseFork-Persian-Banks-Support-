package com.pennywiseai.tracker.ui.screens.unrecognized

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pennywiseai.parser.core.bank.BankParserFactory
import com.pennywiseai.tracker.core.Constants
import com.pennywiseai.tracker.core.Constants.Links
import com.pennywiseai.tracker.data.database.entity.TransactionType
import com.pennywiseai.tracker.data.database.entity.UnrecognizedSmsEntity
import com.pennywiseai.tracker.data.manager.SmsTransactionProcessor
import com.pennywiseai.tracker.data.mapper.toParserCoreType
import com.pennywiseai.tracker.data.repository.TransactionTypeRuleRepository
import com.pennywiseai.tracker.data.repository.UnrecognizedSmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import com.pennywiseai.tracker.utils.SmsReportUrlBuilder
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class UnrecognizedSmsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unrecognizedSmsRepository: UnrecognizedSmsRepository,
    private val transactionTypeRuleRepository: TransactionTypeRuleRepository,
    private val smsTransactionProcessor: SmsTransactionProcessor
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _showReported = MutableStateFlow(true)
    val showReported: StateFlow<Boolean> = _showReported.asStateFlow()
    
    private val allMessages = unrecognizedSmsRepository.getAllVisible()
    
    val unrecognizedMessages: StateFlow<List<UnrecognizedSmsEntity>> = 
        combine(allMessages, _showReported) { messages, showReported ->
            if (showReported) {
                messages
            } else {
                messages.filter { !it.reported }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun toggleShowReported() {
        _showReported.value = !_showReported.value
    }
    
    fun reportMessage(message: UnrecognizedSmsEntity) {
        viewModelScope.launch {
            try {
                val url = SmsReportUrlBuilder.buildUrl(context, message.smsBody, message.sender)
                Log.d("UnrecognizedSmsViewModel", "Full URL length: ${url.length}")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                
                // Mark as reported
                unrecognizedSmsRepository.markAsReported(listOf(message.id))
                
                Log.d("UnrecognizedSmsViewModel", "Opened report for message from: ${message.sender}")
            } catch (e: Exception) {
                Log.e("UnrecognizedSmsViewModel", "Error opening report", e)
            }
        }
    }
    
    fun deleteMessage(message: UnrecognizedSmsEntity) {
        viewModelScope.launch {
            try {
                // Delete the specific message
                unrecognizedSmsRepository.deleteMessage(message.id)
                Log.d("UnrecognizedSmsViewModel", "Deleted message from: ${message.sender}")
            } catch (e: Exception) {
                Log.e("UnrecognizedSmsViewModel", "Error deleting message", e)
            }
        }
    }

    private val _classifyError = MutableSharedFlow<String>()
    val classifyError: SharedFlow<String> = _classifyError.asSharedFlow()

    /**
     * Classifies a pending message (see [UnrecognizedSmsEntity.bankName] /
     * [UnrecognizedSmsEntity.rawTypeLabel]) as [type]: teaches a rule so every
     * future message with the same (bank, label) resolves automatically, then
     * resolves and saves *this* message's transaction, and removes it from
     * the pending list.
     */
    fun classifyMessage(message: UnrecognizedSmsEntity, type: TransactionType) {
        val bankName = message.bankName
        val rawTypeLabel = message.rawTypeLabel
        if (bankName == null || rawTypeLabel == null) return

        viewModelScope.launch {
            try {
                transactionTypeRuleRepository.teach(bankName, rawTypeLabel, type)

                val parser = BankParserFactory.getParsers(message.sender)
                    .firstOrNull { it.extractRawTypeLabel(message.smsBody) == rawTypeLabel }

                val timestamp = message.receivedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val parsed = parser?.parseWithResolvedType(
                    message.smsBody,
                    message.sender,
                    timestamp,
                    type.toParserCoreType()
                )

                if (parsed != null) {
                    smsTransactionProcessor.saveParsedTransaction(parsed, message.smsBody)
                } else {
                    Log.w("UnrecognizedSmsViewModel", "Rule saved, but couldn't resolve this specific message")
                }

                unrecognizedSmsRepository.deleteMessage(message.id)
            } catch (e: Exception) {
                Log.e("UnrecognizedSmsViewModel", "Error classifying message", e)
                _classifyError.emit("Failed to classify: ${e.message}")
            }
        }
    }
    
    fun deleteAllMessages() {
        viewModelScope.launch {
            try {
                unrecognizedSmsRepository.deleteAll()
                Log.d("UnrecognizedSmsViewModel", "Deleted all unrecognized messages")
            } catch (e: Exception) {
                Log.e("UnrecognizedSmsViewModel", "Error deleting all messages", e)
            }
        }
    }
}
