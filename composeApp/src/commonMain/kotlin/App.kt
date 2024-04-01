import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import screens.TaskManagerScreen

import androidx.compose.runtime.staticCompositionLocalOf


val LocalFileChooser = staticCompositionLocalOf<FileChooser?> { null }

private val DarkColorPalette = darkColors(
    primary = Color(0xFF00ff76),
    primaryVariant = Color(0xFF00cb65),
    secondary = Color(0xFF6200EE),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)


private val LightColorPalette = lightColors(
    primary = Color(0xFF00ff76),
    primaryVariant = Color(0xFF00cb65),
    secondary = Color(0xFF6200EE),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Black
)


@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors: Colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
@Preview
fun App(fileChooser: FileChooser?) {
    CompositionLocalProvider(LocalFileChooser provides fileChooser) {
        MyAppTheme {
            Navigator(TaskManagerScreen())
        }
    }
}
