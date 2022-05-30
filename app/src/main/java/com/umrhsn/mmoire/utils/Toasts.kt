package com.umrhsn.mmoire.utils

import android.content.Context
import android.widget.Toast

fun showToastHaveAlreadyWon(context: Context) =
    Toast.makeText(context, "You've already won", Toast.LENGTH_SHORT).show()

fun showToastInvalidMove(context: Context) =
    Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show()

fun showToastYouWon(context: Context) =
    Toast.makeText(context, "You won! Congratulations!", Toast.LENGTH_LONG).show()

fun showToastSmoothWin(context: Context) =
    Toast.makeText(context, "Smooth! You won without a single mistake!", Toast.LENGTH_LONG).show()

fun showToastNothingToRefresh(context: Context) =
    Toast.makeText(context, "Nothing to refresh here $UTF_THINKING_EMOJI", Toast.LENGTH_SHORT)
        .show()