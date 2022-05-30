package com.umrhsn.mmoire.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener,
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    companion object {
        const val MARGIN_SIZE = 10
    }

    interface ImageClickListener {
        fun onPlaceholderClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        // to make card dimensions dynamic
        val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = min(cardWidth, cardHeight)
        val layoutParams =
            view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imageUris.size) {
            // means that the user has picked an image for that position
            holder.bind(imageUris[position])
        } else {
            // show the default grey background to indicate that user still needs to pick an image
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return boardSize.getNumPairs()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(uri: Uri) {
            // once image view has an image user shouldn't be able to click it again
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }

        fun bind() {
            // user didn't pick a photo
            ivCustomImage.setOnClickListener {
                // launch an implicit intent for the ser to select photos
                imageClickListener.onPlaceholderClicked()
            }
        }
    }
}
