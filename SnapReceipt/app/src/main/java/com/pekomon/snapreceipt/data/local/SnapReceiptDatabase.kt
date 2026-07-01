package com.pekomon.snapreceipt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ReceiptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SnapReceiptDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao

    companion object {
        fun create(context: Context): SnapReceiptDatabase {
            return Room.databaseBuilder(
                context,
                SnapReceiptDatabase::class.java,
                "snapreceipt.db"
            ).build()
        }
    }
}
