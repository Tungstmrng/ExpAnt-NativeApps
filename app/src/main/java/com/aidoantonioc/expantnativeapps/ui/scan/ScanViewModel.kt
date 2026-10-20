package com.aidoantonioc.expantnativeapps.ui.scan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aidoantonioc.expantnativeapps.ExpAntApplication
import com.aidoantonioc.expantnativeapps.data.repository.ReceiptScanRepository
import com.aidoantonioc.expantnativeapps.data.repository.TransactionRepository
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Scanning(val imageUri: Uri) : ScanUiState
    data class Review(val imageUri: Uri, val details: TransactionDetails) : ScanUiState
    data class Error(val message: String, val imageUri: Uri) : ScanUiState
    data object Saved : ScanUiState
}

class ScanViewModel(
    private val scanRepository: ReceiptScanRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun onImageSelected(uri: Uri) {
        scanJob?.cancel()
        _uiState.value = ScanUiState.Scanning(uri)
        scanJob = viewModelScope.launch {
            scanRepository.scan(uri)
                .onSuccess { _uiState.value = ScanUiState.Review(uri, it) }
                .onFailure {
                    _uiState.value = ScanUiState.Error(it.message ?: "Scan failed", uri)
                }
        }
    }

    fun retry() {
        (_uiState.value as? ScanUiState.Error)?.let { onImageSelected(it.imageUri) }
    }

    fun reset() {
        scanJob?.cancel()
        _uiState.value = ScanUiState.Idle
    }

    fun save() {
        val review = _uiState.value as? ScanUiState.Review ?: return
        viewModelScope.launch {
            transactionRepository.addTransactionWithDetails(review.details)
            _uiState.value = ScanUiState.Saved
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as ExpAntApplication
                ScanViewModel(
                    scanRepository = app.container.receiptScanRepository,
                    transactionRepository = app.container.transactionRepository
                )
            }
        }
    }
}