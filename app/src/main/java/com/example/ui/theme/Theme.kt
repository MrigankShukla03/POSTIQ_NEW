package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val Slate850 = Color(0xFF1E293B)
val Slate800 = Color(0xFF334155)
val Slate700 = Color(0xFF475569)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)

val Purple500 = Color(0xFFA855F7)
val Purple400 = Color(0xFFC084FC)
val Purple300 = Color(0xFFD8B4FE)
val Purple900 = Color(0xFF581C87)

val Pink500 = Color(0xFFEC4899)
val Pink400 = Color(0xFFF472B6)

val Cyan500 = Color(0xFF06B6D4)
val Cyan400 = Color(0xFF22D3EE)

val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)

val Amber500 = Color(0xFFF59E0B)
val Amber400 = Color(0xFFFBBF24)

val Red500 = Color(0xFFEF4444)
val Red400 = Color(0xFFF87171)

private val DarkColorScheme = darkColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple900,
    onPrimaryContainer = Purple300,
    secondary = Pink500,
    onSecondary = Color.White,
    tertiary = Cyan500,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate300,
    outline = Slate800
)

@Composable
fun PostIQTheme(
    darkTheme: Boolean = true, // PostIQ defaults to dark cyber-aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
