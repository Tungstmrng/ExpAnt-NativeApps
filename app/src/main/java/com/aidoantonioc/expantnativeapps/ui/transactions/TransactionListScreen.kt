package com.aidoantonioc.expantnativeapps.ui.transactions


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aidoantonioc.expantnativeapps.domain.model.Transaction
import com.aidoantonioc.expantnativeapps.ui.util.formatRupiah

@Composable
fun TransactionListRoute(
    onAddClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: TransactionListViewModel = viewModel(factory = TransactionListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionListScreen(
        uiState = uiState,
        onAddClick = onAddClick,
        onTransactionClick = onTransactionClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    uiState: TransactionListUiState,
    onAddClick: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Transactions") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                TransactionListUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                TransactionListUiState.Empty ->
                    Text(
                        text = "No transactions yet.\nTap + to add one.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                is TransactionListUiState.Error ->
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )

                is TransactionListUiState.Success ->
                    TransactionList(uiState.transactions, onTransactionClick)
            }
        }
    }
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = transactions, key = { it.id }) { transaction ->
            TransactionItem(transaction, onClick = { onTransactionClick(transaction.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchantName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatRupiah(transaction.totalAmount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionListPreview() {
    MaterialTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Success(
                listOf(
                    Transaction("1", "Indomaret", 54_500.0, "2026-10-19"),
                    Transaction("2", "Warung Bu Sri", 32_000.0, "2026-10-18")
                )
            ),
            onAddClick = {},
            onTransactionClick = {}
        )
    }
}