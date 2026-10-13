package com.aidoantonioc.expantnativeapps.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aidoantonioc.expantnativeapps.data.local.entity.ItemEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.SplitShareEntity
import com.aidoantonioc.expantnativeapps.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        ItemEntity::class,
        SplitShareEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "expantnative.db"
            ).build()
    }
}