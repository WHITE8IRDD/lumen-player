package com.lumen.player.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.lumen.player.R
import com.lumen.player.core.theme.InstrumentSerifFamily
import com.lumen.player.core.theme.InterFamily

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

enum class LumenFontChoice(val displayName: String, val family: FontFamily) {
    SERIF_DISPLAY("Serif Display", InstrumentSerifFamily),
    MODERN_SANS("Modern Sans", InterFamily),
    GEOMETRIC("Geometric", FontFamily(
        Font(GoogleFont("Outfit"), googleFontProvider, FontWeight.Light),
        Font(GoogleFont("Outfit"), googleFontProvider, FontWeight.Normal),
        Font(GoogleFont("Outfit"), googleFontProvider, FontWeight.Medium),
        Font(GoogleFont("Outfit"), googleFontProvider, FontWeight.SemiBold),
        Font(GoogleFont("Outfit"), googleFontProvider, FontWeight.Bold)
    )),
    HUMANIST("Humanist", FontFamily(
        Font(GoogleFont("Nunito"), googleFontProvider, FontWeight.Light),
        Font(GoogleFont("Nunito"), googleFontProvider, FontWeight.Normal),
        Font(GoogleFont("Nunito"), googleFontProvider, FontWeight.Medium),
        Font(GoogleFont("Nunito"), googleFontProvider, FontWeight.SemiBold),
        Font(GoogleFont("Nunito"), googleFontProvider, FontWeight.Bold)
    )),
    HELVETICA("Helvetica", FontFamily(
        Font(GoogleFont("Nunito Sans"), googleFontProvider, FontWeight.Light),
        Font(GoogleFont("Nunito Sans"), googleFontProvider, FontWeight.Normal),
        Font(GoogleFont("Nunito Sans"), googleFontProvider, FontWeight.Medium),
        Font(GoogleFont("Nunito Sans"), googleFontProvider, FontWeight.SemiBold),
        Font(GoogleFont("Nunito Sans"), googleFontProvider, FontWeight.Bold)
    )),
    MONO_ACCENT("Mono Accent", FontFamily(
        Font(GoogleFont("JetBrains Mono"), googleFontProvider, FontWeight.Light),
        Font(GoogleFont("JetBrains Mono"), googleFontProvider, FontWeight.Normal),
        Font(GoogleFont("JetBrains Mono"), googleFontProvider, FontWeight.Medium),
        Font(GoogleFont("JetBrains Mono"), googleFontProvider, FontWeight.SemiBold),
        Font(GoogleFont("JetBrains Mono"), googleFontProvider, FontWeight.Bold)
    ))
}
