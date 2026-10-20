package com.aidoantonioc.expantnativeapps.ui.transactions


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aidoantonioc.expantnativeapps.ExpAntApplication
import com.aidoantonioc.expantnativeapps.data.repository.TransactionRepository
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import com.aidoantonioc.expantnativeapps.ui.navigation.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState
    data class Success(val details: TransactionDetails) : TransactionDetailUiState
}

class TransactionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: TransactionRepository
) : ViewModel() {

    // Navigation puts the route argument into the SavedStateHandle for us.
    private val transactionId: String = checkNotNull(savedStateHandle[Routes.ARG_ID])

    val uiState: StateFlow<TransactionDetailUiState> =
        repository.observeTransactionDetails(transactionId)
            .map<TransactionDetails?, TransactionDetailUiState> { details ->
                if (details == null) TransactionDetailUiState.NotFound
                else TransactionDetailUiState.Success(details)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionDetailUiState.Loading
            )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as ExpAntApplication
                TransactionDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = app.container.transactionRepository
                )
            }
        }
    }
}