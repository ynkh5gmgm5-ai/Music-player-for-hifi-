package com.yuandao.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val YuandaoBlue = Color(0xFF5B86FF)
val YuandaoGreen = Color(0xFF58E07A)
val YuandaoCyan = Color(0xFF37D6C2)
val YuandaoOrange = Color(0xFFFFA947)
val YuandaoBackground = Color(0xFF05070A)
val YuandaoSurface = Color(0xFF14171D)
val YuandaoSurfaceHigh = Color(0xFF20242C)

private val DarkScheme = darkColorScheme(
    primary = YuandaoBlue,
    secondary = YuandaoGreen,
    tertiary = YuandaoCyan,
    background = YuandaoBackground,
    surface = YuandaoSurface,
    surfaceVariant = YuandaoSurfaceHigh,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

object YuandaoThemePolicy {
    val rootBackgroundColor: Color = YuandaoBackground
    val rootContentColor: Color = Color.White
}

@Composable
fun YuandaoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = MaterialTheme.typography,
    ) {
        Surface(
            color = YuandaoThemePolicy.rootBackgroundColor,
            contentColor = YuandaoThemePolicy.rootContentColor,
            content = content,
        )
    }
}
