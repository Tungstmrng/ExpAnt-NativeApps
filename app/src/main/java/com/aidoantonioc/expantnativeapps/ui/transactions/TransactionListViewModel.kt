package com.aidoantonioc.expantnativeapps.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aidoantonioc.expantnativeapps.ExpAntApplication
import com.aidoantonioc.expantnativeapps.data.repository.TransactionRepository
import com.aidoantonioc.expantnativeapps.domain.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import com.aidoantonioc.expantnativeapps.domain.model.LineItem
import com.aidoantonioc.expantnativeapps.domain.model.SplitShare
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails

sealed interface TransactionListUiState {
    data object Loading : TransactionListUiState
    data object Empty : TransactionListUiState
    data class Success(val transactions: List<Transaction>) : TransactionListUiState
    data class Error(val message: String) : TransactionListUiState
}

class TransactionListViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<TransactionListUiState> = repository.observeTransactions()
        .map<List<Transaction>, TransactionListUiState> { list ->
            if (list.isEmpty()) TransactionListUiState.Empty
            else TransactionListUiState.Success(list)
        }
        .catch { emit(TransactionListUiState.Error(it.message ?: "Something went wrong")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState.Loading
        )

    // TEMPORARY: lets us test the Room -> UI flow before the real "Add" screen exists.
    // We delete this in Tahap 6.
    fun addSampleTransaction() {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val items = listOf(
                LineItem("$id-1", "Mie Instan", 3_500.0, "Food", 4, 0.0),
                LineItem("$id-2", "Air Mineral 600ml", 4_000.0, "Drink", 2, 500.0),
                LineItem("$id-3", "Sabun Cair", 18_500.0, "Household", 1, 2_000.0)
            )
            val total = items.sumOf { it.subtotal }
            repository.addTransactionWithDetails(
                TransactionDetails(
                    transaction = Transaction(
                        id = id,
                        merchantName = listOf("Indomaret", "Alfamart", "Warung Bu Sri").random(),
                        totalAmount = total,
                        date = LocalDate.now().toString()
                    ),
                    items = items,
                    splitShares = listOf(
                        SplitShare(name = "Andi", amount = total / 2, isPaid = true),
                        SplitShare(name = "Budi", amount = total / 2, isPaid = false)
                    )
                )
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as ExpAntApplication
                TransactionListViewModel(app.container.transactionRepository)
            }
        }
    }
}