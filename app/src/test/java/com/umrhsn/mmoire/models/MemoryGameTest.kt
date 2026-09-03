package com.umrhsn.mmoire.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `haveWonGame returns true only when all pairs are found`() {
        val boardSize = BoardSize.SUPER_DUPER_EASY // 3 pairs
        val game = MemoryGame.create(boardSize, listOf("1", "2", "3"), shouldShuffle = false)

        val (g1, _) = game.flipCard(0).first.flipCard(3) // pair 1
        assertFalse(g1.haveWonGame())

        val (g2, _) = g1.flipCard(1).first.flipCard(4) // pair 2
        assertFalse(g2.haveWonGame())

        val (g3, _) = g2.flipCard(2).first.flipCard(5) // pair 3
        assertTrue(g3.haveWonGame())
    }

    @Test
    fun `smoothWin returns true when won with minimum moves`() {
        val boardSize = BoardSize.SUPER_DUPER_EASY // 3 pairs
        val game = MemoryGame.create(boardSize, listOf("1", "2", "3"), shouldShuffle = false)

        // Perfect game
        val gFinal = game.flipCard(0).first.flipCard(3).first // move 1
            .flipCard(1).first.flipCard(4).first // move 2
            .flipCard(2).first.flipCard(5).first // move 3

        assertTrue(gFinal.haveWonGame())
        assertTrue(gFinal.smoothWin())
    }

    @Test
    fun `flipping a third card hides previous non-matching pair`() {
        val game = MemoryGame.create(
            BoardSize.SUPER_DUPER_EASY,
            listOf("1", "2", "3"),
            shouldShuffle = false
        )

        val (g1, _) = game.flipCard(0) // Show card 0
        val (g2, _) = g1.flipCard(1) // Show card 1 (no match)
        assertTrue(g2.cards[0].isFaceUp)
        assertTrue(g2.cards[1].isFaceUp)

        val (g3, _) = g2.flipCard(2) // Flip card 2
        // Cards 0 and 1 should be face down now
        assertFalse(g3.cards[0].isFaceUp)
        assertFalse(g3.cards[1].isFaceUp)
        assertTrue(g3.cards[2].isFaceUp)
    }
}
