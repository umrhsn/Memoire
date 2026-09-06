package com.umrhsn.mmoire.utils

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    private val prefs: PrefsManager
) {
    private val TAG = "LocaleManager"

    fun getSelectedLanguageTag(): String? {
        val tag = prefs.getLanguage()
        Log.d(TAG, "getSelectedLanguageTag: $tag")
        return tag
    }

    fun applyLanguageTag(tag: String?) {
        Log.d(TAG, "applyLanguageTag: $tag")
        prefs.setLanguage(tag)
        val appLocales = if (tag != null) {
            LocaleListCompat.forLanguageTags(tag)
        } else {
            LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    fun isTagCurrent(tag: String?): Boolean {
        val current = getSelectedLanguageTag()
        val result = current == tag
        Log.d(TAG, "isTagCurrent: target=$tag, current=$current -> result=$result")
        return result
    }
}
