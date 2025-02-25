package com.example.pekomon.memorygame.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pekomon.memorygame.domain.model.BestScore

@Database(entities = [BestScore::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bestScoreDao(): BestScoreDao
}