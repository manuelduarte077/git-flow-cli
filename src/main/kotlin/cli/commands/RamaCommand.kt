package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.donmanuel.cli.CliMessages
import dev.donmanuel.cli.config.BnDefaults
import dev.donmanuel.cli.core.BranchNameBuilder
import dev.donmanuel.cli.core.BranchNameValidator
import dev.donmanuel.cli.core.GitService
import dev.donmanuel.cli.promptNonEmptyLine
import kotlin.system.exitProcess

class RamaCommand : CliktCommand(
    name = "rama",
    help = "Crea y cambia a una rama con el formato BN (feature/hotfix/release). Sin flags, modo interactivo. " +
            "Usa `rama verify` para comprobar el nombre de una rama (p. ej. tras git checkout -b).",
    invokeWithoutSubcommand = true,
) {
    init {
        subcommands(RamaVerifyCommand())
    }

    private val tipo by option("-t", "--tipo", help = "feature, hotfix o release")

    private val app by option("-a", "--app", help = "Siglas aplicación (ej. BNMP)")

    private val sprint by option("-s", "--sprint", help = "Versión sprint (ej. V58-Sprint22.05)")

    private val area by option("--area", help = "Requerido para feature/hotfix (ej. DCSTI)")

    private val hu by option("--hu", help = "Referencia HU/ticket (ej. HU-116268)")

    override fun run() {
        val gs = GitService()
        if (!gs.isInsideGitWorkTree()) {
            throw UsageError(CliMessages.NOT_IN_GIT_REPO)
        }

        var tipoStr = tipo
        var appStr = app
        var sprintStr = sprint
        var areaStr = area
        var huStr = hu

        if (tipoStr == null || appStr == null || sprintStr == null) {
            echo("Modo interactivo. Pulsa Enter tras cada valor.")
            if (tipoStr == null) {
                tipoStr = promptNonEmptyLine(
                    "Tipo de rama (feature / hotfix / release): ",
                    ramaNonInteractiveHint(),
                )
            }
            if (appStr == null) {
                appStr = promptNonEmptyLine(
                    "Siglas de aplicación (ej. BNMP): ",
                    ramaNonInteractiveHint(),
                )
            }
            if (sprintStr == null) {
                sprintStr = promptNonEmptyLine(
                    "Versión sprint (ej. V58-Sprint22.05): ",
                    ramaNonInteractiveHint(),
                )
            }
        }

        val t = try {
            BranchNameBuilder.parseTipo(tipoStr!!)
        } catch (e: IllegalStateException) {
            throw UsageError(e.message ?: "tipo inválido")
        }

        when (t) {
            BranchNameBuilder.TipoRama.FEATURE, BranchNameBuilder.TipoRama.HOTFIX -> {
                val h = ramaNonInteractiveHint()
                if (areaStr == null) areaStr = promptNonEmptyLine("Área (ej. DCSTI): ", h)
                if (huStr == null) huStr = promptNonEmptyLine("Referencia HU/ticket (ej. HU-116268): ", h)
            }

            BranchNameBuilder.TipoRama.RELEASE -> Unit
        }

        val branch = try {
            BranchNameBuilder.build(
                tipo = t,
                siglasApp = appStr!!,
                versionSprint = sprintStr!!,
                area = areaStr,
                empresa = when (t) {
                    BranchNameBuilder.TipoRama.RELEASE -> null
                    BranchNameBuilder.TipoRama.FEATURE, BranchNameBuilder.TipoRama.HOTFIX -> BnDefaults.EMPRESA
                },
                refHu = huStr,
            )
        } catch (e: IllegalArgumentException) {
            throw UsageError(e.message ?: "rama inválida")
        }

        echo("")
        echo("La rama será: $branch")
        echo("")
        echo("Creando rama…")
        gs.createBranch(branch)
        echo("Listo. Rama actual: $branch")
    }

    private fun ramaNonInteractiveHint() =
        "Usa --tipo/--app/--sprint (y --area/--hu si aplica), o build/install/.../bin/git-flow-cli rama …"
}

class RamaVerifyCommand : CliktCommand(
    name = "verify",
    help = "Comprueba que el nombre de la rama cumple el formato BN (rama actual o --name). Código 0 si es válido u omitido.",
) {
    private val name by option("--name", "-n", help = "Nombre de rama a validar (por defecto: rama actual)")

    private val quiet by option(
        "--quiet",
        "-q",
        help = "Sin mensaje en salida estándar si es válido u omitido (útil para el hook post-checkout).",
    ).flag(default = false)

    override fun run() {
        val gs = GitService()
        if (!gs.isInsideGitWorkTree()) {
            throw UsageError(CliMessages.NOT_IN_GIT_REPO)
        }
        val branch = name?.trim()?.takeIf { it.isNotEmpty() }
            ?: gs.currentBranchName()
            ?: throw UsageError("No se obtuvo la rama actual (git rev-parse --abbrev-ref HEAD).")
        when (val v = BranchNameValidator.validate(branch)) {
            BranchNameValidator.ValidationResult.Ok -> {
                if (!quiet) {
                    echo("Rama BN válida: $branch")
                }
                exitProcess(0)
            }

            BranchNameValidator.ValidationResult.Skipped -> {
                if (!quiet) {
                    echo("Sin validación BN: $branch")
                }
                exitProcess(0)
            }

            is BranchNameValidator.ValidationResult.Invalid -> {
                System.err.println("git-flow-cli: ${v.reason}")
                System.err.println("Formato esperado: ${BranchNameValidator.FORMAT_HINT}")
                exitProcess(1)
            }
        }
    }
}
