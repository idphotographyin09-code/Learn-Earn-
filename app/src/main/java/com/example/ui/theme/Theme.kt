package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LearnEarnColorScheme = darkColorScheme(
  primary = PrimaryBlue,
  secondary = SecondaryGold,
  tertiary = SuccessGreen,
  background = DarkBackground,
  surface = CardBackground,
  onPrimary = White,
  onSecondary = DarkBackground,
  onTertiary = White,
  onBackground = White,
  onSurface = TextGray,
  error = ErrorRed,
  onError = White,
  outline = BorderColor,
  surfaceVariant = SurfaceDark,
  onSurfaceVariant = TextGray
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default as background is #0A0F1F
  dynamicColor: Boolean = false, // Use our brand identity colors instead of system colors
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LearnEarnColorScheme,
    typography = Typography,
    content = content
  )
}

