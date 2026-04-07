package com.takipsanplus.rfidtablet.presentation.common

import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    TURKISH("tr"),
    SPANISH("es"),
    FRENCH("fr"),
    VIETNAMESE("vi")
}

object LanguageManager {
    fun applyLanguage(language: AppLanguage) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun currentLanguage(): AppLanguage {
        val systemLocales = AppCompatDelegate.getApplicationLocales()
        val code = systemLocales[0]?.language ?: LocaleList.getDefault()[0].language
        return AppLanguage.entries.firstOrNull { it.code == code } ?: AppLanguage.ENGLISH
    }
}
