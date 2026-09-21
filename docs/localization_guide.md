# Localization Guide: Adding a New Language

This document explains the steps required to add a new language localization to the Mémoire application.

## 1. Resource Configuration
In `app/build.gradle`, add the new language code (e.g., `"it"`, `"ar-rSY"`) to the `resConfigs` line to ensure these resources are bundled with the APK.

```gradle
defaultConfig {
    // ...
    resConfigs "en", "ar", "ar-rEG", "ar-rSY", "de", "fr", "es", "it"
}
```

## 2. Locales Configuration
Add the new locale to `app/src/main/res/xml/locales_config.xml`. This is required for Android 13+ per-app language selection.

```xml
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en" />
    <locale android:name="ar-SY" />
    <!-- Add new locale here -->
    <locale android:name="it" />
</locale-config>
```

## 3. Base Context Injection
To ensure the selected locale is loaded correctly when the app starts or an activity is created, override `attachBaseContext` in both `MainActivity.kt` and `SettingsActivity.kt`.

### MainActivity.kt & SettingsActivity.kt
```kotlin
override fun attachBaseContext(newBase: Context) {
    val prefs = newBase.getSharedPreferences("memoire_prefs", Context.MODE_PRIVATE)
    val localeTag = prefs.getString("app_language", null)
    val context = if (localeTag != null) {
        val locale = Locale.forLanguageTag(localeTag)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        newBase.createConfigurationContext(config)
    } else {
        newBase
    }
    super.attachBaseContext(context)
}
```

## 4. UI Integration (Settings Screen)
Add the new language option to the `languages` list in `SettingsScreen.kt`.

```kotlin
val languages = listOf(
    null to stringResource(R.string.lang_system) to "🌐",
    "ar-EG" to stringResource(R.string.lang_ar_eg) to "🇪🇬",
    "ar-SY" to stringResource(R.string.lang_ar_sy) to "🇸🇾",
    "it" to stringResource(R.string.lang_it) to "🇮🇹", // New language
    // ...
)
```

## 5. View Model Logic
If the new language requires tag normalization (like Syrian Arabic `ar-rSY` to `ar-SY`), update `confirmLanguageChange` in `SettingsViewModel.kt`.

```kotlin
fun confirmLanguageChange() {
    val lang = _uiState.value.pendingLanguage
    
    // Normalize tags if necessary
    val finalLang = if (lang == "ar-rSY") "ar-SY" else lang

    localeManager.applyLanguageTag(finalLang)
    _uiState.update {
        it.copy(
            currentLanguage = finalLang,
            showRestartDialog = false,
            pendingLanguage = null
        )
    }
}
```

## 6. Translation Files
Create the actual translation file at `app/src/main/res/values-<lang>/strings.xml`.

Example for Italian: `app/src/main/res/values-it/strings.xml`

> [!IMPORTANT]
> Ensure that all new string resource keys (like `lang_it`) are also added to the default `values/strings.xml` and all other translation files to avoid resource lookup crashes.
