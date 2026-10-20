package com.aidoantonioc.expantnativeapps.data.remote

import com.aidoantonioc.expantnativeapps.domain.model.LineItem
import com.aidoantonioc.expantnativeapps.domain.model.Transaction
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import java.time.LocalDate
import java.util.UUID

fun ReceiptScanResponse.toTransactionDetails(): TransactionDetails {
    val txId = UUID.randomUUID().toString()

    val lineItems = items.orEmpty().mapIndexed { index, dto ->
        LineItem(
            id = "$txId-${index + 1}",
            name = dto.name?.trim().takeUnless { it.isNullOrBlank() } ?: "Item ${index + 1}",
            price = dto.price ?: 0.0,
            category = dto.category?.trim().takeUnless { it.isNullOrBlank() } ?: "Uncategorized",
            qty = (dto.qty ?: 1).coerceAtLeast(1),
            discount = dto.discount ?: 0.0
        )
    }

    return TransactionDetails(
        transaction = Transaction(
            id = txId,
            merchantName = merchantName?.trim().takeUnless { it.isNullOrBlank() } ?: "Unknown merchant",
            totalAmount = totalAmount ?: lineItems.sumOf { it.subtotal },
            date = normalizeDate(date)
        ),
        items = lineItems,
        splitShares = emptyList()
    )
}

// The list is sorted by this string, so it must be ISO (yyyy-MM-dd). Fall back to today if OCR gave junk.
private fun normalizeDate(raw: String?): String =
    raw?.trim()?.take(10)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?.toString()
        ?: LocalDate.now().toString()