package com.umrhsn.mmoire.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    private val prefs: PrefsManager
) {
    fun getSelectedLanguageTag(): String? {
        return prefs.getLanguage()
    }

    fun applyLanguageTag(tag: String?) {
        prefs.setLanguage(tag)
        val appLocales = if (tag != null) {
            LocaleListCompat.forLanguageTags(tag)
        } else {
            LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    fun isTagCurrent(tag: String?): Boolean {
        return getSelectedLanguageTag() == tag
    }
}
