package dev.donmanuel.cli.config

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

object ConfigFinder {

    /**
     * Sube desde [start] buscando [.git-bn-cli.toml] o un directorio [.git].
     */
    fun findConfigFile(start: Path = Path.of("").toAbsolutePath()): Path? {
        var dir = start.normalize()
        if (Files.isRegularFile(dir)) {
            dir = dir.parent
        }
        while (true) {
            val candidate = dir.resolve(BnConfig.FILE_NAME)
            if (Files.isRegularFile(candidate)) {
                return candidate
            }
            if (Files.isDirectory(dir.resolve(".git"))) {
                return null
            }
            val parent = dir.parent ?: return null
            if (parent == dir) return null
            dir = parent
        }
    }

    /**
     * Directorio raíz del repositorio git (donde está `.git`), o null.
     */
    fun findGitRoot(start: Path = Path.of("").toAbsolutePath()): Path? {
        var dir = start.normalize()
        if (Files.isRegularFile(dir)) {
            dir = dir.parent
        }
        while (true) {
            if (Files.isDirectory(dir.resolve(".git"))) {
                return dir
            }
            val parent = dir.parent ?: return null
            if (parent == dir) return null
            dir = parent
        }
    }
}
