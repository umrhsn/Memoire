package com.umrhsn.mmoire.models

import com.umrhsn.mmoire.utils.DEFAULT_CARDS

data class MemoryGame(
    val boardSize: BoardSize,
    val cards: List<MemoryCard>,
    val numPairsFound: Int = 0,
    val numCardFlips: Int = 0,
    val indexOfSingleSelectedCard: Int? = null
) {

    companion object {
        fun create(
            boardSize: BoardSize,
            customImages: List<String>? = null,
            customResources: List<Int>? = null,
            shouldShuffle: Boolean = true
        ): MemoryGame {
            val numPairs = boardSize.getNumPairs()
            val cards = if (customImages != null) {
                // User provided string URLs
                val chosenImages = customImages.take(numPairs)
                val randomizedImages =
                    (chosenImages + chosenImages).let { if (shouldShuffle) it.shuffled() else it }
                randomizedImages.map { card -> MemoryCard(card.hashCode(), card) }
            } else {
                // Use resource IDs
                val chosenResources = customResources ?: DEFAULT_CARDS.shuffled().take(numPairs)
                val randomizedResources =
                    (chosenResources + chosenResources).let { if (shouldShuffle) it.shuffled() else it }
                randomizedResources.map { card -> MemoryCard(card, null) }
            }
            return MemoryGame(boardSize, cards)
        }
    }

    fun flipCard(position: Int): Pair<MemoryGame, Boolean> {
        val card = cards[position]
        if (card.isFaceUp || card.isMatched) return Pair(this, false)

        val newNumCardFlips = numCardFlips + 1
        var newCards = cards.toMutableList()
        var newNumPairsFound = numPairsFound
        val newIndexOfSingleSelectedCard: Int?
        var foundMatch = false

        if (indexOfSingleSelectedCard == null) {
            // Case 1: 0 cards previously flipped over or just restored
            newCards = newCards.map { if (!it.isMatched) it.copy(isFaceUp = false) else it }
                .toMutableList()
            newCards[position] = newCards[position].copy(isFaceUp = true)
            newIndexOfSingleSelectedCard = position
        } else {
            // Case 2: 1 card previously flipped over
            val card1 = newCards[indexOfSingleSelectedCard]
            val card2 = newCards[position]

            if (card1.identifier == card2.identifier) {
                foundMatch = true
                newNumPairsFound++
                newCards[indexOfSingleSelectedCard] = card1.copy(isMatched = true, isFaceUp = true)
                newCards[position] = card2.copy(isMatched = true, isFaceUp = true)
            } else {
                newCards[position] = card2.copy(isFaceUp = true)
            }
            newIndexOfSingleSelectedCard = null
        }

        return Pair(
            this.copy(
                cards = newCards,
                numPairsFound = newNumPairsFound,
                numCardFlips = newNumCardFlips,
                indexOfSingleSelectedCard = newIndexOfSingleSelectedCard
            ),
            foundMatch
        )
    }

    fun getNumMoves(): Int = numCardFlips / 2

    fun haveWonGame(): Boolean = numPairsFound == boardSize.getNumPairs()

    fun smoothWin(): Boolean = haveWonGame() && (getNumMoves() == boardSize.getNumPairs())
}
