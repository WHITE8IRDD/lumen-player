package com.lumen.player.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lumen.player.R

private val weightSteps = listOf(
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold
)

fun adjustedWeight(base: FontWeight, offset: Int): FontWeight {
    val baseIdx = weightSteps.indexOf(base).takeIf { it >= 0 } ?: 1
    val newIdx = (baseIdx + offset).coerceIn(0, weightSteps.size - 1)
    return weightSteps[newIdx]
}

val InstrumentSerifFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.instrument_serif_regular, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.instrument_serif_italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic)
)

val InterFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.inter_variable)
)

fun buildLumenTypography(
    family: FontFamily,
    weightOffset: Int = 0
): Typography {
    val isDefaultSerif = (family == InstrumentSerifFamily)

    if (isDefaultSerif && weightOffset == 0) {
        return Typography(
            displayLarge = TextStyle(
                fontFamily = InstrumentSerifFamily, fontWeight = FontWeight.Normal,
                fontSize = 52.sp, lineHeight = 60.sp, letterSpacing = (-1).sp
            ),
            displayMedium = TextStyle(
                fontFamily = InstrumentSerifFamily, fontWeight = FontWeight.Normal,
                fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp
            ),
            displaySmall = TextStyle(
                fontFamily = InstrumentSerifFamily, fontWeight = FontWeight.Normal,
                fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = InstrumentSerifFamily, fontWeight = FontWeight.Normal,
                fontSize = 32.sp, lineHeight = 40.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = InstrumentSerifFamily, fontWeight = FontWeight.Normal,
                fontSize = 26.sp, lineHeight = 34.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = InstrumentSerifFamily, fontWeight = FontWeight.Normal,
                fontSize = 22.sp, lineHeight = 30.sp
            ),
            titleLarge = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp, lineHeight = 24.sp
            ),
            titleMedium = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp, lineHeight = 22.sp
            ),
            titleSmall = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.Medium,
                fontSize = 14.sp, lineHeight = 20.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.Normal,
                fontSize = 16.sp, lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.Normal,
                fontSize = 14.sp, lineHeight = 20.sp
            ),
            bodySmall = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.Normal,
                fontSize = 12.sp, lineHeight = 16.sp
            ),
            labelLarge = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.Medium,
                fontSize = 12.sp, letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, letterSpacing = 2.sp
            )
        )
    }

    fun adj(base: FontWeight) = adjustedWeight(base, weightOffset)
    return Typography(
        displayLarge = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 52.sp, lineHeight = 60.sp, letterSpacing = (-1).sp),
        displayMedium = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp),
        displaySmall = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp),
        headlineLarge = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 26.sp, lineHeight = 34.sp),
        headlineSmall = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 22.sp, lineHeight = 30.sp),
        titleLarge = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.SemiBold), fontSize = 18.sp, lineHeight = 24.sp),
        titleMedium = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.SemiBold), fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Medium), fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Normal), fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.SemiBold), fontSize = 14.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.Medium), fontSize = 12.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = family, fontWeight = adj(FontWeight.SemiBold), fontSize = 11.sp, letterSpacing = 2.sp)
    )
}
