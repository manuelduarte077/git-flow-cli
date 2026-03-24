package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.donmanuel.cli.CliVersion

class GitFlowCliCommand : CliktCommand(
    name = "git-flow-cli",
    help = "Herramientas para ramas y commits con formato BN / canales digitales. " +
        "Ejemplos: git-flow-cli rama --help · git-flow-cli cc -t TICKET -m \"descripción\" · git-flow-cli hooks install.",
    invokeWithoutSubcommand = false,
) {
    init {
        versionOption(
            CliVersion.current(),
            names = setOf("--version", "-V"),
        )
        subcommands(
            RamaCommand(),
            CcCommand(),
            HooksCommand(),
            AboutCommand(),
        )
    }

    override fun run() = Unit
}
