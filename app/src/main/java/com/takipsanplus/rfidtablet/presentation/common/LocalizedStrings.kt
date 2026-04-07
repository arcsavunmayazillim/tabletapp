package com.takipsanplus.rfidtablet.presentation.common

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun localizedString(@StringRes resId: Int, language: AppLanguage): String {
    val context = LocalContext.current
    return remember(context, resId, language) {
        val locale = Locale(language.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        localizedContext.resources.getString(resId)
    }
}

@Composable
fun localizedString(@StringRes resId: Int, language: AppLanguage, vararg formatArgs: Any): String {
    val context = LocalContext.current
    return remember(context, resId, language, formatArgs) {
        val locale = Locale(language.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        localizedContext.resources.getString(resId, *formatArgs)
    }
}

fun localizedString(
    baseContext: android.content.Context,
    @StringRes resId: Int,
    language: AppLanguage
): String {
    val locale = Locale(language.code)
    val config = Configuration(baseContext.resources.configuration)
    config.setLocale(locale)
    val localizedContext = baseContext.createConfigurationContext(config)
    return localizedContext.resources.getString(resId)
}
