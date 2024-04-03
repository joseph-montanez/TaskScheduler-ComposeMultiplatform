import com.jthemedetecor.OsThemeDetector

actual fun isSystemInDarkTheme(): Boolean = OsThemeDetector.getDetector().isDark

actual fun observeSystemThemeChange(listener: ThemeChangeListener) {
    val detector = OsThemeDetector.getDetector()
    detector.registerListener { isDark ->
        listener.onThemeChanged(isDark)
    }
}