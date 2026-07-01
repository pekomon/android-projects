package com.pekomon.snapreceipt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query(
        """
        SELECT * FROM receipts
        ORDER BY transactionDateIso DESC, updatedAtEpochMillis DESC
        """
    )
    fun observeReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :receiptId LIMIT 1")
    suspend fun getReceipt(receiptId: String): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteReceiptById(receiptId: String)
}
