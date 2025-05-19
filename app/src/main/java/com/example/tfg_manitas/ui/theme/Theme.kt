package com.example.tfg_manitas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🎨 PALETA PERSONALIZADA (basada en logo Manitas)
val MangoAmarillo = Color(0xFFF4A950)
val RojoTierra = Color(0xFFA93F1F)
val AzulGrafito = Color(0xFF2F4C5A)
val BeigePiel = Color(0xFFF9C89B)
val MarronOscuro = Color(0xFF4E2C1E)

// 🟢 Colores adicionales (accesibilidad y consistencia)
val VerdeExito = Color(0xFF10B981)
val RojoError = Color(0xFFEF4444)
val Blanco = Color(0xFFFFFFFF)
val GrisClaro = Color(0xFFE5E7EB)
val GrisOscuro = Color(0xFF4B5563)

private val LightColorScheme = lightColorScheme(
    primary = AzulGrafito,
    secondary = MangoAmarillo,
    background = BeigePiel,
    surface = Color.White,
    error = RojoTierra,
    onPrimary = Color.White,
    onSecondary = MarronOscuro,
    onBackground = MarronOscuro,
    onSurface = MarronOscuro,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = MangoAmarillo,
    secondary = RojoTierra,
    background = MarronOscuro,
    surface = AzulGrafito,
    error = Color.Red,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = BeigePiel,
    onSurface = BeigePiel,
    onError = Color.Black
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
