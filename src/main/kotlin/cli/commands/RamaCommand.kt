package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import dev.donmanuel.cli.core.BranchNameBuilder
import dev.donmanuel.cli.core.GitService

class RamaCommand : CliktCommand(
    name = "rama",
    help = "Crea y cambia a una rama con el formato BN (feature/hotfix/release). No hace commit.",
) {

    private val tipo by option("-t", "--tipo", help = "feature, hotfix o release")
        .required()

    private val app by option("-a", "--app", help = "Siglas aplicación (ej. BNMP)")
        .required()

    private val sprint by option("-s", "--sprint", help = "Versión sprint (ej. V58-Sprint22.05)")
        .required()

    private val area by option("--area", help = "Requerido para feature/hotfix (ej. DCSTI)")

    private val empresa by option("--empresa", help = "Requerido para feature/hotfix (ej. BABEL)")

    private val hu by option("--hu", help = "Referencia HU/ticket (ej. HU-116268)")

    override fun run() {
        val t = try {
            BranchNameBuilder.parseTipo(tipo)
        } catch (e: IllegalStateException) {
            throw UsageError(e.message ?: "tipo inválido")
        }
        val branch = try {
            BranchNameBuilder.build(
                tipo = t,
                siglasApp = app,
                versionSprint = sprint,
                area = area,
                empresa = empresa,
                refHu = hu,
            )
        } catch (e: IllegalArgumentException) {
            throw UsageError(e.message ?: "rama inválida")
        }
        echo("Creando rama: $branch")
        GitService().createBranch(branch)
        echo("Listo. Rama actual: $branch")
    }
}
