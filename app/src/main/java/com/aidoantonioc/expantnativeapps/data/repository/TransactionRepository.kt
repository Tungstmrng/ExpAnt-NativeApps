package com.aidoantonioc.expantnativeapps.data.repository


import com.aidoantonioc.expantnativeapps.data.local.TransactionDao
import com.aidoantonioc.expantnativeapps.domain.model.Transaction
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeTransactionDetails(id: String): Flow<TransactionDetails?>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun addTransactionWithDetails(details: TransactionDetails)
}

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeTransactionDetails(id: String): Flow<TransactionDetails?> =
        dao.observeDetails(id).map { it?.toDomain() }

    override suspend fun addTransaction(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun addTransactionWithDetails(details: TransactionDetails) {
        val txId = details.transaction.id
        dao.insertWithDetails(
            transaction = details.transaction.toEntity(),
            items = details.items.map { it.toEntity(txId) },
            shares = details.splitShares.map { it.toEntity(txId) }
        )
    }
}