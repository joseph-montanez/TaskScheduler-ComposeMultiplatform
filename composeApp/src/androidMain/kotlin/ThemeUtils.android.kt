import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
actual fun isSystemInDarkTheme(): Boolean = isSystemInDarkTheme()

actual fun observeSystemThemeChange(listener: ThemeChangeListener) {
// Android-specific logic to observe theme changes if available
// Since Compose does not provide an explicit way to listen for system theme changes,
// this might be a no-op or use Android Framework/AppCompat mechanisms if necessary.
}