package com.example.pekomon.memorygame.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pekomon.memorygame.domain.model.BestScore

@Dao
interface BestScoreDao {
    @Query("SELECT * FROM best_score LIMIT 1")
    suspend fun getBestScore(): BestScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBestScore(bestScore: BestScore)
}