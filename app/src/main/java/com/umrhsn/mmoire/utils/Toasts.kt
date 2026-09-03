package com.umrhsn.mmoire.utils

import android.content.Context
import android.widget.Toast
import com.umrhsn.mmoire.R

fun showToastYouWon(context: Context) {
    Toast.makeText(context, context.getString(R.string.congratulations), Toast.LENGTH_LONG).show()
}

fun showToastSmoothWin(context: Context) {
    Toast.makeText(context, context.getString(R.string.smooth_win_toast), Toast.LENGTH_LONG).show()
}
