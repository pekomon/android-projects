package com.example.pekomon.memorygame.di

import android.content.Context
import androidx.room.Room
import com.example.pekomon.memorygame.data.CardRepositoryImpl
import com.example.pekomon.memorygame.data.local.AppDatabase
import com.example.pekomon.memorygame.domain.repository.CardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "memory_game_db"
            ).build()
        }

        @Provides
        fun provideBestScoreDao(database: AppDatabase) = database.bestScoreDao()
    }
}