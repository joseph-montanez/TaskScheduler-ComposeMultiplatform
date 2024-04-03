import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
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
    primary = Color(0xFF4CAF50),
    primaryVariant = Color(0xFF388E3C),
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
fun App(fileChooser: FileChooser?) {
    var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme()) }

    DisposableEffect(Unit) {
        val themeChangeListener = object : ThemeChangeListener {
            override fun onThemeChanged(newIsDarkTheme: Boolean) {
                isDarkTheme = newIsDarkTheme
            }
        }

        observeSystemThemeChange(themeChangeListener)

        onDispose { }
    }
    CompositionLocalProvider(LocalFileChooser provides fileChooser) {
        MyAppTheme(darkTheme = isDarkTheme) {
            Navigator(TaskManagerScreen())
        }
    }
}
