package com.umrhsn.mmoire.themes

import android.content.Context
import com.dolatkia.animatedThemeManager.AppTheme

interface MyAppTheme : AppTheme { // TODO
    fun activityBackgroundColor(context: Context) : Int
    fun activityTextColor(context: Context) : Int
    // any other methods for other elements
}