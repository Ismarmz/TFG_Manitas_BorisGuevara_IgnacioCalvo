package com.example.tfg_manitas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// PALETA PERSONALIZADA
val AzulOscuro = Color(0xFF1E3A8A)
val AzulClaro = Color(0xFF93C5FD)
val Blanco = Color(0xFFFFFFFF)
val GrisClaro = Color(0xFFE5E7EB)
val GrisOscuro = Color(0xFF4B5563)
val VerdeExito = Color(0xFF10B981)
val RojoError = Color(0xFFEF4444)

private val LightColorScheme = lightColorScheme(
    primary = AzulOscuro,
    secondary = AzulClaro,
    background = Blanco,
    surface = Blanco,
    error = RojoError,
    onPrimary = Blanco,
    onSecondary = GrisOscuro,
    onBackground = GrisOscuro,
    onSurface = GrisOscuro,
    onError = Blanco
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    secondary = AzulOscuro,
    background = GrisOscuro,
    surface = GrisOscuro,
    error = RojoError,
    onPrimary = GrisClaro,
    onSecondary = Blanco,
    onBackground = Blanco,
    onSurface = Blanco,
    onError = Blanco
)

@Composable
fun TFG_ManitasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
