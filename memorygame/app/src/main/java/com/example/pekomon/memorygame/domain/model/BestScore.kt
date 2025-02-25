package com.example.pekomon.memorygame.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "best_scores")
data class BestScore(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // There's only one best score
    val score: Int

)