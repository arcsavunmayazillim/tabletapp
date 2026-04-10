package com.takipsanplus.rfidtablet.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import com.takipsanplus.rfidtablet.R

private val googleFontProvider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val rethinkSansFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Rethink Sans"),
        fontProvider = googleFontProvider
    )
)

val AppTypography = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = rethinkSansFamily),
        displayMedium = displayMedium.copy(fontFamily = rethinkSansFamily),
        displaySmall = displaySmall.copy(fontFamily = rethinkSansFamily),
        headlineLarge = headlineLarge.copy(fontFamily = rethinkSansFamily),
        headlineMedium = headlineMedium.copy(fontFamily = rethinkSansFamily),
        headlineSmall = headlineSmall.copy(fontFamily = rethinkSansFamily),
        titleLarge = titleLarge.copy(fontFamily = rethinkSansFamily),
        titleMedium = titleMedium.copy(fontFamily = rethinkSansFamily),
        titleSmall = titleSmall.copy(fontFamily = rethinkSansFamily),
        bodyLarge = bodyLarge.copy(fontFamily = rethinkSansFamily),
        bodyMedium = bodyMedium.copy(fontFamily = rethinkSansFamily),
        bodySmall = bodySmall.copy(fontFamily = rethinkSansFamily),
        labelLarge = labelLarge.copy(fontFamily = rethinkSansFamily),
        labelMedium = labelMedium.copy(fontFamily = rethinkSansFamily),
        labelSmall = labelSmall.copy(fontFamily = rethinkSansFamily)
    )
}

