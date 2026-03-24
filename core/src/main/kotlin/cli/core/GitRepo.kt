package dev.donmanuel.cli.core

import dev.donmanuel.cli.CliVersion
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object GitRepo {

    const val HOOK_MANAGED_MARKER = "git-flow-cli: managed hook"

    /** Marca en el hook post-checkout instalado por este CLI. */
    const val HOOK_POST_CHECKOUT_MARKER = "git-flow-cli: managed hook (post-checkout)"

    /**
     * Directorio de hooks según Git (`core.hooksPath`, worktrees, etc.).
     * Equivale a `git rev-parse --git-path hooks` desde la raíz del work tree.
     */
    fun resolveHooksDirectory(repoRoot: Path): Path {
        val raw = readGitStdout(repoRoot, "rev-parse", "--git-path", "hooks")?.trim().orEmpty()
        if (raw.isEmpty()) {
            return repoRoot.resolve(".git/hooks").normalize()
        }
        val p = Path.of(raw)
        return (if (p.isAbsolute) p else repoRoot.resolve(p)).normalize()
    }

    /**
     * Directorio Git real (`.git` o ruta en worktree).
     */
    fun gitDirectory(repoRoot: Path): Path {
        val raw = readGitStdout(repoRoot, "rev-parse", "--git-dir")?.trim()
            ?: error("git rev-parse --git-dir falló (¿no es un repositorio git?)")
        val p = Path.of(raw)
        return (if (p.isAbsolute) p else repoRoot.resolve(p)).normalize()
    }

    fun readGitStdout(repoRoot: Path, vararg args: String): String? {
        val cmd = ArrayList<String>(2 + args.size).apply {
            add("git")
            add("-C")
            add(repoRoot.absolutePathString())
            addAll(args)
        }
        val pb = ProcessBuilder(cmd)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
        val p = pb.start()
        val text = p.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }.trim()
        return if (p.waitFor() == 0) text else null
    }

    /**
     * Ruta al lanzador del CLI (p. ej. `bin/git-flow-cli`), nunca a la JVM.
     * Si se usara `java` directamente, el hook haría `exec java hooks verify …` y Java intentaría cargar la clase `hooks`.
     */
    fun resolveCliLauncherForHook(): String? {
        val cmd = ProcessHandle.current().info().command().orElse(null)?.trim()?.takeIf { it.isNotEmpty() }
            ?: return null
        if (!isJavaExecutable(cmd)) {
            return cmd
        }
        val jar = CliVersion.mainJarFileOrNull() ?: return null
        val libDir = jar.parentFile ?: return null
        val root = libDir.parentFile ?: return null
        val binDir = File(root, "bin")
        val unix = File(binDir, "git-flow-cli")
        if (unix.isFile) {
            return unix.absolutePath
        }
        val win = File(binDir, "git-flow-cli.bat")
        if (win.isFile) {
            return win.absolutePath
        }
        return null
    }

    private fun isJavaExecutable(path: String): Boolean {
        val name = File(path).name.lowercase()
        return name == "java" || name == "java.exe"
    }

    /** Escapa una cadena para usarla entre comillas simples en `sh`. */
    fun shellSingleQuoted(s: String): String = "'" + s.replace("'", "'\\''") + "'"
}
