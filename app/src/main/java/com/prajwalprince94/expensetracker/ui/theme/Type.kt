package com.prajwalprince94.expensetracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.R

// Genuine Poppins Font Family bundled in res/font for 100% offline rendering
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun poppinsStyle(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Double = 0.0
) = TextStyle(
    fontFamily = Poppins,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
    lineHeightStyle = Trim
)

/**
 * Expressive typography with Poppins font family inspired by NikhilKain/Range.
 * Display sizes are tightened so financial figures read like bold headlines.
 */
val Typography = Typography(
    displayLarge = poppinsStyle(54, 58, FontWeight.ExtraBold, -1.4),
    displayMedium = poppinsStyle(40, 46, FontWeight.ExtraBold, -1.0),
    displaySmall = poppinsStyle(32, 38, FontWeight.Bold, -0.6),
    headlineLarge = poppinsStyle(28, 34, FontWeight.Bold, -0.4),
    headlineMedium = poppinsStyle(24, 30, FontWeight.Bold, -0.3),
    headlineSmall = poppinsStyle(20, 26, FontWeight.SemiBold, -0.2),
    titleLarge = poppinsStyle(18, 24, FontWeight.Bold, -0.1),
    titleMedium = poppinsStyle(15, 22, FontWeight.SemiBold, 0.1),
    titleSmall = poppinsStyle(13, 18, FontWeight.SemiBold, 0.1),
    bodyLarge = poppinsStyle(15, 22, FontWeight.Normal, 0.15),
    bodyMedium = poppinsStyle(13, 19, FontWeight.Normal, 0.2),
    bodySmall = poppinsStyle(11, 15, FontWeight.Normal, 0.25),
    labelLarge = poppinsStyle(13, 18, FontWeight.Bold, 0.2),
    labelMedium = poppinsStyle(11, 15, FontWeight.SemiBold, 0.4),
    labelSmall = poppinsStyle(10, 13, FontWeight.SemiBold, 0.5)
)
