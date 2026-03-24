package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.core.CommitMessageValidator
import dev.donmanuel.cli.core.GitRepo
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.*
import kotlin.system.exitProcess

class HooksCommand : CliktCommand(
    name = "hooks",
    help = "Instalar o verificar hooks (commit-msg; opcional post-checkout para validar nombres de rama BN).",
) {
    init {
        subcommands(HooksInstallCommand(), HooksVerifyCommand())
    }

    override fun run() = Unit
}

class HooksInstallCommand : CliktCommand(
    name = "install",
    help = "Genera commit-msg en el directorio de hooks de Git (respeta core.hooksPath y worktrees).",
) {
    private val force by option(
        "--force",
        "-f",
        help = "Sobrescribe un commit-msg ajeno sin copia de seguridad.",
    ).flag(default = false)

    private val resolveBinary by option(
        "--resolve-binary",
        help = "Escribe en el script la ruta absoluta del lanzador bin/git-flow-cli (no la JVM), útil si Git/IDE no tienen git-flow-cli en PATH.",
    ).flag(default = false)

    private val branchHook by option(
        "--branch-hook",
        help = "Instala también post-checkout para validar nombres BN al crear/cambiar de rama (git checkout -b, etc.).",
    ).flag(default = false)

    override fun run() {
        val root = ConfigFinder.findGitRoot()
            ?: throw UsageError("No se encontró un repositorio git (directorio .git).")
        val hooksDir = GitRepo.resolveHooksDirectory(root)
        hooksDir.createDirectories()

        val defaultHooks = root.resolve(".git/hooks").normalize()
        if (hooksDir.normalize() != defaultHooks) {
            echo("Directorio de hooks (git rev-parse --git-path hooks): ${hooksDir.absolutePathString()}")
        }

        val hookPath = hooksDir.resolve("commit-msg")
        if (hookPath.isRegularFile()) {
            val content = hookPath.readText()
            val managed = content.contains(GitRepo.HOOK_MANAGED_MARKER)
            if (!managed) {
                if (force) {
                    echo("Sobrescribiendo commit-msg existente (--force).")
                } else {
                    val bak = uniqueBackupPath(hookPath)
                    Files.move(hookPath, bak, StandardCopyOption.REPLACE_EXISTING)
                    echo("El commit-msg anterior se guardó en: ${bak.absolutePathString()}")
                }
            }
        }

        val resolved = if (resolveBinary) {
            GitRepo.resolveCliLauncherForHook().also {
                if (it == null) {
                    echo(
                        "Advertencia: no se pudo resolver el script bin/git-flow-cli (p. ej. sin JAR en ./gradlew run). " +
                                "El hook usará git-flow-cli del PATH.",
                    )
                }
            }
        } else {
            null
        }

        hookPath.writeText(buildHookShellScript(resolved))
        setExecutableOrWarn(hookPath)

        if (branchHook) {
            val pcPath = hooksDir.resolve("post-checkout")
            if (pcPath.isRegularFile()) {
                val content = pcPath.readText()
                val managed = content.contains(GitRepo.HOOK_POST_CHECKOUT_MARKER)
                if (!managed) {
                    if (force) {
                        echo("Sobrescribiendo post-checkout existente (--force).")
                    } else {
                        val bak = uniqueBackupPath(pcPath)
                        Files.move(pcPath, bak, StandardCopyOption.REPLACE_EXISTING)
                        echo("El post-checkout anterior se guardó en: ${bak.absolutePathString()}")
                    }
                }
            }
            pcPath.writeText(buildPostCheckoutHookShellScript(resolved))
            setExecutableOrWarn(pcPath)
            echo("Hook post-checkout instalado: ${pcPath.absolutePathString()}")
        }

        echo("Hook commit-msg instalado: ${hookPath.absolutePathString()}")
    }

    private fun setExecutableOrWarn(hookPath: Path) {
        val f = File(hookPath.toString())
        if (!f.setExecutable(true, false)) {
            echo("Advertencia: no se pudo marcar el hook como ejecutable; ejecuta: chmod +x ${hookPath.absolutePathString()}")
        }
    }

    private fun uniqueBackupPath(hookPath: Path): Path {
        val bakName = hookPath.fileName.toString() + ".bak"
        val sibling = hookPath.resolveSibling(bakName)
        return if (!Files.exists(sibling)) {
            sibling
        } else {
            hookPath.resolveSibling("$bakName.${System.currentTimeMillis()}")
        }
    }

    private fun buildHookShellScript(resolvedBinary: String?): String {
        val body = if (resolvedBinary != null) {
            val q = GitRepo.shellSingleQuoted(resolvedBinary)
            """
            CLI_BIN=$q
            if [ ! -x "${'$'}CLI_BIN" ] && [ ! -f "${'$'}CLI_BIN" ]; then
              echo "git-flow-cli: ejecutable no encontrado o no legible: ${'$'}CLI_BIN" >&2
              exit 1
            fi
            if [ -z "${'$'}1" ]; then
              echo "git-flow-cli: commit-msg sin ruta de archivo de mensaje" >&2
              exit 1
            fi
            exec "${'$'}CLI_BIN" hooks verify --file "${'$'}1"
            """.trimIndent()
        } else {
            """
            if ! command -v git-flow-cli >/dev/null 2>&1; then
              echo "git-flow-cli: ejecutable no encontrado en PATH" >&2
              exit 1
            fi
            if [ -z "${'$'}1" ]; then
              echo "git-flow-cli: commit-msg sin ruta de archivo de mensaje" >&2
              exit 1
            fi
            exec git-flow-cli hooks verify --file "${'$'}1"
            """.trimIndent()
        }
        return buildString {
            appendLine("#!/bin/sh")
            appendLine("# ${GitRepo.HOOK_MANAGED_MARKER}")
            appendLine("set -e")
            appendLine(body)
            appendLine()
        }
    }

    private fun buildPostCheckoutHookShellScript(resolvedBinary: String?): String {
        val body = if (resolvedBinary != null) {
            val q = GitRepo.shellSingleQuoted(resolvedBinary)
            """
            CLI_BIN=$q
            if [ ! -x "${'$'}CLI_BIN" ] && [ ! -f "${'$'}CLI_BIN" ]; then
              exit 0
            fi
            exec "${'$'}CLI_BIN" rama verify -q
            """.trimIndent()
        } else {
            """
            if ! command -v git-flow-cli >/dev/null 2>&1; then
              exit 0
            fi
            exec git-flow-cli rama verify -q
            """.trimIndent()
        }
        return buildString {
            appendLine("#!/bin/sh")
            appendLine("# ${GitRepo.HOOK_POST_CHECKOUT_MARKER}")
            appendLine("set -e")
            appendLine("if [ \"${'$'}3\" != 1 ]; then")
            appendLine("  exit 0")
            appendLine("fi")
            appendLine(body)
            appendLine()
        }
    }
}

class HooksVerifyCommand : CliktCommand(
    name = "verify",
    help = "Valida el mensaje de commit en un archivo (uso interno del hook).",
) {
    private val file by option("--file", "-f", help = "Ruta al archivo con el mensaje").required()

    override fun run() {
        if (file.isBlank()) {
            throw UsageError("Falta la ruta al archivo de mensaje (--file).")
        }
        val path = Path.of(file).toAbsolutePath().normalize()
        when {
            !Files.exists(path) ->
                throw UsageError("No existe el archivo: ${path.absolutePathString()}")

            Files.isDirectory(path) ->
                throw UsageError("La ruta es un directorio; se esperaba un archivo de mensaje: ${path.absolutePathString()}")

            !Files.isRegularFile(path) ->
                throw UsageError("No es un archivo regular: ${path.absolutePathString()}")
        }

        val gitRoot = ConfigFinder.findGitRoot(path)
            ?: throw UsageError("No se encontró un repositorio git que contenga el archivo.")
        val gitDir = try {
            GitRepo.gitDirectory(gitRoot).toRealPath()
        } catch (e: IllegalStateException) {
            throw UsageError(e.message ?: "No se pudo determinar el directorio .git.")
        }
        val realFile = path.toRealPath()
        if (!(realFile.startsWith(gitDir))) {
            throw UsageError(
                "Solo se validan archivos bajo el directorio Git del repositorio (${gitDir.absolutePathString()}). " +
                        "Recibido: ${realFile.absolutePathString()}",
            )
        }

        val text = Files.readString(path)
        when (val v = CommitMessageValidator.validateMessageText(text)) {
            CommitMessageValidator.ValidationResult.Ok -> exitProcess(0)
            CommitMessageValidator.ValidationResult.Skipped -> exitProcess(0)
            is CommitMessageValidator.ValidationResult.Invalid -> {
                System.err.println("git-flow-cli: ${v.reason}")
                System.err.println("Formato esperado: ${CommitMessageValidator.FORMAT_EXPECTED_HINT}")
                exitProcess(1)
            }
        }
    }
}
