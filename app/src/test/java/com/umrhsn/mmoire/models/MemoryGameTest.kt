package com.umrhsn.mmoire.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MemoryGameTest {

    @Test
    fun `initial game state is correct`() {
        val boardSize = BoardSize.EASY
        val game = MemoryGame.create(boardSize, null)

        assertEquals(boardSize, game.boardSize)
        assertEquals(boardSize.numCards, game.cards.size)
        assertEquals(0, game.numPairsFound)
        assertEquals(0, game.numCardFlips)
        assertNull(game.indexOfSingleSelectedCard)
        assertFalse(game.haveWonGame())
    }

    @Test
    fun `flipping a card updates state`() {
        val game = MemoryGame.create(BoardSize.EASY, null)
        val (updatedGame, foundMatch) = game.flipCard(0)

        assertTrue(updatedGame.cards[0].isFaceUp)
        assertEquals(1, updatedGame.numCardFlips)
        assertEquals(0, updatedGame.indexOfSingleSelectedCard)
        assertFalse(foundMatch)
    }

    @Test
    fun `flipping two matching cards updates matched state`() {
        // Create a non-shuffled game
        val customImages = listOf("url1", "url2", "url3")
        val boardSize = BoardSize.SUPER_DUPER_EASY // 6 cards
        // Cards should be: url1, url2, url3, url1, url2, url3
        val game = MemoryGame.create(boardSize, customImages, shouldShuffle = false)
        
        val (gameAfterFirstFlip, _) = game.flipCard(0) // url1
        val (gameAfterSecondFlip, foundMatch) = gameAfterFirstFlip.flipCard(3) // matching url1

        assertTrue(foundMatch)
        assertEquals(1, gameAfterSecondFlip.numPairsFound)
        assertTrue(gameAfterSecondFlip.cards[0].isMatched)
        assertTrue(gameAfterSecondFlip.cards[3].isMatched)
    }

    @Test
    fun `flipping two non-matching cards does not update matched state`() {
        val customImages = listOf("url1", "url2", "url3")
        val boardSize = BoardSize.SUPER_DUPER_EASY
        val game = MemoryGame.create(boardSize, customImages, shouldShuffle = false)

        val (gameAfterFirstFlip, _) = game.flipCard(0) // url1
        val (gameAfterSecondFlip, foundMatch) = gameAfterFirstFlip.flipCard(1) // url2

        assertFalse(foundMatch)
        assertEquals(0, gameAfterSecondFlip.numPairsFound)
        assertFalse(gameAfterSecondFlip.cards[0].isMatched)
        assertFalse(gameAfterSecondFlip.cards[1].isMatched)
    }
}
