package com.aidoantonioc.expantnativeapps.data.repository


import com.aidoantonioc.expantnativeapps.data.local.TransactionWithDetails
import com.aidoantonioc.expantnativeapps.data.local.entity.ItemEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.SplitShareEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.TransactionEntity
import com.aidoantonioc.expantnativeapps.domain.model.LineItem
import com.aidoantonioc.expantnativeapps.domain.model.SplitShare
import com.aidoantonioc.expantnativeapps.domain.model.Transaction
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    merchantName = merchantName,
    totalAmount = totalAmount,
    date = date
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    merchantName = merchantName,
    totalAmount = totalAmount,
    date = date
)

fun ItemEntity.toDomain() = LineItem(
    id = id,
    name = name,
    price = price,
    category = category,
    qty = qty,
    discount = discount
)

fun LineItem.toEntity(transactionId: String) = ItemEntity(
    id = id,
    transactionId = transactionId,
    name = name,
    price = price,
    category = category,
    qty = qty,
    discount = discount
)

fun SplitShareEntity.toDomain() = SplitShare(
    id = id,
    name = name,
    amount = amount,
    isPaid = isPaid == 1
)

fun SplitShare.toEntity(transactionId: String) = SplitShareEntity(
    id = id,
    transactionId = transactionId,
    name = name,
    amount = amount,
    isPaid = if (isPaid) 1 else 0
)

fun TransactionWithDetails.toDomain() = TransactionDetails(
    transaction = transaction.toDomain(),
    items = items.map { it.toDomain() },
    splitShares = splitShares.map { it.toDomain() }
)