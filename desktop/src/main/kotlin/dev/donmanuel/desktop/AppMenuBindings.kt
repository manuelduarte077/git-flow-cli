package dev.donmanuel.desktop

import dev.donmanuel.desktop.desktopTheme.ThemeMode

/**
 * Acciones enlazadas a la barra de menú y atajos; actualizadas desde [DesktopApp] con [LaunchedEffect].
 */
data class AppMenuBindings(
    val openProject: () -> Unit = {},
    val showAbout: () -> Unit = {},
    val showSettings: () -> Unit = {},
    val selectRama: (() -> Unit)? = null,
    val selectCc: (() -> Unit)? = null,
    val setTheme: (ThemeMode) -> Unit = {},
)
