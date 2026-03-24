package dev.donmanuel.desktop.storage

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * Historial de proyectos Git abiertos (rutas absolutas normalizadas).
 * Persistencia: `~/.git-flow-cli-desktop/recent-projects.txt` (una ruta por línea).
 */
object ProjectHistoryStore {

    private const val MAX = 15
    private val storeFile: Path
        get() {
            val home = Path.of(System.getProperty("user.home"))
            return home.resolve(".git-flow-cli-desktop").resolve("recent-projects.txt")
        }

    fun load(): List<Path> {
        val f = storeFile
        if (f.notExists()) return emptyList()
        return try {
            Files.readAllLines(f, StandardCharsets.UTF_8)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { Path.of(it).toAbsolutePath().normalize() }
                .distinct()
                .filter { Files.isDirectory(it.resolve(".git")) }
                .take(MAX)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(root: Path) {
        val normalized = root.toAbsolutePath().normalize()
        val merged = listOf(normalized) + load().filter { it != normalized }
        val unique = merged.distinct().take(MAX)
        try {
            storeFile.parent.createDirectories()
            Files.writeString(
                storeFile,
                unique.joinToString("\n") { it.toAbsolutePath().normalize().toString() } + "\n",
                StandardCharsets.UTF_8,
            )
        } catch (_: Exception) {
        }
    }

    fun remove(root: Path) {
        val n = root.toAbsolutePath().normalize()
        val rest = load().filter { it != n }
        try {
            if (rest.isEmpty()) {
                Files.deleteIfExists(storeFile)
            } else {
                Files.writeString(
                    storeFile,
                    rest.joinToString("\n") { it.toAbsolutePath().normalize().toString() } + "\n",
                    StandardCharsets.UTF_8,
                )
            }
        } catch (_: Exception) {
        }
    }
}
