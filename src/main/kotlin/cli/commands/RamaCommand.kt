package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.option
import dev.donmanuel.cli.core.BranchNameBuilder
import dev.donmanuel.cli.core.GitService

class RamaCommand : CliktCommand(
    name = "rama",
    help = "Crea y cambia a una rama con el formato BN (feature/hotfix/release). Sin flags, modo interactivo.",
) {

    private val tipo by option("-t", "--tipo", help = "feature, hotfix o release")

    private val app by option("-a", "--app", help = "Siglas aplicación (ej. BNMP)")

    private val sprint by option("-s", "--sprint", help = "Versión sprint (ej. V58-Sprint22.05)")

    private val area by option("--area", help = "Requerido para feature/hotfix (ej. DCSTI)")

    private val empresa by option("--empresa", help = "Requerido para feature/hotfix (ej. BABEL)")

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
        var empresaStr = empresa
        var huStr = hu

        if (tipoStr == null || appStr == null || sprintStr == null) {
            echo("Modo interactivo. Pulsa Enter tras cada valor.")
            if (tipoStr == null) {
                tipoStr = promptNonEmpty("Tipo de rama (feature / hotfix / release): ")
            }
            if (appStr == null) {
                appStr = promptNonEmpty("Siglas de aplicación (ej. BNMP): ")
            }
            if (sprintStr == null) {
                sprintStr = promptNonEmpty("Versión sprint (ej. V58-Sprint22.05): ")
            }
        }

        val t = try {
            BranchNameBuilder.parseTipo(tipoStr!!)
        } catch (e: IllegalStateException) {
            throw UsageError(e.message ?: "tipo inválido")
        }

        when (t) {
            BranchNameBuilder.TipoRama.FEATURE, BranchNameBuilder.TipoRama.HOTFIX -> {
                if (areaStr == null) areaStr = promptNonEmpty("Área (ej. DCSTI): ")
                if (empresaStr == null) empresaStr = promptNonEmpty("Empresa (ej. BABEL): ")
                if (huStr == null) huStr = promptNonEmpty("Referencia HU/ticket (ej. HU-116268): ")
            }
            BranchNameBuilder.TipoRama.RELEASE -> Unit
        }

        val branch = try {
            BranchNameBuilder.build(
                tipo = t,
                siglasApp = appStr!!,
                versionSprint = sprintStr!!,
                area = areaStr,
                empresa = empresaStr,
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

    private fun promptNonEmpty(label: String): String {
        while (true) {
            echo(label)
            val v = readln().trim()
            if (v.isNotEmpty()) return v
            echo("Valor vacío; reintenta.")
        }
    }
}
