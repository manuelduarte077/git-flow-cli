package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.donmanuel.cli.CliVersion

class GitFlowCliCommand : CliktCommand(
    name = "git-flow-cli",
    help = "Herramientas para ramas y commits con formato BN / canales digitales. " +
            "Ejemplos: git-flow-cli rama --help · git-flow-cli rama verify · git-flow-cli cc -t TICKET -m \"descripción\" · " +
            "git-flow-cli hooks install · git-flow-cli doctor · git-flow-cli init · git-flow-cli generate-completion bash.",
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
            DoctorCommand(),
            InitCommand(),
            CompletionCommand(),
        )
    }

    override fun run() = Unit
}
