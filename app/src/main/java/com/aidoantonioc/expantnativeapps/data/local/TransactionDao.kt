package com.aidoantonioc.expantnativeapps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aidoantonioc.expantnativeapps.data.local.entity.ItemEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.SplitShareEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    abstract fun observeAll(): Flow<List<TransactionEntity>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract fun observeDetails(id: String): Flow<TransactionWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItems(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSplitShares(shares: List<SplitShareEntity>)

    @androidx.room.Transaction
    open suspend fun insertWithDetails(
        transaction: TransactionEntity,
        items: List<ItemEntity>,
        shares: List<SplitShareEntity>
    ) {
        insert(transaction)       // parent first, so the foreign keys are valid
        insertItems(items)
        insertSplitShares(shares)
    }
}