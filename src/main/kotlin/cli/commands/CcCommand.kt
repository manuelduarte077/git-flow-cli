package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.core.CommitMessageValidator
import dev.donmanuel.cli.core.GitService

class CcCommand : CliktCommand(
    name = "cc",
    help = "Commit con formato pipe (canal|subcanal|empresa|ticket| descripción); ejecuta git commit salvo --print.",
) {

    private val ticket by option("-t", "--ticket", help = "Ticket (ej. \"BUG 886814\", \"HU-116268\", \"MEJ-857175\")")
        .required()

    private val descripcion by option("-m", "--descripcion", help = "Texto descriptivo del cambio")
        .required()

    private val canal by option("--canal", help = "Sobreescribe canal (por defecto desde .git-flow-cli.toml)")

    private val subcanal by option("--subcanal", help = "Sobreescribe subcanal")

    private val empresa by option("--empresa", help = "Sobreescribe empresa")

    private val printOnly by option("--print", "-p", help = "Solo imprime el mensaje; no ejecuta git commit")
        .flag(default = false)

    override fun run() {
        val cfgPath = ConfigFinder.findConfigFile()
        val cfg = cfgPath?.let { BnConfig.load(it) }

        val c = canal ?: cfg?.canal
            ?: throw UsageError("Falta 'canal': añade .git-flow-cli.toml en la raíz del repo (o .git-bn-cli.toml) o usa --canal")
        val sc = subcanal ?: cfg?.subcanal
            ?: throw UsageError("Falta 'subcanal': añade .git-flow-cli.toml (o .git-bn-cli.toml) o usa --subcanal")
        val emp = empresa ?: cfg?.empresa
            ?: throw UsageError("Falta 'empresa': añade .git-flow-cli.toml (o .git-bn-cli.toml) o usa --empresa")

        val line = "$c|$sc|$emp|$ticket| ${descripcion.trim()}"

        when (val v = CommitMessageValidator.validate(line)) {
            is CommitMessageValidator.ValidationResult.Invalid -> throw UsageError(v.reason)
            CommitMessageValidator.ValidationResult.Skipped -> { }
            CommitMessageValidator.ValidationResult.Ok -> { }
        }

        if (printOnly) {
            echo(line)
            return
        }

        echo(line)
        GitService().commit(line)
        echo("Commit creado.")
    }
}
