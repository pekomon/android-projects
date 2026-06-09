package com.pekomon.cryptoapp.domain.repository

import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    val positions: Flow<List<PortfolioPosition>>

    suspend fun updatePositions(positions: List<PortfolioPosition>)
}
