package com.aidoantonioc.expantnativeapps.domain.model

data class Transaction(
    val id: String,
    val merchantName: String,
    val totalAmount: Double,
    val date: String
)