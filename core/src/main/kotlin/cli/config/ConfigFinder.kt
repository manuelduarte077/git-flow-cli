package dev.donmanuel.cli.config

import java.nio.file.Files
import java.nio.file.Path

object ConfigFinder {

    private val CONFIG_FILENAMES = listOf(
        BnConfig.FILE_NAME,
        ".git-bn-cli.toml",
    )

    fun findConfigFile(start: Path = Path.of("").toAbsolutePath()): Path? {
        var dir = start.normalize()
        if (Files.isRegularFile(dir)) {
            dir = dir.parent
        }
        while (true) {
            for (name in CONFIG_FILENAMES) {
                val candidate = dir.resolve(name)
                if (Files.isRegularFile(candidate)) {
                    return candidate
                }
            }
            if (Files.isDirectory(dir.resolve(".git"))) {
                return null
            }
            val parent = dir.parent ?: return null
            if (parent == dir) return null
            dir = parent
        }
    }

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
