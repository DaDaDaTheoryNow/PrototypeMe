package com.dadadadev.prototype_me.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Monochrome palette ────────────────────────────────────────────────────────
// Mirrors a pen-on-paper / sketch aesthetic: no hue, only value contrast.

val Black        = Color(0xFF111111)
val DarkGrey     = Color(0xFF3A3A3A)
val MidGrey      = Color(0xFF7A7A7A)
val LightGrey    = Color(0xFFD0D0D0)
val SurfaceWhite = Color(0xFFFFFFFF)
val CanvasBg     = Color(0xFFF2F2F2)   // off-white board background
val DotGrid      = Color(0xFFCCCCCC)   // subtle dot grid dots

private val MonoColorScheme = lightColorScheme(
    primary                = Black,
    onPrimary              = SurfaceWhite,
    primaryContainer       = Color(0xFF2A2A2A),
    onPrimaryContainer     = SurfaceWhite,

    secondary              = DarkGrey,
    onSecondary            = SurfaceWhite,
    secondaryContainer     = Color(0xFFE8E8E8),
    onSecondaryContainer   = Black,

    tertiary               = MidGrey,
    onTertiary             = SurfaceWhite,
    tertiaryContainer      = Color(0xFFF0F0F0),
    onTertiaryContainer    = Black,

    // "error" tokens are reused for locked-node styling (grey, not red)
    error                  = DarkGrey,
    onError                = SurfaceWhite,
    errorContainer         = Color(0xFFE0E0E0),
    onErrorContainer       = Black,

    background             = CanvasBg,
    onBackground           = Black,

    surface                = SurfaceWhite,
    onSurface              = Black,
    surfaceVariant         = Color(0xFFEAEAEA),
    onSurfaceVariant       = DarkGrey,

    outline                = LightGrey,
    outlineVariant         = Color(0xFFE8E8E8),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonoColorScheme,
        content = content
    )
}
