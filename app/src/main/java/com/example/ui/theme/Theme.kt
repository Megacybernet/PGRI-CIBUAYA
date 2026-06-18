package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = FrostedPrimary,
    secondary = FrostedSecondary,
    tertiary = ActionChipBg,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8F9FF),
    onSurface = Color(0xFFF8F9FF)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FrostedPrimary,
    secondary = FrostedSecondary,
    tertiary = ActionChipBg,
    background = FrostedBackground,
    surface = FrostedSurfaceSolid,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = FrostedText,
    onSurface = FrostedText,
    surfaceVariant = FrostedBorderDark,
    onSurfaceVariant = Color(0xFF44474E)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
