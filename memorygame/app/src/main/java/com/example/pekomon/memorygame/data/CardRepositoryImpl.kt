package com.example.pekomon.memorygame.data

import com.example.pekomon.memorygame.R
import com.example.pekomon.memorygame.data.local.BestScoreDao
import com.example.pekomon.memorygame.domain.model.BestScore
import com.example.pekomon.memorygame.domain.model.Card
import com.example.pekomon.memorygame.domain.repository.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val bestScoreDao: BestScoreDao
) : CardRepository {
    override fun getCards(): List<Card> {
        val images = listOf(
            R.drawable.image1, R.drawable.image2, R.drawable.image3,R.drawable.image4,
            R.drawable.image1, R.drawable.image2, R.drawable.image3,R.drawable.image4
        ).shuffled()

        return images.mapIndexed { index, resourceId ->
            Card(id = index, imageRes = resourceId)

        }
    }

    override suspend fun getBestScore(): Int? {
        return withContext(Dispatchers.IO) {
            bestScoreDao.getBestScore()?.score
        }
    }

    override suspend fun saveBestScore(score: Int) {
        withContext(Dispatchers.IO) {
            bestScoreDao.insertBestScore(BestScore(score = score))
        }
    }
}