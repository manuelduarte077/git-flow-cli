package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.BnDefaults
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.core.CommitMessageValidator
import dev.donmanuel.cli.core.GitService
import dev.donmanuel.cli.promptNonEmptyLine

class CcCommand : CliktCommand(
    name = "cc",
    help = "Commit con formato pipe. Por defecto canal y empresa son canales_digitales y NOVACOMP; se pueden sobrescribir en .git-flow-cli.toml.",
) {

    private val ticket by option("-t", "--ticket", help = "Ticket (ej. \"BUG 886814\", \"HU-116268\")")

    private val descripcion by option("-m", "--descripcion", help = "Texto descriptivo del cambio")

    private val subcanal by option("--subcanal", help = "Subcanal (por defecto desde .git-flow-cli.toml)")

    private val printOnly by option("--print", "-p", help = "Solo imprime el mensaje; no ejecuta git commit")
        .flag(default = false)

    override fun run() {
        val cfgPath = ConfigFinder.findConfigFile()
        val cfg = cfgPath?.let { path ->
            try {
                BnConfig.load(path)
            } catch (e: IllegalStateException) {
                throw UsageError(e.message ?: "Configuración inválida en $path")
            } catch (e: Exception) {
                throw UsageError("No se pudo cargar la configuración en $path: ${e.message ?: e.javaClass.simpleName}")
            }
        }

        var t = ticket
        var d = descripcion
        val ccHint = ccNonInteractiveHint()
        if (t == null || d == null) {
            echo("Modo interactivo. Pulsa Enter tras cada valor.")
            if (t == null) {
                t = promptNonEmptyLine("Ticket (ej. BUG 886814, HU-116268): ", ccHint)
            }
            if (d == null) {
                d = promptNonEmptyLine("Descripción del cambio: ", ccHint)
            }
        }

        val c = cfg?.canal ?: BnDefaults.CANAL_COMMIT
        val emp = cfg?.empresa ?: BnDefaults.EMPRESA
        var sc = subcanal ?: cfg?.subcanal
        if (sc == null) {
            sc = promptNonEmptyLine(
                "Subcanal (ej. canales_2): ",
                ccHint,
            )
        }

        val line = "$c|$sc|$emp|$t| ${d.trim()}"

        when (val v = CommitMessageValidator.validate(line)) {
            is CommitMessageValidator.ValidationResult.Invalid -> {
                val msg =
                    if (v.reason.contains(CommitMessageValidator.FORMAT_EXPECTED_HINT)) {
                        v.reason
                    } else {
                        "${v.reason} Formato: ${CommitMessageValidator.FORMAT_EXPECTED_HINT}"
                    }
                throw UsageError(msg)
            }

            CommitMessageValidator.ValidationResult.Skipped -> {}
            CommitMessageValidator.ValidationResult.Ok -> {}
        }

        echo("")
        echo("El mensaje de commit será:")
        echo(line)
        echo("")

        if (printOnly) {
            return
        }

        val gs = GitService()
        if (!gs.isInsideGitWorkTree()) {
            throw UsageError("No estás dentro de un repositorio Git.")
        }
        if (!gs.hasStagedChanges()) {
            throw UsageError(
                "No hay cambios en staging. Ejecuta git add (o git add .) antes de git-flow-cli cc.",
            )
        }

        try {
            gs.commit(line)
        } catch (e: IllegalStateException) {
            throw UsageError(e.message ?: "git commit falló")
        }
        echo("Commit creado.")
    }

    private fun ccNonInteractiveHint() =
        "Pasa -t/--ticket y -m/--descripcion (y --subcanal si hace falta), o ejecuta el binario " +
                "tras ./gradlew installDist: build/install/git-flow-cli/bin/git-flow-cli cc …"
}
