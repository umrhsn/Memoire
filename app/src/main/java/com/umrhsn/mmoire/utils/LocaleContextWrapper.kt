package com.umrhsn.mmoire.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, languageTag: String?): ContextWrapper {
            val locale = if (languageTag != null) {
                Locale.forLanguageTag(languageTag)
            } else {
                // Use system default if no app-specific language is set
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
            }

            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }

            val newContext = context.createConfigurationContext(config)
            return LocaleContextWrapper(newContext)
        }
    }
}
