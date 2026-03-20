package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import dev.donmanuel.cli.core.GitService
import dev.donmanuel.cli.core.Slugifier

class CreateFlowCommand : CliktCommand() {

    private val tipo by option(help = "Tipo (feature, bugfix, hotfix, chore)")
        .prompt("Tipo")

    private val modulo by option(help = "Módulo")
        .prompt("Módulo")

    private val id by option(help = "ID ticket")
        .prompt("ID")

    private val descripcion by option(help = "Descripción")
        .prompt("Descripción")

    override fun run() {
        val git = GitService()

        validateTipo(tipo)

        val slug = Slugifier.toSlug(descripcion)
        val branch = "$tipo/$modulo/$id-$slug"

        val commitType = mapTipoToCommit(tipo)
        val commitMsg = "canales_digitales|$modulo|NOVACOMP|$commitType $id| $descripcion"

        echo("🚀 Creando rama: $branch")

        git.createBranch(branch)
        git.addAll()
        git.commit(commitMsg)

        echo("✅ Commit creado:")
        echo(commitMsg)
    }

    private fun validateTipo(tipo: String) {
        val valid = listOf("feature", "bugfix", "hotfix", "chore")
        if (tipo !in valid) {
            error("Tipo inválido: $tipo")
        }
    }

    private fun mapTipoToCommit(tipo: String): String {
        return when (tipo) {
            "feature" -> "HU"
            "bugfix", "hotfix" -> "BUG"
            "chore" -> "MEJORA"
            else -> error("Tipo inválido")
        }
    }
}