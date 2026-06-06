package com.brandforge.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ForgeColor {
    val Black = Color(0xFF050506)
    val Panel = Color(0xFF090A0C)
    val PanelRaised = Color(0xFF101214)
    val Grid = Color(0xFF252A2D)
    val White = Color(0xFFF7F7EE)
    val Muted = Color(0xFF9BA3A6)
    val Yellow = Color(0xFFFFD84A)
    val Green = Color(0xFF54F28B)
    val Red = Color(0xFFFF5A5F)
    val Blue = Color(0xFF61D5FF)
}

private val BrandForgeColors = darkColorScheme(
    primary = ForgeColor.Yellow,
    onPrimary = ForgeColor.Black,
    secondary = ForgeColor.Green,
    onSecondary = ForgeColor.Black,
    tertiary = ForgeColor.Blue,
    background = ForgeColor.Black,
    onBackground = ForgeColor.White,
    surface = ForgeColor.Panel,
    onSurface = ForgeColor.White,
    surfaceVariant = ForgeColor.PanelRaised,
    onSurfaceVariant = ForgeColor.Muted,
    error = ForgeColor.Red,
    onError = ForgeColor.Black,
    outline = ForgeColor.White,
)

private val BrandForgeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
    ),
)

@Composable
fun BrandForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrandForgeColors,
        typography = BrandForgeTypography,
        content = content,
    )
}
