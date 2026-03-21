package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.core.CommitMessageValidator
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.system.exitProcess

class HooksCommand : CliktCommand(
    name = "hooks",
    help = "Instalar o verificar el hook commit-msg del repositorio.",
) {
    init {
        subcommands(HooksInstallCommand(), HooksVerifyCommand())
    }

    override fun run() = Unit
}

class HooksInstallCommand : CliktCommand(
    name = "install",
    help = "Genera .git/hooks/commit-msg para validar el formato pipe en cada commit.",
) {
    override fun run() {
        val root = ConfigFinder.findGitRoot()
            ?: throw UsageError("No se encontró un repositorio git (directorio .git).")
        val hookPath = root.resolve(".git/hooks/commit-msg")
        hookPath.parent.createDirectories()
        val script =
            """
            #!/bin/sh
            set -e
            if ! command -v git-bn-cli >/dev/null 2>&1; then
              echo "git-bn-cli: ejecutable no encontrado en PATH" >&2
              exit 1
            fi
            exec git-bn-cli hooks verify --file "${'$'}1"
            """.trimIndent() + "\n"
        hookPath.writeText(script)
        val f = hookPath.toFile()
        if (!f.setExecutable(true, false)) {
            echo("Advertencia: no se pudo marcar el hook como ejecutable; ejecuta: chmod +x ${hookPath.absolutePathString()}")
        }
        echo("Hook instalado: ${hookPath.absolutePathString()}")
    }
}

class HooksVerifyCommand : CliktCommand(
    name = "verify",
    help = "Valida el mensaje de commit en un archivo (uso interno del hook).",
) {
    private val file by option("--file", "-f", help = "Ruta al archivo con el mensaje").required()

    override fun run() {
        val text = Files.readString(Path(file))
        val firstLine = text.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && !it.startsWith("#") }
            ?: ""

        when (val v = CommitMessageValidator.validate(firstLine)) {
            CommitMessageValidator.ValidationResult.Ok -> exitProcess(0)
            CommitMessageValidator.ValidationResult.Skipped -> exitProcess(0)
            is CommitMessageValidator.ValidationResult.Invalid -> {
                System.err.println("git-bn-cli: ${v.reason}")
                System.err.println("Formato esperado: canal|subcanal|empresa|ticket| descripción")
                exitProcess(1)
            }
        }
    }
}
