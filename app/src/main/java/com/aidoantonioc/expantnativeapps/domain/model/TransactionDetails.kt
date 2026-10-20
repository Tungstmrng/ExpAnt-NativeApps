package com.aidoantonioc.expantnativeapps.domain.model

data class LineItem(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val qty: Int,
    val discount: Double
) {
    // Assumption: price is per unit and discount applies to the whole line.
    // Adjust this once we see how your OCR backend reports discounts.
    val subtotal: Double get() = price * qty - discount
}

data class SplitShare(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val isPaid: Boolean
)

data class TransactionDetails(
    val transaction: Transaction,
    val items: List<LineItem>,
    val splitShares: List<SplitShare>
)