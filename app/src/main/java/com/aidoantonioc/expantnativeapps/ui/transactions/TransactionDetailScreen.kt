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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aidoantonioc.expantnativeapps.domain.model.LineItem
import com.aidoantonioc.expantnativeapps.domain.model.SplitShare
import com.aidoantonioc.expantnativeapps.domain.model.Transaction
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import com.aidoantonioc.expantnativeapps.ui.util.formatRupiah

@Composable
fun TransactionDetailRoute(
    onBack: () -> Unit,
    viewModel: TransactionDetailViewModel = viewModel(factory = TransactionDetailViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionDetailScreen(uiState = uiState, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                TransactionDetailUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                TransactionDetailUiState.NotFound ->
                    Text("Transaction not found.", Modifier.align(Alignment.Center))

                is TransactionDetailUiState.Success ->
                    DetailContent(uiState.details)
            }
        }
    }
}

@Composable
private fun DetailContent(details: TransactionDetails) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { HeaderCard(details.transaction) }

        item { SectionTitle("Items") }
        if (details.items.isEmpty()) {
            item { HintText("No items recorded.") }
        } else {
            items(details.items, key = { "item-${it.id}" }) { ItemRow(it) }
        }

        item { SectionTitle("Split bill") }
        if (details.splitShares.isEmpty()) {
            item { HintText("No split shares yet.") }
        } else {
            items(details.splitShares, key = { "share-${it.id}" }) { SplitShareRow(it) }
        }
    }
}

@Composable
private fun HeaderCard(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(transaction.merchantName, style = MaterialTheme.typography.headlineSmall)
            Text(
                transaction.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatRupiah(transaction.totalAmount),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ItemRow(item: LineItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${item.qty} × ${formatRupiah(item.price)} · ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.discount > 0) {
                    Text(
                        text = "Discount −${formatRupiah(item.discount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(formatRupiah(item.subtotal), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SplitShareRow(share: SplitShare) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(share.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (share.isPaid) "Paid" else "Not paid yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (share.isPaid) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
            Text(formatRupiah(share.amount), style = MaterialTheme.typography.bodyLarge)
        }
    }
}