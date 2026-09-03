package com.umrhsn.mmoire.networking

import android.graphics.Bitmap
import androidx.core.graphics.scale

object BitmapScaler {
    /**
     * The intention of these methods is to keep the same aspect ratio of the [Bitmap] but scale it down as per width or height passed in
     * */

    /** scale and maintain aspect ratio given a desired height */
    fun scaleToFitHeight(b: Bitmap, height: Int): Bitmap {
        /** the math is:
         * downscaling factor = desired height (passed in as a parameter) / Bitmap's original height
         * new width = downscaling factor * original width */
        val factor = height / b.height.toFloat()
        return b.scale((b.width * factor).toInt(), height, true)
    }
}