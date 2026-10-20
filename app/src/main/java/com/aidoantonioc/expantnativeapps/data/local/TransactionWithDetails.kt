package com.aidoantonioc.expantnativeapps.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.aidoantonioc.expantnativeapps.data.local.entity.ItemEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.SplitShareEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.TransactionEntity

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,

    @Relation(parentColumn = "id", entityColumn = "transaction_id")
    val items: List<ItemEntity>,

    @Relation(parentColumn = "id", entityColumn = "transaction_id")
    val splitShares: List<SplitShareEntity>
)