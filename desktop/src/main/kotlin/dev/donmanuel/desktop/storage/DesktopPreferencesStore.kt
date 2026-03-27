package dev.donmanuel.desktop.storage

import dev.donmanuel.desktop.desktopTheme.ThemeMode
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * Preferencias de la app desktop en `~/.git-flow-cli-desktop/preferences.properties`.
 */
object DesktopPreferencesStore {

    private const val KEY_THEME = "theme"
    private const val KEY_CC_CANAL = "cc.canal"
    private const val KEY_CC_SUBCANAL = "cc.subcanal"
    private const val KEY_CC_EMPRESA = "cc.empresa"

    private val dir: Path
        get() = Path.of(System.getProperty("user.home")).resolve(".git-flow-cli-desktop")

    private val file: Path
        get() = dir.resolve("preferences.properties")

    fun loadTheme(): ThemeMode {
        if (file.notExists()) return ThemeMode.SYSTEM
        return try {
            val p = Properties()
            Files.newInputStream(file).use { p.load(it) }
            when ((p.getProperty(KEY_THEME) ?: "").uppercase()) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun saveTheme(mode: ThemeMode) {
        val p = loadAllProperties()
        p.setProperty(KEY_THEME, mode.name)
        saveProperties(p)
    }

    fun loadCcPrefs(): CcPrefs {
        if (file.notExists()) return CcPrefs.EMPTY
        return try {
            val p = Properties()
            Files.newInputStream(file).use { p.load(it) }
            CcPrefs(
                canal = p.getProperty(KEY_CC_CANAL),
                subcanal = p.getProperty(KEY_CC_SUBCANAL),
                empresa = p.getProperty(KEY_CC_EMPRESA),
            )
        } catch (_: Exception) {
            CcPrefs.EMPTY
        }
    }

    fun saveCcPrefs(prefs: CcPrefs) {
        val p = loadAllProperties()
        putOrRemove(p, KEY_CC_CANAL, prefs.canal)
        putOrRemove(p, KEY_CC_SUBCANAL, prefs.subcanal)
        putOrRemove(p, KEY_CC_EMPRESA, prefs.empresa)
        saveProperties(p)
    }

    private fun putOrRemove(props: Properties, key: String, value: String?) {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) {
            props.remove(key)
        } else {
            props.setProperty(key, v)
        }
    }

    private fun loadAllProperties(): Properties {
        val p = Properties()
        if (!file.notExists()) {
            try {
                Files.newInputStream(file).use { p.load(it) }
            } catch (_: Exception) {
            }
        }
        return p
    }

    private fun saveProperties(p: Properties) {
        try {
            dir.createDirectories()
            Files.writeString(
                file,
                buildString {
                    val sorted = p.entries.map { it.key.toString() to it.value.toString() }.sortedBy { it.first }
                    for ((k, v) in sorted) {
                        append(k)
                        append('=')
                        append(v.replace("\n", "\\n"))
                        append('\n')
                    }
                },
                StandardCharsets.UTF_8,
            )
        } catch (_: Exception) {
        }
    }

    data class CcPrefs(
        val canal: String? = null,
        val subcanal: String? = null,
        val empresa: String? = null,
    ) {
        companion object {
            val EMPTY = CcPrefs()
        }
    }
}
