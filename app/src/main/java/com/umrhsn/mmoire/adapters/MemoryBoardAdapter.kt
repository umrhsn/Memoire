package com.umrhsn.mmoire.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener,
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    companion object {
        const val MARGIN_SIZE = 10
        const val TAG = "BoardAdapter"
    }

    interface CardClickListener {
        fun onCardClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // customizing cardSideLength
        val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = min(cardWidth, cardHeight)

        // grabbing the memory_card CardView
        val view =
            LayoutInflater.from(context).inflate(R.layout.card_memory, parent, false)

        // assigning the memory_card CardView's layoutParams to cardSideLength and MARGIN_SIZE
        val layoutParams =
            view.findViewById<CardView>(R.id.cvMemoryCard).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return boardSize.numCards
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ibMemoryCard = itemView.findViewById<ImageButton>(R.id.ibMemoryCard)

        fun bind(position: Int) {
            // bind memory cards list to imageButtons
            val memoryCard = cards[position]

            // what image to show if card is face-up or face-down
            if (memoryCard.isFaceUp) {
                if (memoryCard.imageUrl != null) {
                    Picasso.get().load(memoryCard.imageUrl).placeholder(R.drawable.image_loading)
                        .into(ibMemoryCard)
                } else {
                    ibMemoryCard.setImageResource(memoryCard.identifier)
                }
            } else {
                ibMemoryCard.setImageResource(R.drawable.memory_card_facedown)
            }

            // change the opacity of the image buttons of 2 matched cards
            ibMemoryCard.alpha = if (memoryCard.isMatched) 0.4f else 1.0f

            // add a color grey to matched cards to be easily distinguished
            val colorStateList =
                if (memoryCard.isMatched) ContextCompat.getColorStateList(context, R.color.grey)
                else null
            ViewCompat.setBackgroundTintList(ibMemoryCard, colorStateList)

            // setOnClickListener
            ibMemoryCard.setOnClickListener {
                cardClickListener.onCardClicked(position)
                Log.i(TAG, "clicked on card at position $position")
            }
        }
    }
}
