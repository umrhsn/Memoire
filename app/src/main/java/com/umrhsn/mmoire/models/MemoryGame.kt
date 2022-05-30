package com.umrhsn.mmoire.models

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.activities.MainActivity
import com.umrhsn.mmoire.utils.*

class MemoryGame(private val boardSize: BoardSize, customImages: List<String>?) {

    companion object {
        private const val TAG = "MemoryGame"
    }

    val cards: List<MemoryCard> // has: identifier, isFaceUp, isMatched
    var numPairsFound = 0 // to get number of pairs of cards matched together
    private var numCardFlips = 0 // numCardFlips / 2 = numMoves made by user
    private var indexOfSingleSelectedCard: Int? = null // to get the current card's position

    /** if there are NO [customImages] chosen by user, we set the image List to be [DEFAULT_CARDS] */
    init {
        when (customImages) {
            null -> {
                val chosenImages = DEFAULT_CARDS.shuffled().take(boardSize.getNumPairs())
                val randomizedImages = (chosenImages + chosenImages).shuffled()
                cards = randomizedImages.map { card -> MemoryCard(card, null) }
                cards.forEach { card -> Log.i(TAG, "card is ${card.identifier}") }
            }
            else -> {
                val randomizedImages = (customImages + customImages).shuffled()
                cards = randomizedImages.map { card -> MemoryCard(card.hashCode(), card) }
                cards.forEach { card -> Log.i(TAG, "card is ${card.identifier}") }
            }
        }
    }

    // this function describes what happens when user flips a card
    fun flipCard(position: Int): Boolean {
        numCardFlips++
        Log.i(TAG, "numCardFlips = $numCardFlips")

        val card = cards[position]
        /*
        when user clicks a card:
        case1   0 cards previously flipped over => flip over the selected card.
        case2   1 card previously flipped over => flip over selected card, check if images match.
        case3   2 cards previously flipped over => restore cards + flip over the selected card.

        we can merge cases 1, 3 together as they function the same. so we have 2 cases now:
        case1   0 or 2 cards previously flipped over => restore cards + flip over the selected card.
        case2   1 card previously flipped over => flip over selected card, check if images match.
        */
        var foundMatch = false
        if (indexOfSingleSelectedCard == null) { // case1
            restoreCards()
            indexOfSingleSelectedCard = position
        } else { // case2
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp // the actual flip effect
        Log.i(TAG, "card[$position].isFaceUp = ${card.isFaceUp}")
        return foundMatch
    }
    
    /** if 2 face-up cards are not matched they should return to face-down */
    private fun restoreCards() =
        cards.forEach { card -> if (!card.isMatched) card.isFaceUp = false }

    /** to check if the card at position 1 is the same card at position 2 */
    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier) return false
        // if it reaches the code below that means we have a match
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        Log.i(TAG, "numPairsFound = $numPairsFound")
        return true
    }

    /** to check if a card is face-up */
    fun isCardFaceUp(position: Int): Boolean = cards[position].isFaceUp

    /** to get the number of moves made by user to be updated in the UI */
    fun getNumMoves(): Int = numCardFlips / 2

    /** user wins the game if number of pairs matched == number of pairs of the board */
    fun haveWonGame(): Boolean = numPairsFound == boardSize.getNumPairs()

    fun smoothWin(): Boolean = haveWonGame() && (getNumMoves() == boardSize.getNumPairs())
}
