package io.github.raghavg02.fosspulse.ui.theme

import io.github.raghavg02.fosspulse.platform.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

object ThemeManager {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun init(secureStorage: SecureStorage) {
        val saved = secureStorage.get("app_theme_mode")
        _themeMode.value = when (saved?.uppercase()) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode, secureStorage: SecureStorage) {
        _themeMode.value = mode
        secureStorage.save("app_theme_mode", mode.name)
    }
}
