package com.umrhsn.mmoire.utils

import android.content.Context
import android.widget.Toast
import com.umrhsn.mmoire.R

fun showToastAlreadyWon(context: Context) {
    Toast.makeText(context, context.getString(R.string.already_won), Toast.LENGTH_SHORT).show()
}

fun showToastInvalidMove(context: Context) {
    Toast.makeText(context, context.getString(R.string.invalid_move), Toast.LENGTH_SHORT).show()
}

fun showToastYouWon(context: Context) {
    Toast.makeText(context, context.getString(R.string.congratulations), Toast.LENGTH_LONG).show()
}

fun showToastSmoothWin(context: Context) {
    Toast.makeText(context, context.getString(R.string.smooth_win_toast), Toast.LENGTH_LONG).show()
}

fun showToastNothingToRefresh(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.nothing_to_refresh) + " " + UTF_THINKING_EMOJI,
        Toast.LENGTH_SHORT
    ).show()
}
