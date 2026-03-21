package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.option
import dev.donmanuel.cli.promptNonEmptyLine
import dev.donmanuel.cli.config.BnDefaults
import dev.donmanuel.cli.core.BranchNameBuilder
import dev.donmanuel.cli.core.GitService

class RamaCommand : CliktCommand(
    name = "rama",
    help = "Crea y cambia a una rama con el formato BN (feature/hotfix/release). Sin flags, modo interactivo. En feature/hotfix la empresa en el nombre es siempre NOVACOMP.",
) {

    private val tipo by option("-t", "--tipo", help = "feature, hotfix o release")

    private val app by option("-a", "--app", help = "Siglas aplicación (ej. BNMP)")

    private val sprint by option("-s", "--sprint", help = "Versión sprint (ej. V58-Sprint22.05)")

    private val area by option("--area", help = "Requerido para feature/hotfix (ej. DCSTI)")

    private val hu by option("--hu", help = "Referencia HU/ticket (ej. HU-116268)")

    override fun run() {
        val gs = GitService()
        if (!gs.isInsideGitWorkTree()) {
            throw UsageError("No estás dentro de un repositorio Git.")
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
        "Pasa --tipo, --app, --sprint (y si aplica --area, --hu), o ejecuta el binario " +
            "tras ./gradlew installDist: build/install/git-flow-cli/bin/git-flow-cli rama …"
}
