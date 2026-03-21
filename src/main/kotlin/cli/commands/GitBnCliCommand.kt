package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class GitBnCliCommand : CliktCommand(
    name = "git-bn-cli",
    help = "Herramientas para ramas y commits con formato BN / canales digitales.",
    invokeWithoutSubcommand = false,
) {
    init {
        subcommands(
            RamaCommand(),
            CcCommand(),
            HooksCommand(),
        )
    }

    override fun run() = Unit
}
