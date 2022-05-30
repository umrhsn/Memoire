package com.umrhsn.mmoire.utils

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog

fun showAlertDialog(
    context: Context,
    title: String,
    message: String?,
    view: View?,
    positiveClickListener: View.OnClickListener,
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setView(view)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("OK")
        { _, _ -> positiveClickListener.onClick(null) }
        .show()
}