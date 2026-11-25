package com.example.pencatatankeuangan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Skema warna HabisKapan. Aplikasi dirancang terang, jadi semua komponen
// bawaan (dialog, date picker, dropdown, bottom sheet, switch, dll.)
// mengikuti warna ini agar tampilannya seragam.
private val HabisKapanColorScheme = lightColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    primaryContainer = AppBlueSoft,
    onPrimaryContainer = AppBlue,

    secondary = AppBlue,
    onSecondary = Color.White,
    secondaryContainer = AppBlueSoft,
    onSecondaryContainer = AppBlue,

    tertiary = AppBlue,
    onTertiary = Color.White,

    background = AppPageBg,
    onBackground = AppNavy,

    surface = Color.White,
    onSurface = AppNavy,
    surfaceVariant = AppPageBg,
    onSurfaceVariant = AppMuted,
    surfaceTint = Color.Transparent,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = AppPageBg,

    outlineVariant = AppBorder,

    error = AppRed,
    onError = Color.White,
    errorContainer = AppRedBg,
    onErrorContainer = AppRed
)

private val HabisKapanShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun PencatatankeuanganTheme(
    // Dipertahankan agar pemanggil lama tetap valid. Tampilan selalu terang.
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HabisKapanColorScheme,
        typography = Typography,
        shapes = HabisKapanShapes,
        content = content
    )
}
