package com.example.pekomon.memorygame

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.pekomon.memorygame.domain.model.Card
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CardGridKeyTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reordering_cards_does_not_dispose_with_stable_keys() {
        val disposeCounts = mutableMapOf<Int, Int>()
        val cards = mutableStateListOf(
            Card(id = 1, imageRes = 0),
            Card(id = 2, imageRes = 0),
            Card(id = 3, imageRes = 0)
        )
        composeTestRule.setContent {
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(items = cards, key = { it.id }) { card ->
                    DisposableEffect(card.id) {
                        onDispose {
                            disposeCounts[card.id] = disposeCounts.getOrDefault(card.id, 0) + 1
                        }
                    }
                }
            }
        }
        composeTestRule.runOnIdle {
            val first = cards.removeAt(0)
            cards.add(1, first)
        }
        composeTestRule.runOnIdle {
            assertTrue(disposeCounts.isEmpty())
        }
    }
}
