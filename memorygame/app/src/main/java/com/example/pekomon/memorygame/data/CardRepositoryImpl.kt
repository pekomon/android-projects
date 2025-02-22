package com.example.pekomon.memorygame.data

import com.example.pekomon.memorygame.R
import com.example.pekomon.memorygame.domain.model.Card
import com.example.pekomon.memorygame.domain.repository.CardRepository
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor() : CardRepository {
    override fun getCards(): List<Card> {
        val images = listOf(
            R.drawable.image1, R.drawable.image2, R.drawable.image3,R.drawable.image4,
            R.drawable.image1, R.drawable.image2, R.drawable.image3,R.drawable.image4
        ).shuffled()

        return images.mapIndexed { index, resourceId ->
            Card(id = index, imageRes = resourceId)

        }
    }
}