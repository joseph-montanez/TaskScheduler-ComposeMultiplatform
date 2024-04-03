expect fun isSystemInDarkTheme(): Boolean

interface ThemeChangeListener {
    fun onThemeChanged(isDarkTheme: Boolean)
}

expect fun observeSystemThemeChange(listener: ThemeChangeListener)