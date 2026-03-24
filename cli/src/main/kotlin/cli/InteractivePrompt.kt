package dev.donmanuel.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError

fun CliktCommand.promptNonEmptyLine(label: String, nonInteractiveHint: String): String {
    while (true) {
        echo(label)
        val line = readlnOrNull()
            ?: throw UsageError(
                "Entrada interactiva no disponible (stdin cerrado). $nonInteractiveHint",
            )
        val v = line.trim()
        if (v.isNotEmpty()) return v
        echo("Valor vacío; reintenta.")
    }
}
