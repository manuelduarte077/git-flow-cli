package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import dev.donmanuel.cli.CliMessages
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.config.ExampleTomlTemplate
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class InitCommand : CliktCommand(
    name = "init",
    help = "Crea ${BnConfig.FILE_NAME} en la raíz del repo con una plantilla mínima si aún no existe.",
) {
    override fun run() {
        val root = ConfigFinder.findGitRoot()
            ?: throw UsageError(CliMessages.NOT_IN_GIT_REPO)
        val target = root.resolve(BnConfig.FILE_NAME).normalize()
        if (target.exists()) {
            echo("Ya existe: ${target.toAbsolutePath()}")
            return
        }
        target.toFile().writeText(ExampleTomlTemplate.loadText())
        echo("Creado: ${target.absolutePathString()}")
    }
}
