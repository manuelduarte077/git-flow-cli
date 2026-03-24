package dev.donmanuel.desktop

/**
 * Callbacks registrados desde [DesktopApp] para el [androidx.compose.ui.window.MenuBar] en [main].
 */
class DesktopMenuCallbacks {
    var openProject: () -> Unit = {}
    var showAbout: () -> Unit = {}
}
