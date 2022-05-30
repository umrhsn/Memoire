package com.umrhsn.mmoire.themes

import android.content.Context
import androidx.core.content.ContextCompat
import com.umrhsn.mmoire.R

class LightTheme : MyAppTheme { // TODO
    override fun id(): Int = 0

    override fun activityBackgroundColor(context: Context): Int = ContextCompat.getColor(context, R.color.light_purple)

    override fun activityTextColor(context: Context): Int = ContextCompat.getColor(context, R.color.black)
}