package dev.donmanuel.cli.config

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/** Plantilla única empaquetada desde la raíz del repo (`git-flow-cli.example.toml`) vía Gradle. */
object ExampleTomlTemplate {

    fun loadText(): String {
        val stream = ExampleTomlTemplate::class.java.getResourceAsStream("/git-flow-cli.example.toml")
            ?: error("git-flow-cli.example.toml no encontrada en el classpath (:core)")
        return stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }

    /** `true` si el archivo existe y su contenido coincide con la plantilla del ejemplo (sin crear duplicados). */
    fun contentMatchesTemplate(path: Path): Boolean {
        if (!Files.isRegularFile(path)) return false
        val text = Files.readString(path, StandardCharsets.UTF_8)
        return normalize(text) == normalize(loadText())
    }

    private fun normalize(s: String): String =
        s.trim().replace("\r\n", "\n").replace('\r', '\n')
}
