package com.markrogers.journal.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkPurple = darkColorScheme(primary = Color(0xFFB388FF), secondary = Color(0xFF7C4DFF), tertiary = Color(0xFF536DFE), surface = Color(0xFF0F0F13), background = Color(0xFF0B0B0F))
private val LightPurple = lightColorScheme(primary = Color(0xFF6A1B9A), secondary = Color(0xFF7B1FA2), tertiary = Color(0xFF512DA8), surface = Color(0xFFF7F2FA), background = Color(0xFFFCF8FF))

@Composable
fun JournalTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false, content: @Composable () -> Unit) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { val c = LocalContext.current; if (useDarkTheme) dynamicDarkColorScheme(c) else dynamicLightColorScheme(c) }
        useDarkTheme -> DarkPurple
        else -> LightPurple
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}
